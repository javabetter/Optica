// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.block.palette;

import com.optica.core.rendering.RenderingComponent;

public interface PaletteTexture extends RenderingComponent {
    PaletteTextureView reserveEntry();

    void upload();
}
