// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.meshing;

import com.optica.api.gpu.textures.TextureUsage;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ReloadableTexture.class)
public abstract class ReloadableTextureMixin {
    @ModifyConstant(method = "doLoad", constant = @Constant(intValue = 5))
    private int modifyUsage(int constant) {
        return constant | TextureUsage.COPY_SRC;
    }
}
