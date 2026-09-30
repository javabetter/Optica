// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.mc.world.level.chunk;

import com.optica.api.mc.world.level.IBlockState;
import org.joml.Vector3ic;

public interface IChunkSection {
    int SECTION_WIDTH = 16;
    int SECTION_HEIGHT = 16;
    int SECTION_SIZE = 4096;

    IBlockState ph$getBlockState(int x, int y, int z);

    default IBlockState ph$getBlockState(Vector3ic pos) {
        return ph$getBlockState(pos.x(), pos.y(), pos.z());
    }

    boolean ph$hasOnlyAir();

    IChunkSection ph$createCopy();
}
