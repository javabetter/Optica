// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.lights;

import com.optica.core.config.lights.BlockLightInfo;
import org.joml.Vector3i;

public class LightInvalidation {
    public final Vector3i pos;
    public BlockLightInfo before;
    public BlockLightInfo after;

    public int beforeIndex = -1;
    public int afterIndex = -1;

    public LightInvalidation(Vector3i pos) {
        this.pos = pos;
    }
}
