package com.optica.core.iris.rendering.cached;

import com.optica.api.ModLoader;
import com.optica.api.gpu.buffers.BufferUsage;
import com.optica.api.gpu.buffers.IGpuBuffer;
import com.optica.api.gpu.systems.IRenderSystem;
import com.optica.core.Photonics;
import com.optica.core.iris.pipeline.buffer.IBufferHolder;
import com.optica.core.rendering.NativeMemory;
import com.optica.core.rendering.RenderingComponent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Optica's lighting cache profiler (Lighting Cache Settings > Profiler). The cache passes count what
 * they do into a small GPU buffer ({@code rendering/cached/profile.glsl}); about once a second it is
 * copied to a readback buffer, cleared, and read a few frames later (so the CPU never waits for the
 * GPU), and one line of per-frame averages is appended to {@code optica-profile.log} in the game folder.
 */
public final class CacheProfiler implements RenderingComponent {
    /** Counter indices, matching profile.glsl. */
    private static final int FRAMES = 0, PIXELS = 1, CORNERS = 2, FOUND = 3, CREATED = 4, FAIL_PROBE = 5,
            FAIL_QUEUE = 6, FAIL_RACE = 7, FAIL_OVERFLOW = 8, REFRESH_OUTDATED = 9, REFRESH_DIRTY = 10,
            REFRESH_REJECTED = 11, LEVEL0 = 12, COARSE = 16, QUEUE_REQUESTED = 17, QUEUE_PROCESSED = 18,
            BUDGET = 19, CURSOR_EMPTY = 20, CURSOR_IDLE = 21, CURSOR_COMPUTED = 22, ZERO_DIRECT = 23,
            BINS_MISS = 24, COVERED = 25, PARTIAL = 26, UNCOVERED = 27, HISTORY = 28, FALLBACK = 29,
            SLOT_NONE = 30, SLOT_MISMATCH = 31, NOT_COMPUTED = 32, LIGHTS_MAX = 33, VIEW_W = 34, VIEW_H = 35,
            BLENDED = 36;
    private static final int COUNTERS = 64;
    private static final int BYTES = COUNTERS * Integer.BYTES;

    private static final long WINDOW_NANOS = 1_000_000_000L;
    /** Frames to wait after the copy before reading it back. */
    private static final int READBACK_DELAY = 3;

    public static final String LOG_FILE = "optica-profile.log";

    private final SurfaceCache cache;
    private final String settings;

    private final IGpuBuffer counters;
    private final IGpuBuffer readback;
    private final ByteBuffer zeros;

    private BufferedWriter log;
    private long windowStart = System.nanoTime();
    private long lastFrame = System.nanoTime();
    private long cpuFrames = 0;
    private double maxFrameMs = 0;
    private int pendingDelay = -1;
    private long pendingCpuFrames;
    private double pendingSeconds, pendingMaxFrameMs;
    private long sectionsBefore, lightsBefore, regionsBefore;
    private long lines = 0;

    public CacheProfiler(SurfaceCache cache, String settings) {
        this.cache = cache;
        this.settings = settings;

        var device = IRenderSystem.getDevice();
        this.counters = device.ph$createBuffer(() -> "Optica Cache Profiler", BYTES, BufferUsage.COPY_DST | BufferUsage.COPY_SRC);
        this.readback = device.ph$createBuffer(() -> "Optica Cache Profiler Readback", BYTES, BufferUsage.COPY_DST | BufferUsage.MAP_READ);
        this.zeros = NativeMemory.calloc(BYTES);

        device.ph$createCommandEncoder().ph$writeToBuffer(counters, zeros.duplicate().clear());
        openLog();
    }

