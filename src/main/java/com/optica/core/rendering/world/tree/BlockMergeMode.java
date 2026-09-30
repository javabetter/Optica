// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.tree;

import com.optica.core.rendering.world.allocator.VoxelEntryMemory;
import com.optica.core.rendering.world.block.BlockEntry;
import com.optica.core.rendering.world.tree.entries.PriorityBlockEntry;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.PriorityQueue;

public enum  BlockMergeMode {
    OVERWRITE {
        @Override
        public BlockEntry merge(@Nullable BlockEntry oldEntry, BlockEntry newEntry) {
            if (oldEntry == null) return newEntry;

            PriorityBlockEntry blockEntry = oldEntry instanceof PriorityBlockEntry po ? po : new PriorityBlockEntry(oldEntry);
            blockEntry.add(newEntry);

            return blockEntry;
        }
    },
    COMBINE {
        @Override
        public BlockEntry merge(@Nullable BlockEntry oldEntry, BlockEntry newEntry) {
            return oldEntry == null ? newEntry : oldEntry.merge(newEntry);
        }
    };

    public abstract BlockEntry merge(@Nullable BlockEntry oldEntry, BlockEntry newEntry);
}
