// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.tree.nodes;

import com.optica.core.rendering.world.WorldManager;
import com.optica.core.rendering.world.allocator.WorldAllocator;
import com.optica.core.rendering.world.tree.BlockMergeMode;
import org.joml.Vector3i;

public class ChunkNode extends WorldNode {
    ChunkNode(
            WorldManager worldManager,
            WorldAllocator allocator,
            BlockMergeMode mergeMode,
            Vector3i pos
    ) {
        super(worldManager, allocator, mergeMode, CHUNK_DEPTH, pos);

        worldManager.addChunk(this);
    }

    public void removeFromTree() {
        var parent = this.parent;
        if (parent == null) return;

        ((ChunkContainerNode) parent).removeAllChunks();
    }

    @Override
    public void close() {
        worldManager.removeChunk(this);

        super.close();
    }
}
