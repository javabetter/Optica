// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.blaze3d.opengl.buffer;

import com.llamalad7.mixinextras.sugar.Local;
import com.optica.impl.mc.blaze3d.opengl.buffer.GlBufferHeap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Keeps buffers created with {@link GlBufferHeap#NO_PERSISTENCE_MAPPING} (the voxel world heap) from
 * being mapped persistently.
 *
 * <p>Optica (26.2): tryMapBufferPersistent is gone; GlBuffer.Direct maps at construction when its
 * canPersistentMap argument is true, so that argument is cleared for those buffers.
 */
@Mixin(targets = "com.mojang.blaze3d.opengl.BufferStorage$Immutable")
public abstract class ImmutableBufferStorageMixin {
    @ModifyArg(
            method = "createBuffer",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/opengl/GlBuffer$Direct;<init>(Lcom/mojang/blaze3d/opengl/DirectStateAccess;IJIZ)V"
            ),
            index = 4
    )
    private boolean optica$noPersistentMapping(boolean canPersistentMap, @Local(argsOnly = true) int usage) {
        return canPersistentMap && (usage & GlBufferHeap.NO_PERSISTENCE_MAPPING) == 0;
    }
}
