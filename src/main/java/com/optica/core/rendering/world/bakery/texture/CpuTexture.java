// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.bakery.texture;

import com.optica.core.rendering.world.block.TextureData;

public interface CpuTexture {
    int sample(float u, float v);

    @FunctionalInterface
    interface Factory {
        CpuTexture create(int width, int height, int defaultValue, int[] data);
    }
}
