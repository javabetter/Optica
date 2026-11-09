// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.meshing;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.optica.common.meshing.impl.BlockBuilderBufferSource;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.FeatureRenderer;
import net.minecraft.client.renderer.feature.QuadParticleFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Optica (26.2): block entities are voxelized by preparing a frame on Optica's own dispatcher, off the
 * render thread. Its geometry goes to the block builder (RenderTypeFeatureRendererMixin), so there is
 * nothing to upload, and the upload is a GPU call that must not run on a chunk compiler thread. The
 * particle renderer's prepare hooks read render-thread state even without particles, so they are
 * skipped too (block entities submit no particles).
 */
@Mixin(FeatureRenderDispatcher.class)
public abstract class FeatureRenderDispatcherMixin {
    @WrapOperation(
            method = "prepareFrameWithContext",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/StagedVertexBuffer;upload()V")
    )
    private void optica$skipUploadWhileCapturing(StagedVertexBuffer instance, Operation<Void> original) {
        if (BlockBuilderBufferSource.active() != null) return;

        original.call(instance);
    }

    @WrapOperation(
            method = "prepareFrameWithContext",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderer;beginPrepare(Lnet/minecraft/client/renderer/feature/FeatureFrameContext;)V")
    )
    private void optica$skipParticleBeginPrepare(FeatureRenderer<?> instance, FeatureFrameContext context, Operation<Void> original) {
        if (BlockBuilderBufferSource.active() != null && instance instanceof QuadParticleFeatureRenderer) return;

        original.call(instance, context);
    }

    @WrapOperation(
            method = "prepareFrameWithContext",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderer;finishPrepare(Lnet/minecraft/client/renderer/feature/FeatureFrameContext;)V")
    )
    private void optica$skipParticleFinishPrepare(FeatureRenderer<?> instance, FeatureFrameContext context, Operation<Void> original) {
        if (BlockBuilderBufferSource.active() != null && instance instanceof QuadParticleFeatureRenderer) return;

        original.call(instance, context);
    }
}
