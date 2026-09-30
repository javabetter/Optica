// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris.pipeline.uniforms;

import com.optica.core.iris.pipeline.uniform.IUniformUpdateFrequency;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(UniformUpdateFrequency.class)
public abstract class UniformUpdateFrequencyMixin implements IUniformUpdateFrequency {
}
