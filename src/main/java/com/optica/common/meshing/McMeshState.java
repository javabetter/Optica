// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.meshing;

import com.optica.core.rendering.world.bakery.BlockMeshState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.world.level.material.FluidState;

import java.util.List;

public interface McMeshState extends BlockMeshState {
    int blockId();

    FluidState fluidState();

    List<BlockStateModelPart> blockModel();
}
