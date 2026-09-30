// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.registry.light;

import com.optica.api.mc.world.level.IBlockState;
import com.optica.core.iris.IrisPack;
import com.optica.core.config.PhConfig;
import com.optica.core.config.lights.BlockLightInfo;
import com.optica.core.rendering.world.allocator.WorldAllocator;
import com.optica.core.rendering.world.registry.object.ObjectRegistry;
import com.optica.core.rendering.world.registry.object.WeakValue;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReadWriteLock;

public class WorldLightRegistry extends ObjectRegistry<WorldLight> {
    private final WorldAllocator allocator;

    public WorldLightRegistry(
            ReadWriteLock lock,
            WorldAllocator allocator
    ) {
        super(lock);
        this.allocator = allocator;
    }

    public @WeakValue WorldLight allocate(BlockLightInfo lightInfo, int blockId) {
        return cacheObject(
                new WorldLight(this, lightInfo, blockId),
                e -> e.allocate(allocator)
        );
    }

    public @Nullable @WeakValue WorldLight getWeak(IBlockState blockState) {
        var light = PhConfig.getLightRegistry().get(blockState);
        if (light == null) return null;

        var shaderPack = IrisPack.getCurrentPack().orElse(null);
        if (shaderPack == null) return null;

        return allocate(light, shaderPack.ph$getBlockId(blockState));
    }
}
