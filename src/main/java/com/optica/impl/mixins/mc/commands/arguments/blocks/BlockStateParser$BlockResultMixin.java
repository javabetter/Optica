// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.commands.arguments.blocks;

import com.optica.api.mc.IProperty;
import com.optica.api.mc.commands.arguments.blocks.IBlockStateParser;
import com.optica.api.mc.nbt.ICompoundTag;
import com.optica.api.mc.world.level.IBlockState;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(BlockStateParser.BlockResult.class)
public abstract class BlockStateParser$BlockResultMixin implements IBlockStateParser.BlockResult{
    @Shadow
    public abstract BlockState blockState();

    @Shadow
    public abstract Map<Property<?>, Comparable<?>> properties();

    @Shadow
    public abstract @Nullable CompoundTag nbt();

    @Override
    public IBlockState ph$blockState() {
        return (IBlockState) blockState();
    }

    @Override
    public Map<IProperty<?>, Comparable<?>> ph$properties() {
        return (Map) properties();
    }

    @Override
    public ICompoundTag ph$nbt() {
        return (ICompoundTag) (Object) nbt();
    }
}
