// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris.pipeline.builder;

import com.optica.common.iris.pipeline.impl.PipelineAction;
import com.optica.core.iris.pipeline.texture.IrisFramebuffer;
import org.jetbrains.annotations.Nullable;

public interface PipelineActionBuilder {
    default boolean addDebugGroup(String name) {
        return false;
    }

    default boolean addDeferredPass(String name, @Nullable IrisFramebuffer framebuffer, @Nullable String fragmentShader, @Nullable String vertexShader) {
        return false;
    }

    default boolean addThenFlip(IrisFramebuffer... framebuffers) {
        return false;
    }

    default boolean addThenRun(Runnable action) {
        return false;
    }

    PipelineAction buildAction();
}
