// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris.pipeline.passes;

import com.optica.common.iris.IrisUtil;
import net.caffeinemc.mods.sodium.client.gl.shader.GlProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlProgram.class)
public abstract class GlProgramMixin {
    @Inject(method = "bind", at = @At("TAIL"))
    public void applyTail(CallbackInfo ci) {
        IrisUtil.bindBuffers(((GlProgram<?>) (Object) this).handle());
    }
}
