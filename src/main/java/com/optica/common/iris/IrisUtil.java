// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris;

import com.optica.api.gpu.textures.IGpuSampler;
import com.optica.api.gpu.textures.IGpuTexture;
import com.optica.api.gpu.textures.IGpuTexture2D;
import com.optica.api.gpu.textures.IGpuTexture3D;
import com.optica.common.iris.pipeline.IrisRenderingPipelineExt;
import com.optica.common.iris.pipeline.PipelineManagerExt;
import com.optica.common.mixins.iris.pipeline.sampler.GlSamplerAccessor;
import com.optica.core.iris.rendering.PhotonicsPipeline;
import com.optica.impl.mc.blaze3d.opengl.textures.AbstractGlTexture;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class IrisUtil {
    public static PipelineManagerExt getPipelineManager() {
        return (PipelineManagerExt) Iris.getPipelineManager();
    }

    public static int getBlockId(BlockState block) {
        var blockIds = WorldRenderingSettings.INSTANCE.getBlockStateIds();
        return blockIds == null ? -1 : blockIds.getOrDefault(block, -1);
    }

    public static IntSet getUsedBuffers() {
        return IntSet.of();
    }

    public static void bindBuffers(@Nullable WorldRenderingPipeline pipeline, int programId) {
        if (pipeline instanceof IrisRenderingPipeline ext)
            ((IrisRenderingPipelineExt) ext).photonics$bufferHolder().bind(programId, IrisUtil.getUsedBuffers());
    }

    public static void bindBuffers(int programId) {
        bindBuffers(
                Iris.getPipelineManager().getPipelineNullable(),
                programId
        );
    }

    public static TextureType getTextureType(IGpuTexture<?> texture) {
        if (texture instanceof IGpuTexture2D)
            return TextureType.TEXTURE_2D;
        else if (texture instanceof IGpuTexture3D)
            return TextureType.TEXTURE_3D;

        throw new IllegalArgumentException("Unknown texture type " + texture.getClass().getSimpleName());
    }

    public static int getTextureHandle(IGpuTexture<?> texture) {
        return ((AbstractGlTexture<?>) texture).handle();
    }

    public static GlSampler getGlSampler(IGpuSampler sampler) {
        return new GlSampler(((GlSamplerAccessor) sampler).getId());
    }
}
