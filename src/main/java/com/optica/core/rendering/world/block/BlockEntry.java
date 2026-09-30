// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.block;

import com.optica.api.Disposable;
import com.optica.core.rendering.world.tree.VoxelTreeEntry;
import it.unimi.dsi.fastutil.ints.IntSet;

public interface BlockEntry extends VoxelTreeEntry, Disposable {
    @Override
    default int depth() {
        return BLOCK_DEPTH;
    }

    int boundingVolume();

    IntSet regions();

    BlockEntry merge(BlockEntry entry);
}
