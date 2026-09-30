// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.blaze3d.opengl.debug;

import com.optica.impl.mc.blaze3d.opengl.GlDebugLabelExt;
import com.optica.impl.mc.blaze3d.opengl.textures.IGlTexture;
import net.minecraft.util.StringUtil;
import org.lwjgl.opengl.KHRDebug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static org.lwjgl.opengl.GL11.GL_TEXTURE;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlDebugLabel$Core")
@Implements(@Interface(iface = GlDebugLabelExt.class, prefix = "ph$"))
public abstract class GlCoreDebugLabelMixin {
    @Shadow
    @Final
    private int maxLabelLength;

    public void ph$applyLabel(IGlTexture texture) {
        KHRDebug.glObjectLabel(GL_TEXTURE, texture.handle(), StringUtil.truncateStringIfNecessary(texture.ph$label(), this.maxLabelLength, true));
    }
}
