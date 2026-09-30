// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.core;

import com.optica.api.mc.core.IHolderLookup;
import net.minecraft.core.HolderLookup;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HolderLookup.class)
public interface HolderLookupMixin<T> extends IHolderLookup<T> {
}
