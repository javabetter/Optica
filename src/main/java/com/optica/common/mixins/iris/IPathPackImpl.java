// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris;

import com.optica.core.iris.IrisPackPath;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(IrisPackPath.class)
public interface IPathPackImpl {
    @Overwrite
    static IrisPackPath fromAbsolutePath(String absolutePath) {
        return (IrisPackPath) AbsolutePackPath.fromAbsolutePath(absolutePath);
    }
}
