// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.gpu.buffers;

public interface IGpuBufferSlice {
    IGpuBuffer ph$buffer();

    long ph$offset();
    long ph$length();

    IGpuBufferSlice ph$slice(long offset, long length);
}
