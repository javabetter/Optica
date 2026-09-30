// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc;

import com.optica.api.mc.Id;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Identifier.class)
public abstract class IdentifierMixin implements Id {
    @Shadow
    public abstract String getNamespace();

    @Shadow
    public abstract String getPath();

    @Override
    public String ph$namespace() {
        return getNamespace();
    }

    @Override
    public String ph$path() {
        return getPath();
    }
}
