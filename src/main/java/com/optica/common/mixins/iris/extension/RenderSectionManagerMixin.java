// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris.extension;

import com.optica.common.iris.IrisUtil;
import com.optica.core.iris.IrisManager;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSectionManager.class)
public abstract class RenderSectionManagerMixin {
    @Inject(method = "onSectionAdded", at = @At("HEAD"))
    private void onSectionAdded(int x, int y, int z, CallbackInfo ci) {
        IrisManager.onSectionAdded(x, y, z);
    }
    
    @Inject(method = "scheduleRebuild", at = @At("HEAD"))
    private void scheduleRebuild(int x, int y, int z, boolean playerChanged, CallbackInfo ci) {
        IrisManager.onSectionChanged(x, y, z);
    }
}
