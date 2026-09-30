// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.pipeline.texture;

import com.optica.api.Disposable;
import com.optica.api.gpu.textures.ITextureFormat;
import com.optica.core.rendering.RenderingComponent;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

public interface IrisFramebuffer extends RenderingComponent, Disposable {
    void flip();

    void recalculateSizes();

    interface Builder {
        Builder addAttachment(String name, ITextureFormat format, @AttachmentUsage int usage);

        default Builder addAttachment(String name, ITextureFormat format, @AttachmentUsage int usage, BooleanSupplier condition) {
            return condition.getAsBoolean() ? addAttachment(name, format, usage) : this;
        }

        IrisFramebuffer build(Function<IrisFramebuffer, IrisFramebuffer> registration);

        default IrisFramebuffer build() {
            return build(Function.identity());
        }
    }
}
