// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.meshing;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.optica.common.meshing.impl.BlockBuilderBufferSource;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Optica (26.2): block entities are voxelized by preparing a frame on Optica's own dispatcher, off the
 * render thread. Its geometry goes to the block builder (RenderTypeFeatureRendererMixin), so there is
 * nothing to upload, and the upload is a GPU call that must not run on a chunk compiler thread.
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
}
