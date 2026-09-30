// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.registry.light;

import com.optica.core.config.lights.BlockLightInfo;
import com.optica.core.rendering.world.allocator.WorldAllocator;
import com.optica.core.rendering.world.allocator.WorldLightMemory;
import com.optica.core.rendering.world.registry.object.AbstractWorldObject;

public class WorldLight extends AbstractWorldObject<WorldLightMemory> {
    private final BlockLightInfo light;
    private final int blockId;

    public WorldLight(
            WorldLightRegistry registry,
            BlockLightInfo light,
            int blockId
    ) {
        super(registry);

        this.light = light;
        this.blockId = blockId;
    }

    public int entryData() {
        return memoryOrThrow().entryData();
    }

    public void allocate(WorldAllocator allocator) {
        var memory = setMemory(allocator::allocateWorldLight);

        memory.setLight(light, blockId);
        memory.upload();
    }

    public BlockLightInfo lightInfo() {
        return light;
    }

    public int blockId() {
        return blockId;
    }

    @Override
    public int hashCode() {
        return blockId * 31 + light.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WorldLight other
                && other.blockId == blockId
                && other.light.equals(light);
    }
}
