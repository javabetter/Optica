// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.blaze3d.opengl;

import com.optica.api.gpu.textures.ITextureFormat;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(InternalTextureFormat.class)
@Implements(@Interface(iface = ITextureFormat.class, prefix = "ph$"))
public abstract class InternalTextureFormatMixin implements ITextureFormat {
}
