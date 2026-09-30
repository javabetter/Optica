// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris.pipeline;

import com.optica.common.iris.pipeline.impl.PipelineAction;
import com.optica.core.iris.pipeline.IrisRenderer;

import java.util.List;

public record IrisRendererImpl(List<PipelineAction> actions) implements IrisRenderer {
    @Override
    public void renderAll() {
        actions.forEach(PipelineAction::execute);
    }
}
