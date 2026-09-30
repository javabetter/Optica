// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.gpu.textures;

import com.optica.api.Disposable;

import java.util.OptionalDouble;

public interface IGpuSampler extends Disposable {
    IAddressMode ph$addressModeU();

    IAddressMode ph$addressModeV();

    IFilterMode ph$minFilter();

    IFilterMode ph$magFilter();

    int ph$maxAnisotropy();

    OptionalDouble ph$maxLod();
}
