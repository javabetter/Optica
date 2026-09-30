// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.pipeline.buffer;

import com.optica.api.gpu.buffers.IGpuBuffer;
import com.optica.api.gpu.buffers.heap.IGpuBufferHeap;

import java.util.function.Supplier;

public interface IBufferHolder {
    void addDefaultBuffer(String name, Supplier<IGpuBuffer> buffer);

    void addDefaultBufferHeap(String name, Supplier<IGpuBufferHeap> buffer);
}
