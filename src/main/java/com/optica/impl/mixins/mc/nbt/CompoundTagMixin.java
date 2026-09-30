// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.nbt;

import com.optica.api.mc.nbt.ICompoundTag;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CompoundTag.class)
public abstract class CompoundTagMixin implements ICompoundTag{
    @Shadow
    public abstract boolean isEmpty();

    @Override
    public boolean ph$isEmpty() {
        return isEmpty();
    }
}
