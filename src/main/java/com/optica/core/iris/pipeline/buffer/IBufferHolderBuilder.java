// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.pipeline.buffer;

import com.optica.api.gpu.buffers.IGpuBuffer;
import com.optica.api.gpu.buffers.heap.IGpuBufferHeap;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IBufferHolderBuilder<T> {
    T withBuffer(Consumer<IBufferHolder> consumer);

    default T buffer(String name, Supplier<IGpuBuffer> buffer) {
        return withBuffer(buffers -> buffers.addDefaultBuffer(name, buffer));
    }

    default T bufferHeap(String name, Supplier<IGpuBufferHeap> buffer) {
        return withBuffer(buffers -> buffers.addDefaultBufferHeap(name, buffer));
    }
}
