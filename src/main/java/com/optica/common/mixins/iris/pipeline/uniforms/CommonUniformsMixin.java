// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris.pipeline.uniforms;

import com.optica.core.iris.IrisManager;
import com.optica.core.iris.pipeline.uniform.IDynamicUniformHolder;
import com.optica.core.iris.pipeline.uniform.IUniformHolder;
import com.optica.common.iris.IrisUtil;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.shaderpack.IdMap;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommonUniforms.class)
public abstract class CommonUniformsMixin {
    @Inject(
            method = "addNonDynamicUniforms",
            at = @At("TAIL")
    )
    private static void addNonDynamicUniforms(UniformHolder uniforms, IdMap idMap, PackDirectives directives, FrameUpdateNotifier updateNotifier, CallbackInfo ci) {
        IrisManager.registerUniforms((IUniformHolder) uniforms);
    }

    @Inject(
            method = "addDynamicUniforms",
            at = @At("TAIL")
    )
    private static void addDynamicUniforms(DynamicUniformHolder uniforms, FogMode fogMode, CallbackInfo ci) {
        IrisManager.registerDynamicUniforms((IDynamicUniformHolder) uniforms);
    }
}
