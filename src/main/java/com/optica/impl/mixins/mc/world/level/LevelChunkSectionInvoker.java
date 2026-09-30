// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.world.level;

import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelChunkSection.class)
public interface LevelChunkSectionInvoker {
    @Invoker("<init>")
    static LevelChunkSection copySection(LevelChunkSection toCopy) {
        throw new AssertionError();
    }
}
