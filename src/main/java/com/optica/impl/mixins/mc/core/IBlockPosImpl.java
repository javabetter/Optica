// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.core;

import com.optica.api.mc.core.IBlockPos;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = IBlockPos.class)
public interface IBlockPosImpl {
    @Overwrite
    static IBlockPos zero() {
        return (IBlockPos) new BlockPos(0, 0, 0);
    }

    @Overwrite
    static IBlockPos of(int x, int y, int z) {
        return (IBlockPos) new BlockPos(x, y, z);
    }
}
