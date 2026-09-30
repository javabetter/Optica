// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris.pipeline.framebuffer;

import com.optica.core.iris.pipeline.texture.IrisFramebuffer;
import org.joml.Vector2ic;

public interface InternalIrisFramebuffer extends IrisFramebuffer {
    Vector2ic viewportSize();

    void bind();

    void unbind();
}
