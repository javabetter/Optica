// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.registry.block.builder;

import com.optica.core.rendering.world.allocator.VoxelEntryMemory;
import com.optica.core.rendering.world.block.TextureData;
import com.optica.core.rendering.world.tree.VoxelTreeEntry;

public class VoxelData implements VoxelTreeEntry {
    public int normal;
    public TextureData textureData;

    @Override
    public int depth() {
        return -1;
    }

    @Override
    public void uploadTo(VoxelEntryMemory memory) {
        throw new UnsupportedOperationException("uploadTo");
    }
}
