// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris.pipeline;

import com.optica.core.iris.pipeline.texture.IrisFramebuffer;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface CompositeRendererPassExt {
    int getIndex();

    void setIndex(int index);

    String getDebugName();

    void setDebugName(@Nullable String name);

    /** Run after the pass is completed */
    ImmutableList<Runnable> getActions();

    void setActions(List<Runnable> actions);

    Optional<IrisFramebuffer> getFramebuffer();

    void setFramebuffer(@Nullable IrisFramebuffer framebuffer);

    void updateSize();









}
