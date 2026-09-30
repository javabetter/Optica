// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.bakery;

import com.optica.core.rendering.world.block.TextureData;

public interface VoxelConsumer {
    void acceptVoxel(
            int x, int y, int z,
            int normal,
            TextureData textureData
    );
}
