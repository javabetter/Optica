// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.registry.block.model;

import com.optica.core.rendering.world.block.BlockEntry;
import com.optica.core.rendering.world.block.BlockModel;
import com.optica.core.rendering.world.registry.block.BlockLayer;
import com.optica.core.rendering.world.registry.object.WeakValue;
import com.optica.core.rendering.world.tree.entries.LightBlockEntry;
import com.optica.core.rendering.world.tree.entries.SimpleBlockEntry;
import com.optica.core.rendering.world.registry.light.WorldLight;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public class BlockPartImpl implements BlockModel.Part {
    private final Vector3i offset;
    private final BlockLayer block;

    BlockModelImpl owner;

    public BlockPartImpl(Vector3i offset, BlockLayer block) {
        this.offset = offset;
        this.block = block;
    }

    @Override
    public Vector3i offset() {
        return offset;
    }

    public BlockLayer blockLayer() {
        return block;
    }

    @Override
    public BlockEntry createEntry(
            int region,
            int skylight,
            @Nullable @WeakValue WorldLight light
    ) {
        return light == null ? new SimpleBlockEntry(region, skylight, this)
                : new LightBlockEntry(region, skylight, light, this);
    }

    @Override
    public void close() {
        owner.close();
    }
}
