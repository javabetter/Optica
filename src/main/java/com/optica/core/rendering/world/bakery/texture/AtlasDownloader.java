// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.bakery.texture;

import com.optica.api.Disposable;
import com.optica.api.mc.Id;

public interface AtlasDownloader extends Disposable {
    void preloadTexture(Id atlasId);

    AtlasTexture get(Id atlasId);
}
