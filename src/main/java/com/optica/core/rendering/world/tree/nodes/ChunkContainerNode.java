// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.tree.nodes;

import com.optica.core.rendering.world.WorldManager;
import com.optica.core.rendering.world.allocator.WorldAllocator;
import com.optica.core.rendering.world.tree.BlockMergeMode;
import org.joml.Vector3i;

public class ChunkContainerNode extends WorldNode {
    ChunkContainerNode(
            WorldManager worldManager,
            WorldAllocator allocator,
            BlockMergeMode mergeMode,
            Vector3i pos
    ) {
        super(worldManager, allocator, mergeMode, CHUNK_CONTAINER_DEPTH, pos);
    }

    public void removeAllChunks() {
        if (isEmpty()) return;

        for (int i = 0; i < ENTRIES_SIZE; i++) {
            var entry = replaceEntry(i, null);

            if (entry != null)
                ((ChunkNode) entry).parent = null;
        }
    }
}
