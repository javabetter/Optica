// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.meshing;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.world.level.material.FluidState;

import java.util.List;

public record DynamicMeshState(
        int blockId,
        FluidState fluidState,
        List<BlockStateModelPart> blockModel
) implements McMeshState {
    @Override
    public boolean shouldCache() {
        return false;
    }

    @Override
    public void prepareCacheUse() {

    }
}
