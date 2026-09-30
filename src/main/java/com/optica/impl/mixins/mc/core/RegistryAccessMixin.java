// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.core;

import com.optica.api.mc.core.IRegistryAccess;
import net.minecraft.core.RegistryAccess;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RegistryAccess.class)
public interface RegistryAccessMixin extends IRegistryAccess {
}
