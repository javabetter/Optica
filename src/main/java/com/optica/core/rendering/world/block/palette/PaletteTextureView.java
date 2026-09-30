// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.block.palette;

import com.optica.api.Disposable;
import org.joml.Vector4i;

public interface PaletteTextureView extends Disposable {
    int entryData();

    void writeFace(int face, Vector4i value);

    void upload();
}
