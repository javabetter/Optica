// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.meshing.impl;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jspecify.annotations.NonNull;

public class EmptyOutlineBufferSource extends OutlineBufferSource {
    public static final EmptyOutlineBufferSource INSTANCE = new EmptyOutlineBufferSource();

    private EmptyOutlineBufferSource() {
    }

    @Override
    public VertexConsumer getBuffer(@NonNull RenderType renderType) {
        return EmptyVertexConsumer.INSTANCE;
    }

    @Override
    public void setColor(int i) {

    }

    @Override
    public void endOutlineBatch() {

    }
}
