// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.gpu.buffers;

import com.optica.api.Disposable;

import java.nio.ByteBuffer;

public interface IGpuBuffer extends Disposable {
    long ph$size();

    @BufferUsage int ph$usage();

    boolean ph$isClosed();

    IGpuBufferSlice ph$slice(long offset, long length);

    interface MappedView extends Disposable {
        ByteBuffer ph$data();
    }
}
