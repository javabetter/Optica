// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris;

import com.optica.core.iris.IrisPack;
import net.irisshaders.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;

@Mixin(IrisPack.class)
public interface IShaderPackImpl {
    @Overwrite
    static Optional<IrisPack> getCurrentPack() {
        return Iris.getCurrentPack().map(e -> (IrisPack) e);
    }
}
