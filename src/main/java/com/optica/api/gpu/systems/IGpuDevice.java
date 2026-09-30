// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.gpu.systems;

import com.optica.api.gpu.buffers.BufferUsage;
import com.optica.api.gpu.buffers.IGpuBuffer;
import com.optica.api.gpu.buffers.heap.IGpuBufferHeap;
import com.optica.api.gpu.textures.IAddressMode;
import com.optica.api.gpu.textures.IFilterMode;
import com.optica.api.gpu.textures.IGpuSampler;
import com.optica.api.gpu.textures.IGpuTexture2D;
import com.optica.api.gpu.textures.IGpuTexture3D;
import com.optica.api.gpu.textures.ITextureFormat;
import com.optica.api.gpu.textures.TextureUsage;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.function.Supplier;

public interface IGpuDevice {
    ICommandEncoder ph$createCommandEncoder();

    IGpuSampler ph$createSampler(
        IAddressMode addressModeU,
        IAddressMode addressModeV,
        IFilterMode minFilter,
        IFilterMode magFilter,
        int maxAnisotropy,
        OptionalDouble maxLod
    );

    IGpuTexture2D ph$createTexture2D(
            @Nullable Supplier<String> label,
            @TextureUsage int usage,
            ITextureFormat textureFormat,
            int width, int height,
            int mipLevels
    );

    IGpuTexture3D ph$createTexture3D(
            @Nullable Supplier<String> label,
            @TextureUsage int usage,
            ITextureFormat textureFormat,
            int width, int height, int depth,
            int mipLevels
    );

    IGpuBuffer ph$createBuffer(
            @Nullable Supplier<String> label,
            long byteSize,
            @BufferUsage int usage
    );

    IGpuBufferHeap ph$createBufferHeap(
            @Nullable Supplier<String> label,
            long byteSize,
            @BufferUsage int usage
    );
}
