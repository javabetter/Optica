// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.meshing;

import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = { "net.minecraft.client.renderer.rendertype.RenderSetup$TextureBinding" })
public interface TextureSetupAccessor {
    @Accessor
    Identifier getLocation();
}
