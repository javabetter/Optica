// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.properties.rendering;

import com.optica.core.iris.properties.annotations.DefaultValue;
import com.optica.core.iris.properties.annotations.Defines;
import com.optica.core.iris.properties.annotations.FloatRange;
import com.optica.core.iris.properties.annotations.Key;

public interface HandheldProperties {
    @DefaultValue("true")
    @Defines("PH_ENABLE_HANDHELD_LIGHT")
    @Key(legacy = "photonics.enableHandheldLight")
    boolean isEnabled();

    @FloatRange(min = 0.0f)
    @DefaultValue("0.2")
    @Key(legacy = "photonics.enchantmentGlintStrength")
    float getGlintStrength();

    @DefaultValue("false")
    @Defines("PH_SEPARATE_HANDHELD_RAYS")
    @Key(legacy = "photonics.useSeparateHandheldRays")
    boolean getPerHandShadows();
}
