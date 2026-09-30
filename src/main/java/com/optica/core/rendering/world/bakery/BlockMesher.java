// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.bakery;

import com.optica.api.mc.Id;
import com.optica.api.mc.core.IBlockPos;
import com.optica.api.mc.world.level.IBlock;
import com.optica.api.mc.world.level.IBlockAndTintGetter;
import com.optica.api.mc.world.level.IBlockState;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.joml.Vector3i;

import java.util.Optional;

public interface BlockMesher<T extends BlockMeshState> {
    Registry REGISTRY = new Registry();

    default void setup() {

    }

    T extractMeshState(
            Vector3i blockChunkOffset,
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter
    );

    void meshBlock(
            T meshState,
            Vector3i blockChunkOffset,
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter,
            BlockBuilder blockBuilder
    );

    default void teardown() {

    }

    class Registry {
        private final Object2ObjectMap<Id, BlockMesher<?>> blockRegistry = new Object2ObjectOpenHashMap<>();
        private final Object2ObjectMap<String, BlockMesher<?>> namespaceRegistry = new Object2ObjectOpenHashMap<>();
        private BlockMesher<?> defaultMesher = null;

        Registry() {

        }

        public void setup() {
            for (var e : blockRegistry.values())
                e.setup();

            for (var e : namespaceRegistry.values())
                e.setup();

            if (defaultMesher != null)
                defaultMesher.setup();
        }

        public void teardown() {
            for (var e : blockRegistry.values())
                e.teardown();

            for (var e : namespaceRegistry.values())
                e.teardown();

            if (defaultMesher != null)
                defaultMesher.teardown();
        }

        public void addBlock(Id id, BlockMesher<?> mesher) {
            blockRegistry.putIfAbsent(id, mesher);
        }

        public void addNamespace(String namespace, BlockMesher<?> mesher) {
            namespaceRegistry.putIfAbsent(namespace, mesher);
        }

        public void addDefault(BlockMesher<?> mesher) {
            defaultMesher = mesher;
        }

        public Optional<BlockMesher<?>> get(IBlock block) {
            return Optional.<BlockMesher<?>>ofNullable(blockRegistry.get(block.ph$id()))
                    .or(() -> Optional.ofNullable(namespaceRegistry.get(block.ph$id().ph$namespace())))
                    .or(() -> Optional.ofNullable(defaultMesher));
        }
    }
}
