// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.gpu.buffers.heap;

import com.optica.api.Disposable;

import java.nio.ByteBuffer;

public interface MemoryView extends Disposable {
    long begin();
    long end();

    default long size() {
        return end() - begin();
    }

    ByteBuffer buffer();

    void upload();

    static int intBufferBegin(MemoryView memory) {
        if (memory == null) throw new IllegalStateException();

        return (int) (memory.begin() >> 2);
    }
}
