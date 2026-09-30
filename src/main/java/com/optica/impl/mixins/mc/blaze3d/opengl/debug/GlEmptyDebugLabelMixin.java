// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.blaze3d.opengl.debug;

import com.optica.impl.mc.blaze3d.opengl.GlDebugLabelExt;
import com.optica.impl.mc.blaze3d.opengl.textures.IGlTexture;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlDebugLabel$Empty")
@Implements(@Interface(iface = GlDebugLabelExt.class, prefix = "ph$"))
public abstract class GlEmptyDebugLabelMixin {
    public void ph$applyLabel(IGlTexture texture) {

    }
}
