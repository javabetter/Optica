// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.core;

import com.optica.api.mc.core.IHolder;
import com.optica.api.mc.core.IHolderLookup;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Holder.class)
public interface HolderMixin<T> extends IHolder<T> {
    @Shadow
    T value();

    @Override
    default T ph$value() {
        return value();
    }
}
