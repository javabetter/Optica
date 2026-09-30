// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.registry.block;

import com.optica.core.rendering.world.allocator.WorldAllocator;
import com.optica.core.rendering.world.registry.block.builder.VoxelLayerBuilder;
import com.optica.core.rendering.world.registry.object.ObjectRegistry;
import com.optica.core.rendering.world.registry.object.WeakValue;
import com.optica.core.rendering.world.registry.palete.PaletteRegistry;
import com.optica.core.rendering.world.tree.VoxelTreeEntry;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReadWriteLock;

public class BlockRegistry extends ObjectRegistry<BlockObject> {
    private final WorldAllocator allocator;
    private final PaletteRegistry paletteRegistry;

    public BlockRegistry(
            ReadWriteLock lock,
            WorldAllocator allocator,
            PaletteRegistry paletteRegistry
    ) {
        super(lock);

        this.allocator = allocator;
        this.paletteRegistry = paletteRegistry;
    }

    public @WeakValue VoxelLayer allocateVoxelLayer(VoxelLayerBuilder entries) {
        return (VoxelLayer) cacheObject(
                new VoxelLayer(entries, paletteRegistry, this),
                (e) -> e.allocate(allocator)
        );
    }

    public @WeakValue BlockLayer allocateBlockLayer(
            @Nullable VoxelLayer[] entries,
            int size,
            long hash,
            int boundingVolume
    ) {
        return (BlockLayer) cacheObject(
                new BlockLayer(entries, size, hash, boundingVolume, this),
                (e) -> e.allocate(allocator)
        );
    }
}
