// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.properties.components;

import com.optica.core.iris.properties.annotations.DefaultValue;
import com.optica.core.iris.properties.annotations.Defines;
import com.optica.core.iris.properties.annotations.Key;

public interface LightListProperties {
    @DefaultValue("false")
    boolean isEnabled();

    @DefaultValue("1000")
    @Defines("PH_MAX_LIGHTS")
    @Key(legacy = "photonics.maxLights")
    int getSize();
}
