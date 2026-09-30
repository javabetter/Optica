// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.world.level;

import com.optica.api.mc.Id;
import com.optica.api.mc.world.level.IBlock;
import com.optica.api.mc.world.level.IBlockState;
import com.optica.api.mc.world.level.block.state.IStateDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Block.class)
@SuppressWarnings("unchecked")
public abstract class BlockMixin implements IBlock {
    @Shadow
    public abstract StateDefinition<Block, BlockState> getStateDefinition();

    @Shadow
    public abstract BlockState defaultBlockState();

    @Override
    public Id ph$id() {
        return (Id) (Object) BuiltInRegistries.BLOCK.getKey((Block) (Object) this);
    }

    @Override
    public IStateDefinition<IBlock, IBlockState> ph$stateDefinition() {
        return (IStateDefinition<IBlock, IBlockState>) getStateDefinition();
    }

    @Override
    public IBlockState ph$defaultBlockState() {
        return (IBlockState) defaultBlockState();
    }
}
