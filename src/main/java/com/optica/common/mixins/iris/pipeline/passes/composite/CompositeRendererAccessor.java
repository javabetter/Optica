// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris.pipeline.passes.composite;

import com.optica.common.iris.pipeline.CompositeRendererPassExt;
import com.google.common.collect.ImmutableList;
import net.irisshaders.iris.pipeline.CompositeRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CompositeRenderer.class)
public interface CompositeRendererAccessor {
    @Accessor("passes")
    ImmutableList<CompositeRendererPassExt> getPasses();
}
