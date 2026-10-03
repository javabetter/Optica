package com.optica.core.iris.rendering.cached;

import com.optica.core.iris.properties.PhotonicsProperties;
import com.optica.core.iris.properties.annotations.*;
import com.optica.core.iris.properties.PropertyDefines;

/**
 * Optica's cached lighting mode (selected in the pack's settings menu, see OpticaSettings). The
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

    /** 0 = recompute only where blocks or lights changed (no timed refresh). */
    @DefaultValue("0.0")
    @FloatRange(min = 0.0f)
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

    @DefaultValue("0")
    @IntRange(min = 0, max = 8)
    @Defines("PH_CACHE_GI_SAMPLES")
    @Key(legacy = "optica.cacheGiSamples")
    int getGiSamples();

    /** Optica profiler: count what the cache does and log it (CacheProfiler). */
    @DefaultValue("false")
    @Defines("PH_CACHE_PROFILE")
    @Key(legacy = "optica.cacheProfiler")
    boolean isProfiling();

    /** 0 off, 1 cache status colours, 2 detail level colours. */
    @DefaultValue("0")
    @IntRange(min = 0, max = 2)
    @Defines("PH_CACHE_DEBUG_VIEW")
    @Key(legacy = "optica.cacheDebugView")
    int getDebugView();

    @Override
    default void defineProperties(PhotonicsProperties properties) {
        stringDefine("PH_CACHED_ACTIVE", "");
    }
}
