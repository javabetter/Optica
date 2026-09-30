// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mc.blaze3d.opengl.buffer;

import com.optica.api.gpu.buffers.BufferUsage;
import com.optica.api.gpu.buffers.IGpuBuffer;
import com.optica.api.gpu.buffers.heap.AbstractGpuBufferHeap;
import com.optica.api.gpu.buffers.heap.IGpuBufferHeap;
import com.optica.api.gpu.buffers.heap.MemoryView;
import com.optica.api.gpu.systems.IGpuDevice;
import com.optica.core.rendering.NativeMemory;
import com.optica.impl.mixins.mc.blaze3d.opengl.buffer.GlBufferAccessor;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL30C.GL_MAP_INVALIDATE_RANGE_BIT;
import static org.lwjgl.opengl.GL30C.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL45C.glMapNamedBufferRange;
import static org.lwjgl.opengl.GL45C.glUnmapNamedBuffer;

public class GlBufferHeap extends AbstractGpuBufferHeap {
    public static final int NO_PERSISTENCE_MAPPING = 1 << 20;

    private final IGpuBuffer gpuBuffer;
    public final ByteBuffer buffer;

    private final int handle;

    private final Queue<Region> uploadQueue = new ConcurrentLinkedQueue<>();

    public GlBufferHeap(
            IGpuDevice device,
            @Nullable Supplier<String> label,
            long byteSize,
            @BufferUsage int usage
    ) {
        this.gpuBuffer = device.ph$createBuffer(label, byteSize, usage | BufferUsage.MAP_WRITE | NO_PERSISTENCE_MAPPING);
        this.buffer = NativeMemory.calloc(byteSize);

        this.handle = ((GlBufferAccessor) gpuBuffer).getHandle();
    }

    public IGpuBuffer buffer() {
        return gpuBuffer;
    }

    @Override
    public long capacity() {
        return gpuBuffer.ph$size();
    }

    @Nullable
    @Override
    public MemoryView allocate(long byteSize) {
        long ptr = allocatePtr(byteSize);

        return ptr == -1 ? null : new Allocation(ptr, byteSize);
    }

    @Nullable
    @Override
    public IGpuBufferHeap allocateHeap(long byteSize) {
        long ptr = allocatePtr(byteSize);

        return ptr == -1 ? null : new SubHeap(this, ptr, byteSize);
    }

    @Override
    public void upload() {
        var regionsToUpload = Region.takeFrom(uploadQueue);

        for (var region : regionsToUpload) {
            int offset = (int) region.begin();
            int length = (int) region.end() - offset;

            var slice = glMapNamedBufferRange(handle, offset, length, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_RANGE_BIT);
            Objects.requireNonNull(slice, "failed to map buffer range");

            try {
                slice.put(0, buffer, offset, length);
            } finally {
                glUnmapNamedBuffer(handle);
            }
        }
    }

    @Override
    public void close() {
        gpuBuffer.close();
        NativeMemory.free(buffer);
    }

    private class Allocation extends AbstractAllocation {
        private final ByteBuffer slice;

        private Allocation(long begin, long length) {
            super(begin, length);

            slice = buffer.slice(Math.toIntExact(begin), Math.toIntExact(length))
                    .order(buffer.order());
        }

        @Override
        public ByteBuffer buffer() {
            return slice;
        }

        @Override
        public void upload() {
            uploadQueue.add(this);
        }
    }

    private class SubHeap extends AbstractSubheap implements Region, IGpuBufferHeap {
        protected SubHeap(AbstractGpuBufferHeap root, long begin, long length) {
            super(root, begin, length);
        }

        @Override
        protected ByteBuffer createSlice(long begin, long length) {
            return buffer.slice(Math.toIntExact(begin), Math.toIntExact(length))
                    .order(buffer.order());
        }

        @Override
        protected void uploadSlice(Region region) {
            uploadQueue.add(region);
        }

        @Override
        protected IGpuBufferHeap createHeap(long begin, long length) {
            return new SubHeap(GlBufferHeap.this, begin, length);
        }
    }
}