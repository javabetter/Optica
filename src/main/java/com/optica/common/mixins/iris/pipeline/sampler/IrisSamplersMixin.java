// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris.pipeline.sampler;

import com.optica.common.iris.IrisUtil;
import com.optica.core.iris.IrisManager;
import com.optica.core.iris.pipeline.texture.ISamplerHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.samplers.IrisSamplers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IrisSamplers.class)
public abstract class IrisSamplersMixin {
    @Inject(
            method = "addCustomTextures",
            at = @At("TAIL")
    )
    private static void addCustomTextures(
            SamplerHolder samplers,
            Object2ObjectMap<String, TextureAccess> irisCustomTextures,
            CallbackInfo ci
    ) {
        IrisManager.registerCustomTextures((ISamplerHolder) samplers);
    }
}
