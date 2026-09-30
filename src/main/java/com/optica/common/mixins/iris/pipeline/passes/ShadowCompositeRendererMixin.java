// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris.pipeline.passes;

import com.optica.common.iris.IrisUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.iris.gl.program.ComputeProgram;
import net.irisshaders.iris.gl.program.Program;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shadows.ShadowCompositeRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShadowCompositeRenderer.class)
public abstract class ShadowCompositeRendererMixin {
    @Shadow
    @Final
    private WorldRenderingPipeline pipeline;

    @WrapOperation(
            method = "renderAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/gl/program/ComputeProgram;use()V"
            )
    )
    public void useCompute(ComputeProgram instance, Operation<Void> original) {
        IrisUtil.bindBuffers(pipeline, instance.getProgramId());
        original.call(instance);
    }

    @WrapOperation(
            method = "renderAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/gl/program/Program;use()V"
            )
    )
    public void useCompute(Program instance, Operation<Void> original) {
        IrisUtil.bindBuffers(pipeline, instance.getProgramId());
        original.call(instance);
    }
}
