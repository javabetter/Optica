// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris.pipeline.renderer;

import com.optica.common.iris.pipeline.impl.PipelineAction;
import com.optica.core.iris.pipeline.texture.IrisFramebuffer;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class DeferredIrisRenderer implements IrisRenderer, PipelineAction {
    private final String name;

    private final List<Pass> passes;
    private PhotonicsRenderer activeRenderer;

    public DeferredIrisRenderer(
            String name,
            List<Pass> passes
    ) {
        this.name = name;
        this.passes = ImmutableList.copyOf(passes);
    }

    public String name() {
        return name;
    }

    @Override
    public void renderAll() {
        Objects.requireNonNull(activeRenderer).renderAll();
    }

    public List<Pass> getPasses() {
        return passes;
    }

    public void setActive(@Nullable PhotonicsRenderer activeImpl) {
        this.activeRenderer = activeImpl;
    }

    public record Pass(
            String name,
            @Nullable String fragmentShader,
            @Nullable String vertexShader,
            @Nullable IrisFramebuffer framebuffer,
            List<Runnable> actions
    ) {
        public boolean hasFragmentShader() {
            return fragmentShader != null;
        }
    }
}
