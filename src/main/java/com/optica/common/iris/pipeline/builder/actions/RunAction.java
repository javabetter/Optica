// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris.pipeline.builder.actions;

import com.optica.common.iris.pipeline.builder.PipelineActionBuilder;
import com.optica.common.iris.pipeline.impl.PipelineAction;

public record RunAction(Runnable action) implements PipelineAction, PipelineActionBuilder {
    @Override
    public void execute() {
        action.run();
    }

    @Override
    public PipelineAction buildAction() {
        return this;
    }
}
