// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.mc.world.level;

import com.optica.api.mc.core.IBlockPos;
import com.optica.api.mc.world.level.block.IBlockEntity;
import org.jetbrains.annotations.Nullable;

public interface IBlockGetter extends ILevelHeightAccessor {
    IBlockState ph$getBlockState(IBlockPos pos);

    @Nullable
    IBlockEntity ph$getBlockEntity(IBlockPos pos);
}
