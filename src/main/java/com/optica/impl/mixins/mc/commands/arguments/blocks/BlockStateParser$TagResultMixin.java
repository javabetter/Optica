// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.commands.arguments.blocks;

import com.optica.api.mc.commands.arguments.blocks.IBlockStateParser;
import com.optica.api.mc.core.IHolderSet;
import com.optica.api.mc.nbt.ICompoundTag;
import com.optica.api.mc.world.level.IBlock;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@SuppressWarnings("unchecked")
@Mixin(BlockStateParser.TagResult.class)
public abstract class BlockStateParser$TagResultMixin implements IBlockStateParser.TagResult  {
    @Shadow
    public abstract HolderSet<Block> tag();

    @Shadow
    public abstract Map<String, String> vagueProperties();

    @Shadow
    public abstract @Nullable CompoundTag nbt();

    @Override
    public IHolderSet<IBlock> ph$tag() {
        return (IHolderSet) tag();
    }

    @Override
    public Map<String, String> ph$vagueProperties() {
        return vagueProperties();
    }

    @Override
    public @Nullable ICompoundTag ph$nbt() {
        return (ICompoundTag) (Object) nbt();
    }
}
