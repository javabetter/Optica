package com.optica.core.iris.properties.components;

import com.optica.core.iris.properties.annotations.DefaultValue;
import com.optica.core.iris.properties.annotations.Defines;
import com.optica.core.iris.properties.annotations.FloatRange;
import com.optica.core.iris.properties.annotations.IntRange;
import com.optica.core.iris.properties.annotations.Key;

/**
 * Optica's performance settings, for every lighting mode. The optica.* keys are written into the pack's
 * properties by OpticaSettings.applyTo (they are options in the pack's settings menu).
 */
public interface OptimizationProperties {
    /**
     * Multiplies the distances at which lighting gets less detailed (fewer lights, less frequent updates,
     * coarser cache samples). 1 is Optica's default; very large values turn the level of detail off.
     */
    @DefaultValue("1.0")
    @FloatRange(min = 0.01f)
    @Defines("PH_LOD_SCALE")
    @Key(legacy = "optica.lodScale")
    float getLodScale();

    /** How aggressively groups of identical lights are merged: 0 off, 2 default, 4 strongest. */
    @DefaultValue("2")
    @IntRange(min = 0, max = 4)
    @Key(legacy = "optica.lightMerging")
    int getLightMerging();

    /** BASIC mode: frames between shadow ray updates of a nearby pixel (further away it is longer). */
    @DefaultValue("6")
    @IntRange(min = 1, max = 16)
    @Defines("PH_SHARP_REFRESH_INTERVAL")
    @Key(legacy = "optica.shadowUpdateInterval")
    int getShadowUpdateInterval();
}
