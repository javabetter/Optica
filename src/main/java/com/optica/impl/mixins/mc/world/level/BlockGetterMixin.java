// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.world.level;

import com.optica.api.mc.core.IBlockPos;
import com.optica.api.mc.world.level.IBlockGetter;
import com.optica.api.mc.world.level.IBlockState;
import com.optica.api.mc.world.level.block.IBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockGetter.class)
public interface BlockGetterMixin extends IBlockGetter {
    @Shadow BlockState getBlockState(BlockPos pos);

    @Shadow BlockEntity getBlockEntity(BlockPos pos);

    @Override
    default IBlockState ph$getBlockState(IBlockPos pos) {
        return (IBlockState) getBlockState((BlockPos) pos);
    }

    @Override
    default @Nullable IBlockEntity ph$getBlockEntity(IBlockPos pos) {
        return (IBlockEntity) getBlockEntity((BlockPos) pos);
    }
}
