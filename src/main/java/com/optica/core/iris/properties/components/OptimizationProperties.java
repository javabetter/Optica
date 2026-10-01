package com.optica.core.iris.properties.components;

import com.optica.core.iris.properties.annotations.DefaultValue;
import com.optica.core.iris.properties.annotations.Defines;
import com.optica.core.iris.properties.annotations.FloatRange;
import com.optica.core.iris.properties.annotations.IntRange;
import com.optica.core.iris.properties.annotations.Key;

/**
 * Optica's performance settings, for every lighting mode. All of them are off by default, which renders
 * like Photonics itself. The optica.* keys are written into the pack's properties by
 * OpticaSettings.applyTo (they are options in the pack's settings menu).
 */
public interface OptimizationProperties {
    /**
     * Multiplies the distances at which lighting gets less detailed (fewer lights, less frequent updates,
     * coarser cache samples). 1 gives the distances the level of detail was tuned with; very large values
     * (the default) turn it off.
     */
    @DefaultValue("1000.0")
    @FloatRange(min = 0.01f)
    @Defines("PH_LOD_SCALE")
    @Key(legacy = "optica.lodScale")
    float getLodScale();

    /** How aggressively groups of identical lights are merged: 0 off (the default), 2 medium, 4 strongest. */
    @DefaultValue("0")
    @IntRange(min = 0, max = 4)
    @Key(legacy = "optica.lightMerging")
    int getLightMerging();

    /**
     * BASIC mode: frames between shadow ray updates of a nearby pixel (further away it is longer with the
     * level of detail on). 1, the default, traces every pixel every frame.
     */
    @DefaultValue("1")
    @IntRange(min = 1, max = 16)
    @Defines("PH_SHARP_REFRESH_INTERVAL")
    @Key(legacy = "optica.shadowUpdateInterval")
    int getShadowUpdateInterval();
}
