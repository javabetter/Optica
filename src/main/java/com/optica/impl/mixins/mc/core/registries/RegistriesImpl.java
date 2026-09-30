// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.core.registries;

import com.optica.api.mc.core.IHolderLookup;
import com.optica.api.mc.core.registries.Registries;
import com.optica.api.mc.world.level.IBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Registries.class)
@SuppressWarnings("unchecked")
public interface RegistriesImpl {
    @Overwrite
    static IHolderLookup<IBlock> block() {
        return (IHolderLookup<IBlock>) BuiltInRegistries.BLOCK;
    }
}
