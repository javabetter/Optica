// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.gpu.systems;

public interface IRenderSystem {
    static IGpuDevice getDevice() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
