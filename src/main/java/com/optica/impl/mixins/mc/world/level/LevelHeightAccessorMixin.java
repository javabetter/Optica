// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.world.level;

import com.optica.api.mc.world.level.ILevelHeightAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelHeightAccessor.class)
public interface LevelHeightAccessorMixin extends ILevelHeightAccessor{
    @Shadow
    int getSectionIndexFromSectionY(int i);

    @Override
    default int ph$getSectionIndexFromSectionY(int sectionY) {
        return getSectionIndexFromSectionY(sectionY);
    }
}
