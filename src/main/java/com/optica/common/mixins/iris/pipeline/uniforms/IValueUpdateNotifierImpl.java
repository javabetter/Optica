// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris.pipeline.uniforms;

import com.optica.core.iris.pipeline.uniform.IValueUpdateNotifier;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IValueUpdateNotifier.class)
public interface IValueUpdateNotifierImpl extends ValueUpdateNotifier {
}
