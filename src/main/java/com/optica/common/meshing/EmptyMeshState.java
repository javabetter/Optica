// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.meshing;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class EmptyMeshState implements McMeshState {
    public static final EmptyMeshState INSTANCE = new EmptyMeshState();

    private EmptyMeshState() {

    }

    @Override
    public int blockId() {
        return -1;
    }

    @Override
    public FluidState fluidState() {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public List<BlockStateModelPart> blockModel() {
        return List.of();
    }

    @Override
    public boolean shouldCache() {
        return true;
    }

    @Override
    public void prepareCacheUse() {

    }
}
