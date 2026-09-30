// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris;

import com.optica.core.iris.IrisPackPath;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.file.Path;
import java.util.Optional;

@Mixin(AbsolutePackPath.class)
public abstract class AbsolutePackPathMixin implements IrisPackPath {
    @Shadow
    @Final
    private String path;

    @Shadow
    public abstract Optional<AbsolutePackPath> parent();

    @Shadow
    public abstract AbsolutePackPath resolve(String path);

    @Shadow
    public abstract Path resolved(Path root);

    @Override
    public Optional<IrisPackPath> ph$parent() {
        return (Optional) parent();
    }

    public IrisPackPath ph$resolve(String path) {
        return (IrisPackPath) resolve(path);
    }

    @Override
    public Path ph$resolved(Path root) {
        return resolved(root);
    }

    public boolean ph$startsWith(IrisPackPath path) {
        return this.path.startsWith(((AbsolutePackPath) path).getPathString());
    }

    public String ph$pathString() {
        return path;
    }
}
