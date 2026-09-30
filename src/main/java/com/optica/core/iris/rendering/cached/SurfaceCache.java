package com.optica.core.iris.rendering.cached;

import com.optica.api.gpu.buffers.BufferUsage;
import com.optica.api.gpu.buffers.IGpuBuffer;
import com.optica.api.gpu.systems.IRenderSystem;
import com.optica.core.iris.pipeline.buffer.IBufferHolder;
import com.optica.core.rendering.NativeMemory;
import com.optica.core.rendering.RenderingComponent;

/**
 * The GPU storage of the cached lighting mode: a hash table of lighting samples on block faces, and a
 * small state buffer (per-frame queue of new entries, refresh cursor). Both live only on the GPU; the
 * shaders in {@code rendering/cached} own their contents. See {@code rendering/cached/cache.glsl}.
 */
public final class SurfaceCache implements RenderingComponent {
    /** Must match cache.glsl. */
    public static final int ENTRY_UINTS = 8;
    public static final int STATE_HEADER_UINTS = 4;
    public static final int QUEUE_MAX = 65536;

    private static final int CLEAR_CHUNK_BYTES = 4 << 20;

    private final IGpuBuffer entries;
    private final IGpuBuffer state;

    public SurfaceCache(int capacityLog2) {
        var device = IRenderSystem.getDevice();

        long entryBytes = (1L << capacityLog2) * ENTRY_UINTS * Integer.BYTES;
        long stateBytes = (long) (STATE_HEADER_UINTS + QUEUE_MAX) * Integer.BYTES;

        this.entries = device.ph$createBuffer(() -> "Optica Surface Cache", entryBytes, BufferUsage.COPY_DST);
        this.state = device.ph$createBuffer(() -> "Optica Surface Cache State", stateBytes, BufferUsage.COPY_DST);

        clear(entries);
        clear(state);
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
    }

    @Override
    public void close() {
        entries.close();
        state.close();
    }
}
