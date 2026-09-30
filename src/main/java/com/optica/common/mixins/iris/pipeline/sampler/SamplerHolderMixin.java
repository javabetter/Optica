// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris.pipeline.sampler;

import com.optica.api.gpu.textures.IGpuTexture;
import com.optica.common.iris.IrisUtil;
import com.optica.core.iris.pipeline.texture.ISamplerHolder;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

@Mixin(SamplerHolder.class)
public interface SamplerHolderMixin extends SamplerHolder, ISamplerHolder {
    @Override
    default void addSampler(
            String name,
            Supplier<IGpuTexture.WithSampler<?>> textureAndSampler
    ) {
        addDynamicSampler(
                IrisUtil.getTextureType(textureAndSampler.get().texture()),
                () -> IrisUtil.getTextureHandle(textureAndSampler.get().texture()),
                () -> IrisUtil.getGlSampler(textureAndSampler.get().sampler()),
                name
        );
    }

    @Override
    default void addDefaultSampler(String name, Supplier<IGpuTexture<?>> texture) {
        addDynamicSampler(
                IrisUtil.getTextureType(texture.get()),
                () -> IrisUtil.getTextureHandle(texture.get()),
                null,
                name
        );
    }
}
