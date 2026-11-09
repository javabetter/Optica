// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.blaze3d.opengl.buffer;

import com.optica.api.gpu.buffers.IGpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;

// 26.2: buffer mapping moved to GpuBuffer/GpuBufferSlice.map(), which return this record.
@Mixin(GpuBufferSlice.MappedView.class)
public abstract class GlMappedViewMixin implements IGpuBuffer.MappedView {
    @Shadow
    public abstract ByteBuffer data();

    @Override
    public ByteBuffer ph$data() {
        return data();
    }
}
