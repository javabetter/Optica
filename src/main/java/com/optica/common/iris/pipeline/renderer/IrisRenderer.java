// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris.pipeline.renderer;

import com.optica.common.iris.pipeline.impl.PipelineAction;

public interface IrisRenderer extends PipelineAction {
    void renderAll();

    @Override
    default void execute() {
        renderAll();
    }
}
