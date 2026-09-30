// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc;

import com.optica.api.mc.Id;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Id.class)
public interface IdImpl {
    @Overwrite
    static Id fromNamespaceAndPath(String namespace, String path) {
        return (Id) (Object) Identifier.fromNamespaceAndPath(namespace, path);
    }

    @Overwrite
    static Id parse(String string) {
        return (Id) (Object) Identifier.parse(string);
    }

    @Overwrite
    static Id withDefaultNamespace(String path) {
        return (Id) (Object) Identifier.withDefaultNamespace(path);
    }
}
