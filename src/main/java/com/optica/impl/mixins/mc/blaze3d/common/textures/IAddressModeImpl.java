// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.blaze3d.common.textures;

import com.optica.api.gpu.textures.IAddressMode;
import com.mojang.blaze3d.textures.AddressMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(IAddressMode.class)
public interface IAddressModeImpl {
    @Overwrite
    static IAddressMode repeat() {
        return (IAddressMode) (Object) AddressMode.REPEAT;
    }

    @Overwrite
    static IAddressMode clampToEdge() {
        return (IAddressMode) (Object) AddressMode.CLAMP_TO_EDGE;
    }
}
