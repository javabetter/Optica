// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.bakery.texture;

import com.optica.core.rendering.world.block.TextureData;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

import java.util.Objects;

public class Rgba8Texture implements CpuTexture {
    private final int width, height;

    private final int defaultValue;
    private final int[] color;

    public Rgba8Texture(
            int width, int height,
            int defaultValue,
            int[] data
    ) {
        Objects.requireNonNull(data);

        this.width = width;
        this.height = height;

        this.defaultValue = defaultValue;
        this.color = data;
    }

    @Override
    public int sample(float u, float v) {
        int realU = Math.clamp(Math.round(u * (width - 0.5f)), 0, width - 1);
        int realV = Math.clamp(Math.round(v * (height - 0.5f)), 0, height - 1);

        int index = (width * realV) + realU;
        if (index > color.length) return defaultValue;

        return fromABGR(color[index]);
    }

    private static int fromABGR(int value) {
        return Integer.reverseBytes(Integer.rotateLeft(value, 8));
    }
}
