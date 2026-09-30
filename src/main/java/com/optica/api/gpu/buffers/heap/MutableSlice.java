// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.gpu.buffers.heap;

class MutableSlice implements MemorySlice {
    public long begin;
    public long end;

    MutableSlice(long begin, long end) {
        this.begin = begin;
        this.end = end;
    }

    MutableSlice(MemorySlice slice) {
        this.begin = slice.begin();
        this.end = slice.end();
    }

    @Override
    public long begin() {
        return begin;
    }

    @Override
    public long end() {
        return end;
    }
}
