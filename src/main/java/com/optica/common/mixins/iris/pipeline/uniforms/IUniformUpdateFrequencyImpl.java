// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris.pipeline.uniforms;

import com.optica.core.iris.pipeline.uniform.IUniformUpdateFrequency;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(IUniformUpdateFrequency.class)
public interface IUniformUpdateFrequencyImpl {
    @Overwrite
    static IUniformUpdateFrequency once() {
        return (IUniformUpdateFrequency) (Object) UniformUpdateFrequency.ONCE;
    }

    @Overwrite
    static IUniformUpdateFrequency perTick() {
        return (IUniformUpdateFrequency) (Object) UniformUpdateFrequency.PER_TICK;
    }

    @Overwrite
    static IUniformUpdateFrequency perFrame() {
        return (IUniformUpdateFrequency) (Object) UniformUpdateFrequency.PER_FRAME;
    }

    @Overwrite
    static IUniformUpdateFrequency custom() {
        return (IUniformUpdateFrequency) (Object) UniformUpdateFrequency.CUSTOM;
    }
}
