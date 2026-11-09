// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.meshing;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.optica.common.meshing.impl.BlockBuilderBufferSource;
import com.optica.common.meshing.impl.EmptyVertexConsumer;
import net.minecraft.client.renderer.feature.FlameFeatureRenderer;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.ShadowFeatureRenderer;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Optica (26.2): while Optica voxelizes a block entity on this thread, every feature renderer's
 * geometry goes into the block builder instead of the frame's staged vertex buffer. Shadows, flames,
 * name tags and text are dropped, as Photonics did.
 */
@Mixin(RenderTypeFeatureRenderer.class)
public abstract class RenderTypeFeatureRendererMixin {
    @Inject(method = "getVertexBuilder", at = @At("HEAD"), cancellable = true)
    private void optica$captureBlockEntity(RenderType renderType, CallbackInfoReturnable<VertexConsumer> cir) {
        var capture = BlockBuilderBufferSource.active();
        if (capture == null) return;

        Object self = this;
        if (self instanceof ShadowFeatureRenderer || self instanceof FlameFeatureRenderer
                || self instanceof NameTagFeatureRenderer || self instanceof TextFeatureRenderer) {
            cir.setReturnValue(EmptyVertexConsumer.INSTANCE);
            return;
        }

        cir.setReturnValue(capture.getBuffer(renderType));
    }
}
