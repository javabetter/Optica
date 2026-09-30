package com.optica.core.iris.rendering.cached;

import com.optica.core.iris.properties.PhotonicsProperties;
import com.optica.core.iris.properties.annotations.*;
import com.optica.core.iris.properties.PropertyDefines;

/**
 * Optica's cached lighting mode (selected in config/optica.properties, see OpticaSettings). The
 * optica.* keys are written into the pack's properties by OpticaSettings.applyTo.
 */
public interface CachedProperties extends PropertyDefines {
    @DefaultValue("20")
    @IntRange(min = 1)
    @Defines("PH_MAX_SAMPLES")
    @Key(legacy = "photonics.maxSamples")
    int getMaxSamples();

    /** ReSTIR packs may expect GI to be included in the direct light (EP's "combined GI"). */
    @DefaultValue("false")
    @Defines("PH_CACHE_COMBINED_GI")
    @Key(legacy = "photonics.restirCombinedGi")
    boolean isCombinedGi();

    @DefaultValue("1.0")
    @FloatRange(min = 0.1f)
    @Defines("PH_CACHE_REFRESH_SECONDS")
    @Key(legacy = "optica.cacheRefreshSeconds")
    float getRefreshSeconds();

    @DefaultValue("4")
    @IntRange(min = 1, max = 8)
    @Defines("PH_CACHE_DETAIL")
    @Key(legacy = "optica.cacheDetail")
    int getDetail();

    @DefaultValue("21")
    @IntRange(min = 16, max = 24)
    @Defines("PH_CACHE_CAPACITY_LOG2")
    @Key(legacy = "optica.cacheCapacityLog2")
    int getCapacityLog2();

    @DefaultValue("2")
    @IntRange(min = 0, max = 8)
    @Defines("PH_CACHE_GI_SAMPLES")
    @Key(legacy = "optica.cacheGiSamples")
    int getGiSamples();

    @Override
    default void defineProperties(PhotonicsProperties properties) {
        stringDefine("PH_CACHED_ACTIVE", "");
    }
}
