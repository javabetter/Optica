// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering;

import com.optica.api.Disposable;
import com.optica.core.iris.pipeline.buffer.IBufferHolder;
import com.optica.core.iris.pipeline.texture.ISamplerHolder;
import com.optica.core.iris.pipeline.uniform.IDynamicUniformHolder;
import com.optica.core.iris.pipeline.uniform.IUniformHolder;

public interface RenderingComponent extends Disposable {
    default void onFrameBegin() {}

    default void onSectionAdded(int x, int y, int z) {}

    default void onSectionChanged(int x, int y, int z) {}

    default void registerUniforms(IUniformHolder uniforms) {}

    default void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms) {}

    default void registerBuffers(IBufferHolder buffers) {}

    default void registerCustomTextures(ISamplerHolder samplers) {}

    @Override
    default void close() {}
}
