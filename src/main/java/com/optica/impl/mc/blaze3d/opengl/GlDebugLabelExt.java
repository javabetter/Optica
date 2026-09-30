// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mc.blaze3d.opengl;

import com.optica.impl.mc.blaze3d.opengl.textures.GlTexture2D;
import com.optica.impl.mc.blaze3d.opengl.textures.IGlTexture;
import com.optica.impl.mixins.mc.blaze3d.common.systems.GpuDeviceAccessor;
import com.optica.impl.mixins.mc.blaze3d.opengl.systems.GlDeviceInvoker;
import com.mojang.blaze3d.systems.RenderSystem;

public interface GlDebugLabelExt {
    void applyLabel(IGlTexture texture2D);

    static GlDebugLabelExt getInstance() {
        var backend = ((GpuDeviceAccessor) RenderSystem.getDevice()).optica$getBackend();
        return (GlDebugLabelExt) ((GlDeviceInvoker) backend).optica$debugLabels();
    }
}
