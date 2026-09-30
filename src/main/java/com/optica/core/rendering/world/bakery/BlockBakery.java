// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.bakery;

import com.optica.api.Disposable;
import com.optica.api.mc.core.IBlockPos;
import com.optica.api.mc.world.level.IBlockAndTintGetter;
import com.optica.api.mc.world.level.IBlockState;
import com.optica.core.rendering.world.bakery.impl.BlockBakeryImpl;
import com.optica.core.rendering.world.bakery.texture.AtlasDownloader;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public interface BlockBakery {
    @Nullable <T extends BlockMeshState> MeshResult meshBlock(
            BlockMesher<T> mesher,
            T meshState,
            Vector3i blockChunkOffset,
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter
    );

    interface MeshResult extends Disposable {
        long vertexHash();

        void bake(VoxelConsumer voxelConsumer) throws InterruptedException;
    }

    static BlockBakery newBakery(AtlasDownloader atlasDownloader) {
        return new BlockBakeryImpl(atlasDownloader);
    }
}
