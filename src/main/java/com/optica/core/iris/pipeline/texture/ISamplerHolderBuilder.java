// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.pipeline.texture;

import com.optica.api.gpu.textures.IGpuTexture;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ISamplerHolderBuilder<T> {
    T withSampler(Consumer<ISamplerHolder> consumer);

    default T sampler(String name, Supplier<IGpuTexture.WithSampler<?>> textureAndSampler) {
        return withSampler(samplers -> samplers.addSampler(name, textureAndSampler));
    }

    default T defaultSampler(String name, Supplier<IGpuTexture<?>> texture) {
        return withSampler(samplers -> samplers.addDefaultSampler(name, texture));
    }
}
