// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.world.level;

import com.optica.api.mc.core.IRegistryAccess;
import com.optica.api.mc.world.level.ILevelReader;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelReader.class)
public interface LevelReaderMixin extends ILevelReader {
    @Shadow
    RegistryAccess registryAccess();

    @Override
    default IRegistryAccess ph$registryAccess() {
        return (IRegistryAccess) registryAccess();
    }
}
