// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.mc.world.level;

import com.optica.api.mc.core.IBlockPos;
import com.optica.api.mc.world.level.chunk.IChunkAccess;
import org.jetbrains.annotations.Nullable;

public interface ILevel extends ILevelReader {
    @Nullable IChunkAccess ph$getChunkOrNull(int x, int y);

    int ph$getSkylightValue(IBlockPos pos);
}
