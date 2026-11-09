// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris.pipeline.passes;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.optica.common.iris.IrisUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Binds Photonics' buffers to every program the game uses, so terrain and other gbuffers programs can
 * read them.
 *
 * <p>Optica (26.2): Sodium no longer has its own GL program class; it draws terrain through Blaze3D
 * render pipelines like the rest of the game, so every program is made current through
 * {@code GlStateManager._glUseProgram}. HEAD, because it returns early when the program is already
 * current, and the buffer bindings may have changed since. Block indices are cached per program, so
 * programs without Photonics buffers cost one map lookup.
 */
@Mixin(GlStateManager.class)
public abstract class GlProgramMixin {
    @Inject(method = "_glUseProgram", at = @At("HEAD"))
    private static void bindPhotonicsBuffers(int program, CallbackInfo ci) {
        if (program != 0) IrisUtil.bindBuffers(program);
    }
}
