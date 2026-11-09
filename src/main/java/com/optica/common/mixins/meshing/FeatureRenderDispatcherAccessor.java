// Copyright Optica contributors, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.meshing;

import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Optica (26.2): gives Optica's own dispatcher its own staged vertex buffer (see McBlockRenderer). */
@Mixin(FeatureRenderDispatcher.class)
public interface FeatureRenderDispatcherAccessor {
    @Mutable
    @Accessor("stagedVertexBuffer")
    void optica$setStagedVertexBuffer(StagedVertexBuffer buffer);
}
