package com.optica.core.iris.rendering.cached;

import com.optica.api.gpu.buffers.BufferUsage;
import com.optica.api.gpu.buffers.IGpuBuffer;
import com.optica.api.gpu.systems.IRenderSystem;
import com.optica.core.iris.pipeline.buffer.IBufferHolder;
import com.optica.core.iris.pipeline.uniform.IUniformHolder;
import com.optica.core.iris.pipeline.uniform.IUniformUpdateFrequency;
import com.optica.core.rendering.NativeMemory;
import com.optica.core.rendering.RenderingComponent;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The GPU storage of the cached lighting mode: a hash table of lighting samples on block faces, and a
 * small state buffer (per-frame queue of new entries, refresh cursor). Both live only on the GPU; the
 * shaders in {@code rendering/cached} own their contents. See {@code rendering/cached/cache.glsl}.
 *
 * <p>It also tracks where the world changed. When rebuilt chunk sections (blocks placed or removed) or
 * a changed light list reach the GPU, the affected area becomes a dirty region, stamped with that
 * frame, and cache samples inside it computed before then are recomputed as soon as they are seen.
 * Without this, changes only showed up as the rotating refresh reached each sample, a patchwork that
 * could look like lighting updating halfway and then reverting.
 */
public final class SurfaceCache implements RenderingComponent {
    /** Must match cache.glsl. */
    public static final int ENTRY_UINTS = 8;
    public static final int STATE_HEADER_UINTS = 4;
    public static final int QUEUE_MAX = 65536;

    /** Dirty regions kept on the GPU (beyond that, the closest ones are merged). Must match cache.glsl. */
    public static final int DIRTY_MAX = 64;
    private static final int DIRTY_BOX_INTS = 8;
    private static final int DIRTY_HEADER_INTS = 4;
    /** A changed section affects lighting (shadows of nearby lights) this many blocks around it. */
    private static final int DIRTY_MARGIN = 16;
    /** Must match ph_cache_frame_period in cache.glsl. */
    public static final int FRAME_PERIOD = 720720;

    private static final int CLEAR_CHUNK_BYTES = 4 << 20;

    private final IGpuBuffer entries;
    private final IGpuBuffer state;
    private final IGpuBuffer dirty;
    private final ByteBuffer dirtyUpload;