    private void openLog() {
        Path path = ModLoader.getGameDir().resolve(LOG_FILE);

        try {
            boolean fresh = !Files.exists(path);
            log = Files.newBufferedWriter(path, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            if (!fresh) log.newLine();

            log.write("# Optica lighting cache profile, " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    + ", Optica " + Photonics.getModVersion());
            log.newLine();
            log.write("# " + settings);
            log.newLine();
            log.write("# Each line: averages per frame over about one second. Columns are explained in docs/PROFILER.md.");
            log.newLine();
            log.flush();

            Photonics.LOGGER.info("Optica profiler: writing {}", path.toAbsolutePath());
        } catch (IOException e) {
            Photonics.LOGGER.error("Optica profiler: cannot write {}", path, e);
            log = null;
        }
    }

    @Override
    public void onFrameBegin() {
        long now = System.nanoTime();
        maxFrameMs = Math.max(maxFrameMs, (now - lastFrame) / 1e6);
        lastFrame = now;
        cpuFrames++;

        if (pendingDelay >= 0 && pendingDelay-- == 0) read();

        if (pendingDelay < 0 && now - windowStart >= WINDOW_NANOS) {
            var encoder = IRenderSystem.getDevice().ph$createCommandEncoder();
            encoder.ph$copyToBuffer(counters.ph$slice(0, BYTES), readback.ph$slice(0, BYTES));
            encoder.ph$writeToBuffer(counters, zeros.duplicate().clear());

            pendingDelay = READBACK_DELAY;
            pendingCpuFrames = cpuFrames;
            pendingSeconds = (now - windowStart) / 1e9;
            pendingMaxFrameMs = maxFrameMs;

            windowStart = now;
            cpuFrames = 0;
            maxFrameMs = 0;
        }
    }

    private void read() {
        long[] c = new long[COUNTERS];

        try (var view = IRenderSystem.getDevice().ph$createCommandEncoder().ph$mapBuffer(readback, true, false)) {
            var data = view.ph$data().duplicate().order(ByteOrder.nativeOrder());
            for (int i = 0; i < COUNTERS; i++) c[i] = Integer.toUnsignedLong(data.getInt(i * Integer.BYTES));
        }

        long sections = cache.sectionsUploaded - sectionsBefore;
        long lights = cache.lightsChanged - lightsBefore;
        long regions = cache.regionsAdded - regionsBefore;
        sectionsBefore = cache.sectionsUploaded;
        lightsBefore = cache.lightsChanged;
        regionsBefore = cache.regionsAdded;

        write(c, sections, lights, regions);
    }

    private void write(long[] c, long sections, long lights, long regions) {
        if (log == null) return;

        double frames = Math.max(c[FRAMES], 1);
        double pixels = Math.max(c[PIXELS], 1);
        double corners = Math.max(c[CORNERS], 1);
        long visited = c[CURSOR_EMPTY] + c[CURSOR_IDLE] + c[CURSOR_COMPUTED];

        StringBuilder line = new StringBuilder();
        line.append(String.format(Locale.ROOT, "t=%s fps=%.0f maxFrameMs=%.1f gpuFrames=%d view=%dx%d lights=%d",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                pendingCpuFrames / Math.max(pendingSeconds, 1e-3), pendingMaxFrameMs, c[FRAMES], c[VIEW_W], c[VIEW_H], c[LIGHTS_MAX]));
        line.append(String.format(Locale.ROOT, " | pixels=%.0f level0/1/2/3/coarse%%=%.0f/%.0f/%.0f/%.0f/%.0f blended%%=%.0f",
                c[PIXELS] / frames,
                100 * c[LEVEL0] / pixels, 100 * c[LEVEL0 + 1] / pixels, 100 * c[LEVEL0 + 2] / pixels, 100 * c[LEVEL0 + 3] / pixels,
                100 * c[COARSE] / pixels, 100 * c[BLENDED] / pixels));
        line.append(String.format(Locale.ROOT, " | samples=%.0f found%%=%.2f created=%.0f failProbe=%.0f failQueue=%.0f failRace=%.0f failOverflow=%.0f",
                c[CORNERS] / frames, 100 * c[FOUND] / corners, c[CREATED] / frames,
                c[FAIL_PROBE] / frames, c[FAIL_QUEUE] / frames, c[FAIL_RACE] / frames, c[FAIL_OVERFLOW] / frames));
        line.append(String.format(Locale.ROOT, " | refreshOutdated=%.0f refreshDirty=%.0f refreshRejected=%.0f",
                c[REFRESH_OUTDATED] / frames, c[REFRESH_DIRTY] / frames, c[REFRESH_REJECTED] / frames));
        line.append(String.format(Locale.ROOT, " | queueAsked=%.0f queueDone=%.0f budget=%.0f cursorComputed=%.0f tableUsed%%=%.1f zeroDirect=%.0f binsMiss=%.0f",
                c[QUEUE_REQUESTED] / frames, c[QUEUE_PROCESSED] / frames, c[BUDGET] / frames, c[CURSOR_COMPUTED] / frames,
                visited == 0 ? 0.0 : 100.0 * (c[CURSOR_IDLE] + c[CURSOR_COMPUTED]) / visited,
                c[ZERO_DIRECT] / frames, c[BINS_MISS] / frames));
        line.append(String.format(Locale.ROOT, " | covered%%=%.2f partial%%=%.2f uncovered%%=%.2f history%%=%.1f standIn%%=%.2f slotNone=%.0f slotMismatch=%.0f notComputed=%.0f",
                100 * c[COVERED] / pixels, 100 * c[PARTIAL] / pixels, 100 * c[UNCOVERED] / pixels,
                100 * c[HISTORY] / pixels, 100 * c[FALLBACK] / pixels,
                c[SLOT_NONE] / frames, c[SLOT_MISMATCH] / frames, c[NOT_COMPUTED] / frames));
        line.append(String.format(Locale.ROOT, " | sectionsUploaded=%d lightsChanged=%d dirtyRegions=%d", sections, lights, regions));

        try {
            log.write(line.toString());
            log.newLine();
            log.flush();
        } catch (IOException e) {
            Photonics.LOGGER.error("Optica profiler: write failed", e);
            log = null;
        }

        lines++;
    }

    @Override
    public void registerBuffers(IBufferHolder buffers) {
        buffers.addDefaultBuffer("ph_surface_cache_profile", () -> counters);
    }

    @Override
    public void close() {
        counters.close();
        readback.close();
        NativeMemory.free(zeros);

        if (log != null) {
            try {
                log.write("# end, " + lines + " lines");
                log.newLine();
                log.close();
            } catch (IOException ignored) {
            }
        }
    }
}
