// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.allocator;

import com.optica.api.Disposable;
import com.optica.core.config.lights.BlockLightInfo;

public interface WorldLightMemory extends Disposable {
    int entryData();

    void setLight(BlockLightInfo light, int blockId);

    void upload();
}
