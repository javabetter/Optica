// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris.pipeline.builder.actions;

import com.optica.common.iris.pipeline.builder.PipelineActionBuilder;
import com.optica.common.iris.pipeline.impl.PipelineAction;
import com.optica.core.iris.pipeline.texture.IrisFramebuffer;

public record FlipAction(IrisFramebuffer[] framebuffers) implements PipelineAction, PipelineActionBuilder {
    @Override
    public void execute() {
        for (var framebuffer : framebuffers)
            framebuffer.flip();
    }

    @Override
    public PipelineAction buildAction() {
        return this;
    }
}
