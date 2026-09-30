// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.tree;

import com.optica.core.rendering.world.allocator.VoxelEntryMemory;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Nullable;

public interface VoxelTreeEntry {
    int VOXEL_DEPTH = 0;
    int BLOCK_DEPTH = 1;
    int BLOCK_CONTAINER_DEPTH = 2;
    int CHUNK_DEPTH = 3;
    int CHUNK_CONTAINER_DEPTH = 4;

    int depth();

    default @Nullable VoxelTreeEntry removeRegions(IntSet regions) {
        return this;
    }

    default VoxelTreeEntry toMutable() {
        return this;
    }

    default VoxelTreeEntry toImmutable() {
        return this;
    }

    void uploadTo(VoxelEntryMemory memory);
}
