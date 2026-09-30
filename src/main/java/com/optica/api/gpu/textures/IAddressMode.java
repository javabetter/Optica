// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.gpu.textures;

public interface IAddressMode {
    static IAddressMode repeat() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IAddressMode clampToEdge() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
