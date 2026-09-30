// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.blaze3d.common.systems;

import com.optica.api.gpu.buffers.IGpuBuffer;
import com.optica.api.gpu.buffers.heap.IGpuBufferHeap;
import com.optica.api.gpu.systems.ICommandEncoder;
import com.optica.api.gpu.systems.IGpuDevice;
import com.optica.api.gpu.textures.IAddressMode;
import com.optica.api.gpu.textures.IFilterMode;
import com.optica.api.gpu.textures.IGpuSampler;
import com.optica.api.gpu.textures.IGpuTexture;
import com.optica.api.gpu.textures.IGpuTexture2D;
import com.optica.api.gpu.textures.IGpuTexture3D;
import com.optica.api.gpu.textures.ITextureFormat;
import com.optica.impl.mc.blaze3d.common.GpuDeviceImpl;
import com.optica.impl.mc.blaze3d.common.TextureFormats;
import com.optica.impl.mc.blaze3d.opengl.GlTextureFormats;
import com.optica.impl.mc.blaze3d.opengl.buffer.GlBufferHeap;
import com.optica.impl.mc.blaze3d.opengl.textures.GlTexture2D;
import com.optica.impl.mc.blaze3d.opengl.textures.GlTexture3D;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.textures.GpuSampler;

import java.util.OptionalDouble;
import java.util.function.Supplier;

// 26.1 made GlDevice package-private behind the public GpuDevice wrapper (which RenderSystem.getDevice()
// now returns), so the Photonics device API is implemented on the wrapper instead.
@Mixin(GpuDevice.class)
public abstract class GpuDeviceMixin implements GpuDeviceImpl, IGpuDevice {
    @Shadow
    public abstract CommandEncoder createCommandEncoder();

    @Shadow
    public abstract GpuSampler createSampler(
            AddressMode addressModeU,
            AddressMode addressModeV,
            FilterMode minFilter,
            FilterMode magFilter,
            int maxAnisotropy,
            OptionalDouble maxLod
    );

    @Shadow
    public abstract GpuBuffer createBuffer(@Nullable Supplier<String> label, int usage, long size);

    @Override
    public ICommandEncoder ph$createCommandEncoder() {
        return (ICommandEncoder) createCommandEncoder();
    }

    @Override
    public IGpuSampler ph$createSampler(
            IAddressMode addressModeU,
            IAddressMode addressModeV,
            IFilterMode minFilter,
            IFilterMode magFilter,
            int maxAnisotropy,
            OptionalDouble maxLod
    ) {
        return (IGpuSampler) createSampler(
                (AddressMode) (Object) addressModeU,
                (AddressMode) (Object) addressModeV,
                (FilterMode) (Object) minFilter,
                (FilterMode) (Object) magFilter,
                maxAnisotropy,
                maxLod
        );
    }

    @Override
    public IGpuTexture2D ph$createTexture2D(
            @Nullable Supplier<String> label,
            int usage,
            ITextureFormat textureFormat,
            int width, int height,
            int mipLevels
    ) {
        //TODO: Verify arguments
        return new GlTexture2D(label, usage, textureFormat, new Vector2i(width, height), mipLevels);
    }

    @Override
    public IGpuTexture3D ph$createTexture3D(
            @Nullable Supplier<String> label,
            int usage, ITextureFormat textureFormat,
            int width, int height, int depth,
            int mipLevels
    ) {
        //TODO: Verify arguments
        return new GlTexture3D(label, usage, textureFormat, new Vector3i(width, height, depth), mipLevels);
    }

    @Override
    public IGpuBuffer ph$createBuffer(@Nullable Supplier<String> label, long byteSize, int usage) {
        return (IGpuBuffer) createBuffer(label, usage, byteSize);
    }

    @Override
    public IGpuBufferHeap ph$createBufferHeap(@Nullable Supplier<String> label, long byteSize, int usage) {
        return new GlBufferHeap(this, label, byteSize, usage);
    }

    @Override
    public TextureFormats getTextureFormats() {
        return GlTextureFormats.INSTANCE;
    }
}
