// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris.pipeline.framebuffer;

import com.optica.api.gpu.systems.IRenderSystem;
import com.optica.api.gpu.textures.IGpuTexture2D;
import com.optica.api.gpu.textures.ITextureFormat;
import com.optica.api.gpu.textures.TextureUsage;
import com.optica.core.iris.pipeline.texture.AttachmentUsage;
import com.optica.core.iris.pipeline.texture.IrisFramebuffer;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class IrisFramebufferBuilderImpl implements IrisFramebuffer.Builder {
    private final FramebufferSize sizeSupplier;
    private final Vector2ic initialSize;

    private final List<FramebufferAttachment> writeAttachments = new ArrayList<>();
    private final List<FramebufferAttachment> readAttachments = new ArrayList<>();

    private int swapAttachmentsCount = 0;

    public IrisFramebufferBuilderImpl(FramebufferSize sizeSupplier) {
        this.sizeSupplier = sizeSupplier;
        initialSize = sizeSupplier.get();
    }

    @Override
    public IrisFramebuffer.Builder addAttachment(String name, ITextureFormat format, @AttachmentUsage int usage) {
        final var writeAttachment = IRenderSystem.getDevice()
                .ph$createTexture2D(
                        () -> name + " main",
                        TextureUsage.RENDER_ATTACHMENT,
                        format,
                        initialSize.x(), initialSize.y(),
                        1
                );

        final IGpuTexture2D readAttachment;

        if ((usage & AttachmentUsage.FLIP) != 0) {
            readAttachment = IRenderSystem.getDevice()
                    .ph$createTexture2D(
                            () -> name + " alt",
                            TextureUsage.RENDER_ATTACHMENT,
                            format,
                            initialSize.x(), initialSize.y(),
                            1
                    );

            swapAttachmentsCount++;
        } else readAttachment = writeAttachment;

        writeAttachments.add(
                new FramebufferAttachment(
                        name,
                        writeAttachment,
                        (usage & AttachmentUsage.CREATE_SAMPLER) != 0,
                        (usage & AttachmentUsage.FLIP) != 0
                )
        );

        readAttachments.add(
                new FramebufferAttachment(
                        name,
                        readAttachment,
                        (usage & AttachmentUsage.CREATE_SAMPLER) != 0,
                        (usage & AttachmentUsage.FLIP) != 0
                )
        );

        return this;
    }

    public IrisFramebuffer build(Function<IrisFramebuffer, IrisFramebuffer> registration) {
        if (readAttachments.isEmpty()) return EmptyFramebuffer.INSTANCE;

        return registration.apply(swapAttachmentsCount == 0 ? new SingleFramebuffer(
                writeAttachments,
                sizeSupplier
        ) : new FlippableFramebuffer(
                new SingleFramebuffer(writeAttachments, sizeSupplier),
                new SingleFramebuffer(readAttachments, sizeSupplier)
        ));
    }
}
