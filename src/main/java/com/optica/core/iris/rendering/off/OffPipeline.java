// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.rendering.off;

import com.optica.core.iris.pipeline.IrisPipeline;
import com.optica.core.iris.properties.PhotonicsProperties;
import com.optica.core.iris.rendering.PhotonicsPipeline;
import com.optica.core.iris.rendering.restir.RestirProperties;
import com.optica.core.rendering.lights.HandheldItemSupplier;
import com.optica.core.rendering.world.bakery.texture.AtlasDownloader;

public class OffPipeline extends PhotonicsPipeline {
    public OffPipeline(
            PhotonicsProperties phProperties,
            OffProperties offProperties,
            AtlasDownloader atlasDownloader,
            HandheldItemSupplier handheldItemSupplier,
            IrisPipeline irisPipeline
    ) {
        super(phProperties, atlasDownloader, irisPipeline);
    }
}
