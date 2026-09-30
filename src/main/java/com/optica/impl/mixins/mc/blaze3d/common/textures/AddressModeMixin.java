// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.blaze3d.common.textures;

import com.optica.api.gpu.textures.IAddressMode;
import com.optica.api.gpu.textures.IFilterMode;
import com.mojang.blaze3d.textures.AddressMode;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AddressMode.class)
@Implements(@Interface(iface = IAddressMode.class, prefix = "ph$"))
public abstract class AddressModeMixin {
}
