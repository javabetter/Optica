// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.meshing.impl;

import com.optica.api.mc.Id;
import com.optica.common.mixins.meshing.OuterWrappedRenderTypeAccessor;
import com.optica.common.mixins.meshing.RenderSetupAccessor;
import com.optica.common.mixins.meshing.RenderTypeAccessor;
import com.optica.core.rendering.world.bakery.BlockBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.Nullable;

/**
 * Routes the geometry of block entities (chests, signs, beds, ...) into the voxel block builder, one
 * render type at a time, using the render type's texture as the atlas.
 *
 * <p>Optica (26.2): MultiBufferSource is gone; feature renderers take their vertex consumers from
 * RenderTypeFeatureRenderer.getVertexBuilder, which RenderTypeFeatureRendererMixin sends here while a
 * capture is active on the current thread (block meshing runs on the chunk compiler threads).
 */
public class BlockBuilderBufferSource {
    private static final ThreadLocal<BlockBuilderBufferSource> ACTIVE = new ThreadLocal<>();

    private BlockBuilder blockBuilder;

    public static @Nullable BlockBuilderBufferSource active() {
        return ACTIVE.get();
    }

    /** Captures the block entity geometry submitted on this thread until {@link #end()}. */
    public void begin(BlockBuilder blockBuilder) {
        this.blockBuilder = blockBuilder;
        ACTIVE.set(this);
    }

    public void end() {
        ACTIVE.remove();
        this.blockBuilder = null;
    }

    public VertexConsumer getBuffer(RenderType renderType) {
        if (renderType instanceof OuterWrappedRenderType wrapped)
            renderType = ((OuterWrappedRenderTypeAccessor) wrapped).getWrapped();

        RenderSetup renderSetup = ((RenderTypeAccessor) renderType).getState();
        var textures = ((RenderSetupAccessor) (Object) renderSetup).getTextureSetup();
        var sampler0 = textures.get("Sampler0");

        if (sampler0 == null) return EmptyVertexConsumer.INSTANCE;

        blockBuilder.useAtlas((Id) (Object) sampler0.getLocation());

        return (VertexConsumer) blockBuilder;
    }
}
