// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.world.level;

import com.optica.api.mc.world.level.IBlockState;
import com.optica.api.mc.world.level.chunk.IChunkSection;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionMixin implements IChunkSection {
    @Shadow
    public abstract BlockState getBlockState(int x, int y, int z);

    @Shadow
    public abstract boolean hasOnlyAir();

    @Override
    public IBlockState ph$getBlockState(int x, int y, int z) {
        return (IBlockState) getBlockState(x, y, z);
    }

    @Override
    public boolean ph$hasOnlyAir() {
        return hasOnlyAir();
    }

    @Override
    public IChunkSection ph$createCopy() {
        return (IChunkSection) LevelChunkSectionInvoker.copySection((LevelChunkSection) (Object) this);
    }
}
