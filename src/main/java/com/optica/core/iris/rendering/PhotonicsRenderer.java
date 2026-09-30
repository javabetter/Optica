// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.rendering;

import com.optica.core.iris.pipeline.IrisPipeline;
import com.optica.core.iris.properties.PhotonicsProperties;
import com.optica.core.iris.properties.impl.PropertiesManager;
import com.optica.core.iris.rendering.cached.CachedPipeline;
import com.optica.core.iris.rendering.cached.CachedProperties;
import com.optica.core.iris.rendering.off.OffPipeline;
import com.optica.core.iris.rendering.off.OffProperties;
import com.optica.core.iris.rendering.restir.RestirPipeline;
import com.optica.core.iris.rendering.restir.RestirProperties;
import com.optica.core.iris.rendering.sharp.SharpPipeline;
import com.optica.core.iris.rendering.sharp.SharpProperties;
import com.optica.core.rendering.lights.HandheldItemSupplier;
import com.optica.core.rendering.world.bakery.texture.AtlasDownloader;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public enum PhotonicsRenderer {
    OFF(OffProperties.class, OffPipeline::new),
    BASIC(SharpProperties.class, SharpPipeline::new),
    SHARP(SharpProperties.class, SharpPipeline::new),
    RESTIR(RestirProperties.class, RestirPipeline::new),
    // Optica: selected on the Optica page of the pack's settings menu (see OpticaSettings), not by packs.
    CACHED(CachedProperties.class, CachedPipeline::new);

    private final String key = makeKey(name());
    private final Class<?> propertiesType;

    private final PhotonicsPipeline.Supplier<PhotonicsPipeline, Object> supplier;

    @SuppressWarnings("unchecked")
    <T extends PhotonicsPipeline, P> PhotonicsRenderer(Class<P> propertiesType, PhotonicsPipeline.Supplier<T, P> supplier) {
        this.propertiesType = propertiesType;
        this.supplier = (PhotonicsPipeline.Supplier<PhotonicsPipeline, Object>) supplier;
    }

    public String getKey() {
        return key;
    }

    public Class<?> getPropertiesType() {
        return propertiesType;
    }

    public static @Nullable PhotonicsPipeline createPipeline(
            PropertiesManager propertiesManager,
            Supplier<AtlasDownloader> atlasDownloaderSupplier,
            Supplier<HandheldItemSupplier> handheldItemSupplierSupplier,
            IrisPipeline irisPipeline
    ) {
        PhotonicsProperties properties  = propertiesManager.getProperties(PhotonicsProperties.class);
        if (!properties.isEnabled()) return null;

        PhotonicsRenderer renderer = properties.getRenderer();
        Object rendererProperties = propertiesManager.getProperties(renderer.propertiesType);

        return renderer.supplier.create(
                properties,
                rendererProperties,
                atlasDownloaderSupplier.get(),
                handheldItemSupplierSupplier.get(),
                irisPipeline
        );
    }

    private static String makeKey(String name) {
        String[] parts = name.split("_");
        StringBuilder result = new StringBuilder();
        result.append(parts[0].toLowerCase());

        for (int i = 1; i < parts.length; i++) {
            var part = parts[i];
            if (part.isEmpty()) continue;

            result.append(part.substring(0, 1).toUpperCase());
            result.append(part.substring(1).toLowerCase());
        }

        return result.toString();
    }
}
