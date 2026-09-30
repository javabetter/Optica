// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.blaze3d.common.systems;

import com.optica.api.gpu.buffers.IGpuBuffer;
import com.optica.api.gpu.buffers.IGpuBufferSlice;
import com.optica.api.gpu.systems.ICommandEncoder;
import com.optica.api.gpu.textures.IGpuTexture;
import com.optica.api.gpu.textures.IGpuTexture2D;
import com.optica.api.gpu.textures.IGpuTexture3D;
import com.optica.impl.mc.blaze3d.opengl.textures.IGlTexture;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.CommandEncoder;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.Vector2ic;
import org.joml.Vector3ic;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL44.glClearTexImage;

// 26.1 made GlCommandEncoder package-private behind the public CommandEncoder wrapper, so the Photonics
// command encoder API is implemented on the wrapper and delegates to its (validating) public methods.
@Mixin(CommandEncoder.class)
public abstract class CommandEncoderMixin implements ICommandEncoder {
    @Shadow
    public abstract void writeToBuffer(GpuBufferSlice destination, ByteBuffer data);

    @Shadow
    public abstract GpuBuffer.MappedView mapBuffer(GpuBuffer buffer, boolean read, boolean write);

    @Shadow
    public abstract GpuBuffer.MappedView mapBuffer(GpuBufferSlice slice, boolean read, boolean write);

    @Shadow
    public abstract void copyToBuffer(GpuBufferSlice source, GpuBufferSlice target);

    @Override
    public void ph$clearColorTexture(IGpuTexture<?> gpuTexture, Vector4fc clearColor) {
        // Photonics cleared through GlCommandEncoder's private draw FBO, which is no longer reachable.
        // glClearTexImage (GL 4.4) clears the texture directly; Photonics already requires GL 4.5.
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glClearTexImage(
                    ((IGlTexture) gpuTexture).handle(),
                    0,
                    GL_RGBA,
                    GL_FLOAT,
                    stack.floats(clearColor.x(), clearColor.y(), clearColor.z(), clearColor.w())
            );
        }
    }

    @Override
    public void ph$writeToBuffer(IGpuBuffer buffer, ByteBuffer byteBuffer) {
        writeToBuffer(((GpuBuffer) buffer).slice(), byteBuffer);
    }

    @Override
    public void ph$writeToBuffer(IGpuBufferSlice slice, ByteBuffer byteBuffer) {
        writeToBuffer((GpuBufferSlice) (Object) slice, byteBuffer);
    }

    @Override
    public IGpuBuffer.MappedView ph$mapBuffer(IGpuBuffer buffer, boolean readable, boolean writeable) {
        return (IGpuBuffer.MappedView) mapBuffer((GpuBuffer) buffer, readable, writeable);
    }

    @Override
    public IGpuBuffer.MappedView ph$mapBuffer(IGpuBufferSlice bufferSlice, boolean readable, boolean writeable) {
        return (IGpuBuffer.MappedView) mapBuffer((GpuBufferSlice) (Object) bufferSlice, readable, writeable);
    }

    @Override
    public void ph$copyToBuffer(IGpuBufferSlice slice1, IGpuBufferSlice slice2) {
        copyToBuffer((GpuBufferSlice) (Object) slice1, (GpuBufferSlice) (Object) slice2);
    }

    @Override
    public void ph$writeToTexture(
            IGpuTexture2D texture,
            ByteBuffer data,
            Vector2ic offset,
            Vector2ic size
    ) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void ph$writeToTexture(IGpuTexture3D texture, ByteBuffer data, Vector3ic offset, Vector3ic size) {
        throw new NotImplementedException("TODO");
    }
}
