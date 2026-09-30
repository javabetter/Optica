// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.pipeline.texture;

import com.optica.api.gpu.textures.IGpuTexture;

import java.util.function.Supplier;

public interface ISamplerHolder {
    void addSampler(String name, Supplier<IGpuTexture.WithSampler<?>> textureAndSampler);

    void addDefaultSampler(String name, Supplier<IGpuTexture<?>> texture);
}
