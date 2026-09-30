// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.blaze3d.opengl.buffer;

import com.optica.api.gpu.buffers.BufferUsage;
import com.optica.impl.mc.blaze3d.opengl.buffer.GlBufferHeap;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

@Mixin(targets = "com.mojang.blaze3d.opengl.BufferStorage$Immutable")
public abstract class ImmutableBufferStorageMixin {
    @Inject(method = "tryMapBufferPersistent", at = @At("HEAD"), cancellable = true)
    private void tryMapBufferPersistent(
            DirectStateAccess directStateAccess,
            @BufferUsage int i,
            int j,
            long l,
            CallbackInfoReturnable<ByteBuffer> cir
    ) {
        if ((i & GlBufferHeap.NO_PERSISTENCE_MAPPING) != 0)
            cir.setReturnValue(null);
    }
}
