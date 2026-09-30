// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris.pipeline.framebuffer;

import com.optica.api.Disposable;
import com.optica.api.gpu.textures.IGpuTexture2D;
import org.joml.Vector2ic;

public record FramebufferAttachment(
        String name,
        IGpuTexture2D texture,
        boolean createSampler,
        boolean createPrevSampler
) implements Disposable {
    public void resize(Vector2ic newSize) {
        texture.ph$resize(newSize);
    }

    @Override
    public void close() {
        texture.close();
    }
}
