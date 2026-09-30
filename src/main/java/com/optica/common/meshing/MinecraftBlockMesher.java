// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.meshing;

import com.optica.api.mc.core.IBlockPos;
import com.optica.api.mc.world.level.IBlockAndTintGetter;
import com.optica.api.mc.world.level.IBlockState;
import com.optica.core.rendering.world.bakery.BlockBuilder;
import com.optica.core.rendering.world.bakery.BlockMesher;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3i;

public class MinecraftBlockMesher implements BlockMesher<McMeshState> {
    private static final ThreadLocal<McBlockRenderer> RENDERERS = ThreadLocal.withInitial(McBlockRenderer::new);

    @Override
    public void setup() {
        BlockModelLighter.enableCaching();
    }

    @Override
    public void teardown() {
        BlockModelLighter.clearCache();
    }

    @Override
    public McMeshState extractMeshState(
            Vector3i blockChunkOffset,
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter
    ) {
        return RENDERERS.get().extractMeshState(
                blockChunkOffset,
                (BlockPos) pos,
                (BlockState) blockState,
                (BlockAndTintGetter) blockAndTintGetter
        );
    }

    @Override
    public void meshBlock(
            McMeshState meshState,
            Vector3i blockChunkOffset,
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter,
            BlockBuilder blockBuilder
    ) {
        RENDERERS.get().meshBlock(
                meshState,
                blockChunkOffset,
                (BlockPos) pos,
                (BlockState) blockState,
                (BlockAndTintGetter) blockAndTintGetter,
                blockBuilder
        );
    }
}
