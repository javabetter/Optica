// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.properties.rendering;

import com.optica.core.iris.properties.annotations.DefaultValue;
import com.optica.core.iris.properties.annotations.Defines;
import com.optica.core.iris.properties.annotations.Key;

public interface BlockLightProperties {
    @DefaultValue("true")
    @Defines("PH_ENABLE_BLOCKLIGHT")
    @Key(legacy = "photonics.enableBlockLight")
    boolean isEnabled();
}
