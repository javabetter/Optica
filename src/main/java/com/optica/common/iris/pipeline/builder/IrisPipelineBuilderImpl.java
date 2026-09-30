// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris.pipeline.builder;

import com.optica.common.iris.pipeline.IrisPipelineImpl;
import com.optica.common.iris.pipeline.IrisRendererImpl;
import com.optica.common.iris.pipeline.impl.PipelineAction;
import com.optica.core.iris.pipeline.IrisPipeline;
import com.optica.core.iris.pipeline.IrisRenderer;
import com.optica.core.iris.pipeline.buffer.IBufferHolder;
import com.optica.core.iris.pipeline.texture.ISamplerHolder;
import com.optica.core.iris.pipeline.texture.IrisFramebuffer;
import com.optica.core.iris.pipeline.uniform.IDynamicUniformHolder;
import com.optica.core.iris.pipeline.uniform.IUniformHolder;
import it.unimi.dsi.fastutil.ints.IntObjectBiConsumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class IrisPipelineBuilderImpl extends AbstractActionBuilderConsumer implements IrisRenderer.Builder {
    private @Nullable IrisFramebuffer framebuffer = null;

    private @NonNls String fragmentPrefix = "";
    private @NonNls String vertexPrefix = "";

    public IrisPipelineBuilderImpl(IrisPipelineImpl factory) {
        super(factory, "Photonics");
    }

    @Override
    public IrisRenderer.Builder withBuffer(Consumer<IBufferHolder> consumer) {
        factory.withBuffer(consumer);

        return this;
    }

    @Override
    public IrisRenderer.Builder withSampler(Consumer<ISamplerHolder> consumer) {
        factory.withSampler(consumer);

        return this;
    }

    @Override
    public IrisRenderer.Builder withDynamicUniform(Consumer<IDynamicUniformHolder> consumer) {
        factory.withDynamicUniform(consumer);

        return this;
    }

    @Override
    public IrisRenderer.Builder withUniform(Consumer<IUniformHolder> consumer) {
        factory.withUniform(consumer);

        return this;
    }

    private void scope(Consumer<IrisRenderer.Builder> builderAction) {
        var oldFrameBuffer = framebuffer;
        var oldFragmentPrefix = fragmentPrefix;
        var oldVertexPrefix = vertexPrefix;

        builderAction.accept(this);

        framebuffer = oldFrameBuffer;
        fragmentPrefix = oldFragmentPrefix;
        vertexPrefix = oldVertexPrefix;
    }

    @Override
    public IrisRenderer.Builder withFragmentPrefix(@NonNls String prefix) {
        Objects.requireNonNull(prefix, "fragment prefix was null");
        fragmentPrefix = prefix;

        return this;
    }

    @Override
    public IrisRenderer.Builder withVertexPrefix(@NonNls String prefix) {
        Objects.requireNonNull(prefix, "vertex prefix was null");
        vertexPrefix = prefix;

        return this;
    }

    @Override
    public IrisRenderer.Builder debugGroup(String name) {
        addDebugGroup(name);

        return this;
    }

    @Override
    public IrisRenderer.Builder withFramebuffer(IrisFramebuffer framebuffer) {
        this.framebuffer = framebuffer;
        return this;
    }

    @Override
    public IrisRenderer.Builder deferredPass(String name, @Nullable String fragmentShader, @Nullable String vertexShader) {
        addDeferredPass(
                name,
                framebuffer,
                fragmentShader != null ? fragmentPrefix + fragmentShader : null,
                vertexShader != null ? vertexPrefix + vertexShader : null
        );

        return this;
    }

    @Override
    public IrisRenderer.Builder thenFlip(IrisFramebuffer... framebuffers) {
        addThenFlip(framebuffers);

        return this;
    }

    @Override
    public IrisRenderer.Builder thenRun(Runnable action) {
        addThenRun(action);

        return this;
    }

    @Override
    public IrisRenderer.Builder repeat(int n, IntObjectBiConsumer<IrisRenderer.Builder> builderAction) {
        for (int i = 0; i < n; i++) {
            final int index = i;
            scope((b0) -> builderAction.accept(index, b0));
        }

        return this;
    }

    @Override
    public IrisRenderer.Builder when(BooleanSupplier condition, Consumer<IrisRenderer.Builder> builderAction) {
        if (condition.getAsBoolean())
            scope(builderAction);

        return this;
    }

    @Override
    public IrisRenderer build(Function<IrisRenderer, IrisRenderer> registration) {
        return registration.apply(new IrisRendererImpl(buildActions()));
    }

    @Override
    public PipelineAction buildAction() {
        throw new UnsupportedOperationException("buildActions");
    }
}
