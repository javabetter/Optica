// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins;

import com.optica.api.ModLoader;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.nio.file.Path;

@Mixin(value = ModLoader.class)
public interface IModLoaderImpl {
    @Overwrite
    static Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Overwrite
    static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
