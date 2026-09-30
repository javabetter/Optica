// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.meshing.impl;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jspecify.annotations.NonNull;

public class EmptyBufferSource extends MultiBufferSource.BufferSource {
    public static final EmptyBufferSource INSTANCE = new EmptyBufferSource();

    private EmptyBufferSource() {
        super(null, null);
    }

    @Override
    public @NonNull VertexConsumer getBuffer(@NonNull RenderType renderType) {
        return EmptyVertexConsumer.INSTANCE;
    }

    @Override
    public void endLastBatch() {

    }

    @Override
    public void endBatch() {

    }

    @Override
    public void endBatch(@NonNull RenderType renderType) {

    }
}
