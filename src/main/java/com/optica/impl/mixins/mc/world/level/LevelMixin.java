// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.world.level;

import com.optica.api.mc.core.IBlockPos;
import com.optica.api.mc.world.level.ILevel;
import com.optica.api.mc.world.level.chunk.IChunkAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Level.class)
public abstract class LevelMixin implements ILevel {
    @Shadow
    public abstract @Nullable ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl);

    @Override
    public @Nullable IChunkAccess ph$getChunkOrNull(int x, int y) {
        return (IChunkAccess) getChunk(x, y, ChunkStatus.FULL, false);
    }

    @Shadow
    public abstract LevelLightEngine getLightEngine();

    @Override
    public int ph$getSkylightValue(IBlockPos pos) {
        var lightEngine = getLightEngine();
        var layerListener = lightEngine.getLayerListener(LightLayer.SKY);

        return layerListener.getLightValue((BlockPos) pos);
    }
}
