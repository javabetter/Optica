// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris.pipeline;

import com.optica.common.iris.pipeline.renderer.DeferredIrisRenderer;
import com.optica.core.iris.rendering.PhotonicsPipeline;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface PipelineManagerExt {
    List<DeferredIrisRenderer> getRenderers();

    void setRenderers(@Nullable List<com.optica.common.iris.pipeline.renderer.PhotonicsRenderer> renderers);

    /** Optica: rebuild all pipelines before the next pipeline selection (see PipelineManagerMixin). */
    void requestRebuild();
}
