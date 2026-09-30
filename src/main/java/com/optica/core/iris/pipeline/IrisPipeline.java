// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.pipeline;

import com.optica.core.iris.pipeline.buffer.IBufferHolder;
import com.optica.core.iris.pipeline.buffer.IBufferHolderBuilder;
import com.optica.core.iris.pipeline.texture.ISamplerHolder;
import com.optica.core.iris.pipeline.texture.ISamplerHolderBuilder;
import com.optica.core.iris.pipeline.texture.IrisFramebuffer;
import com.optica.core.iris.pipeline.uniform.IDynamicUniformHolder;
import com.optica.core.iris.pipeline.uniform.IDynamicUniformHolderBuilder;
import com.optica.core.iris.pipeline.uniform.IUniformHolderBuilder;
import com.optica.core.rendering.RenderingComponent;

public interface IrisPipeline extends
        RenderingComponent,
        IBufferHolderBuilder<IrisPipeline>,
        ISamplerHolderBuilder<IrisPipeline>,
        IDynamicUniformHolderBuilder<IrisPipeline>,
        IUniformHolderBuilder<IrisPipeline> {
    IrisFramebuffer.Builder newFramebuffer(int width, int height);

    IrisFramebuffer.Builder newFramebuffer(float widthScale, float heightScale);

    default IrisFramebuffer.Builder newFramebuffer(float scale) {
        return newFramebuffer(scale, scale);
    }

    IrisRenderer.Builder newRenderer();
}
