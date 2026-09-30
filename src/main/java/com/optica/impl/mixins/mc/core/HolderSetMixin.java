// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.core;

import com.optica.api.mc.core.IHolder;
import com.optica.api.mc.core.IHolderLookup;
import com.optica.api.mc.core.IHolderSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.stream.Stream;

@Mixin(HolderSet.class)
public interface HolderSetMixin<T> extends IHolderSet<T> {
    @Shadow
    Stream<Holder<T>> stream();

    @Override
    default Stream<IHolder<T>> ph$stream() {
        return (Stream) stream();
    }
}