    private record DirtyBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int frame) {
    }

    /** The cache's frame clock (the shaders' optica_cache_frame), wrapping at FRAME_PERIOD. */
    private int frame = 0;
    /** Totals for the profiler (CacheProfiler). */
    long sectionsUploaded = 0;
    long lightsChanged = 0;
    long regionsAdded = 0;

    /** Regions (min xyz, max xyz in blocks) reported since the last frame. */
    private final List<int[]> pending = new ArrayList<>();
    private final List<DirtyBox> boxes = new ArrayList<>();

    public SurfaceCache(int capacityLog2) {
        var device = IRenderSystem.getDevice();

        long entryBytes = (1L << capacityLog2) * ENTRY_UINTS * Integer.BYTES;
        long stateBytes = (long) (STATE_HEADER_UINTS + QUEUE_MAX) * Integer.BYTES;

        long dirtyBytes = (long) (DIRTY_HEADER_INTS + DIRTY_MAX * DIRTY_BOX_INTS) * Integer.BYTES;

        this.entries = device.ph$createBuffer(() -> "Optica Surface Cache", entryBytes, BufferUsage.COPY_DST);
        this.state = device.ph$createBuffer(() -> "Optica Surface Cache State", stateBytes, BufferUsage.COPY_DST);
        this.dirty = device.ph$createBuffer(() -> "Optica Surface Cache Dirty Regions", dirtyBytes, BufferUsage.COPY_DST);
        this.dirtyUpload = NativeMemory.calloc(dirtyBytes);

        clear(entries);
        clear(state);
        clear(dirty);
    }

    /** World sections (block positions of their origin) whose blocks just reached the GPU. */
    public void onSectionsUploaded(List<Vector3i> sectionOrigins) {
        sectionsUploaded += sectionOrigins.size();
        for (var origin : sectionOrigins)
            pending.add(new int[] {
                    origin.x - DIRTY_MARGIN, origin.y - DIRTY_MARGIN, origin.z - DIRTY_MARGIN,
                    origin.x + 16 + DIRTY_MARGIN, origin.y + 16 + DIRTY_MARGIN, origin.z + 16 + DIRTY_MARGIN
            });
    }

    /** Lights (world position, reach) that were added, removed or changed in the light list just uploaded. */
    public void onLightsChanged(List<Vector4f> lights) {
        lightsChanged += lights.size();
        for (var light : lights) {
            int reach = (int) Math.ceil(light.w) + 1;
            int x = (int) Math.floor(light.x), y = (int) Math.floor(light.y), z = (int) Math.floor(light.z);
            pending.add(new int[] {x - reach, y - reach, z - reach, x + reach + 1, y + reach + 1, z + reach + 1});
        }
    }

    @Override
    public void onFrameBegin() {
        frame = (frame + 1) % FRAME_PERIOD;

        // Frame stamps wrap at FRAME_PERIOD; a box older than half of it would read as recent again.
        boolean expired = boxes.removeIf(box -> age(box.frame()) > FRAME_PERIOD / 2);
        if (pending.isEmpty() && !expired) return;

        // Joining a world or walking into new chunks reports hundreds of sections at once: group them
        // by area first, so far apart changes do not become one box over everything in between.
        List<int[]> grouped = pending;
        for (int cell = 64; grouped.size() > DIRTY_MAX / 2; cell *= 2)
            grouped = groupByCell(grouped, cell);

        for (var box : grouped) {
            boxes.add(new DirtyBox(box[0], box[1], box[2], box[3], box[4], box[5], frame));
            regionsAdded++;
        }
        pending.clear();

        // Over the limit, merge the two boxes whose union adds the least volume (keeping the newer
        // stamp). Boxes are never dropped: samples in one that were out of view would stay outdated.
        while (boxes.size() > DIRTY_MAX) mergeClosestPair();

        upload();
    }

    private int age(int stamp) {
        return Math.floorMod(frame - stamp, FRAME_PERIOD);
    }

    private static List<int[]> groupByCell(List<int[]> input, int cell) {
        Map<Long, int[]> cells = new HashMap<>();
        for (var box : input) {
            long cx = Math.floorDiv((box[0] + box[3]) >> 1, cell);
            long cy = Math.floorDiv((box[1] + box[4]) >> 1, cell);
            long cz = Math.floorDiv((box[2] + box[5]) >> 1, cell);
            long key = (cx & 0x1FFFFF) | ((cy & 0x1FFFFF) << 21) | ((cz & 0x1FFFFF) << 42);
            cells.merge(key, box.clone(), SurfaceCache::union);
        }
        return new ArrayList<>(cells.values());
    }

    private static int[] union(int[] a, int[] b) {
        return new int[] {
                Math.min(a[0], b[0]), Math.min(a[1], b[1]), Math.min(a[2], b[2]),
                Math.max(a[3], b[3]), Math.max(a[4], b[4]), Math.max(a[5], b[5])
        };
    }

    private static double volume(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return (double) (maxX - minX) * (maxY - minY) * (maxZ - minZ);
    }

    private static double volume(DirtyBox b) {
        return volume(b.minX(), b.minY(), b.minZ(), b.maxX(), b.maxY(), b.maxZ());
    }

    private void mergeClosestPair() {
        int bestI = 0, bestJ = 1;
        double bestGrowth = Double.MAX_VALUE;

        for (int i = 0; i < boxes.size(); i++) {
            var a = boxes.get(i);
            for (int j = i + 1; j < boxes.size(); j++) {
                var b = boxes.get(j);
                double growth = volume(
                        Math.min(a.minX(), b.minX()), Math.min(a.minY(), b.minY()), Math.min(a.minZ(), b.minZ()),
                        Math.max(a.maxX(), b.maxX()), Math.max(a.maxY(), b.maxY()), Math.max(a.maxZ(), b.maxZ())
                ) - volume(a) - volume(b);

                if (growth < bestGrowth) {
                    bestGrowth = growth;
                    bestI = i;
                    bestJ = j;
                }
            }
        }

        var a = boxes.get(bestI);
        var b = boxes.get(bestJ);
        int newer = age(a.frame()) <= age(b.frame()) ? a.frame() : b.frame();

        boxes.set(bestI, new DirtyBox(
                Math.min(a.minX(), b.minX()), Math.min(a.minY(), b.minY()), Math.min(a.minZ(), b.minZ()),
                Math.max(a.maxX(), b.maxX()), Math.max(a.maxY(), b.maxY()), Math.max(a.maxZ(), b.maxZ()),
                newer
        ));
        boxes.remove(bestJ);
    }

    private void upload() {
        dirtyUpload.clear();
        dirtyUpload.putInt(0, boxes.size());

        int index = 0;
        for (var box : boxes) {
            int offset = (DIRTY_HEADER_INTS + index * DIRTY_BOX_INTS) * Integer.BYTES;
            dirtyUpload.putInt(offset, box.minX());
            dirtyUpload.putInt(offset + 4, box.minY());
            dirtyUpload.putInt(offset + 8, box.minZ());
            dirtyUpload.putInt(offset + 12, box.frame());
            dirtyUpload.putInt(offset + 16, box.maxX());
            dirtyUpload.putInt(offset + 20, box.maxY());
            dirtyUpload.putInt(offset + 24, box.maxZ());
            dirtyUpload.putInt(offset + 28, 0);
            index++;
        }

        IRenderSystem.getDevice().ph$createCommandEncoder().ph$writeToBuffer(dirty, dirtyUpload.duplicate().clear());
    }

    @Override
    public void registerUniforms(IUniformHolder uniforms) {
        uniforms.uniform1i(IUniformUpdateFrequency.perFrame(), "optica_cache_frame", () -> frame);
    }

    /** Buffer contents start undefined; an all-zero entry is an empty slot. */
    private static void clear(IGpuBuffer buffer) {
        var encoder = IRenderSystem.getDevice().ph$createCommandEncoder();
        var zeros = NativeMemory.calloc(Math.min(CLEAR_CHUNK_BYTES, buffer.ph$size()));

        try {
            for (long offset = 0; offset < buffer.ph$size(); offset += zeros.capacity()) {
                long length = Math.min(zeros.capacity(), buffer.ph$size() - offset);
                encoder.ph$writeToBuffer(buffer.ph$slice(offset, length), zeros.slice(0, (int) length));
            }
        } finally {
            NativeMemory.free(zeros);
        }
    }

    @Override
    public void registerBuffers(IBufferHolder buffers) {
        buffers.addDefaultBuffer("ph_surface_cache", () -> entries);
        buffers.addDefaultBuffer("ph_surface_cache_state", () -> state);
        buffers.addDefaultBuffer("ph_surface_cache_dirty", () -> dirty);
    }

    @Override
    public void close() {
        entries.close();
        state.close();
        dirty.close();
        NativeMemory.free(dirtyUpload);
    }
}
