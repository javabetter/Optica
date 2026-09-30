// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris.extension;

import com.optica.common.iris.IrisUtil;
import com.optica.core.iris.IrisManager;
import com.optica.core.iris.rendering.PhotonicsPipeline;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(
            method = "renderLevel",
            at = @At("HEAD"),
            order = 900
    )
    // Only marks the frame start; takes no target arguments so renderLevel's signature (changed in 26.1) doesn't matter.
    public void renderLevel(CallbackInfo ci) {
        IrisManager.onFrameBegin();
    }
}
