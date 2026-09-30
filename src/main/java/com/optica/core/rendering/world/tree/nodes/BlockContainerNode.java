// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.tree.nodes;
import com.optica.core.rendering.world.WorldManager;
import com.optica.core.rendering.world.allocator.VoxelEntryListMemory;
import com.optica.core.rendering.world.allocator.WorldAllocator;
import com.optica.core.rendering.world.block.BlockEntry;
import com.optica.core.rendering.world.tree.BlockMergeMode;
import com.optica.core.rendering.world.tree.VoxelTreeEntry;
import org.joml.Vector3i;

public class BlockContainerNode extends WorldNode {
    BlockContainerNode(
            WorldManager worldManager,
            WorldAllocator allocator,
            BlockMergeMode mergeMode,
            Vector3i pos
    ) {
        super(worldManager, allocator, mergeMode, BLOCK_CONTAINER_DEPTH, pos);
    }

    @Override
    protected VoxelEntryListMemory allocate(WorldAllocator allocator) {
        return allocator.allocateEntryList(true, 2);
    }

    @Override
    public void insertEntry(int x, int y, int z, VoxelTreeEntry entry) {
        if (!(entry instanceof BlockEntry blockEntry))
            throw new IllegalArgumentException("only BlockEntry can be inserted to BlockContainerNode");

        insertRegions(entry);

        int index = indexOf(x, y, z, magnitude());

        final BlockEntry oldEntry = (BlockEntry) getEntry(index);
        final BlockEntry newEntry = oldEntry == null ? blockEntry : mergeMode.merge(oldEntry, blockEntry);

        var ignored = replaceEntry(index, newEntry);
    }
}
