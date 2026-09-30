// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.rendering.sharp;

import com.optica.core.iris.properties.PhotonicsProperties;
import com.optica.core.iris.properties.PropertyDefines;
import com.optica.core.iris.properties.annotations.DefaultValue;
import com.optica.core.iris.properties.annotations.Defines;
import com.optica.core.iris.properties.annotations.IntRange;
import com.optica.core.iris.properties.annotations.Key;

public interface SharpProperties extends PropertyDefines {
    @DefaultValue("20")
    @IntRange(min = 1)
    @Defines("PH_MAX_SAMPLES")
    @Key(legacy = "photonics.maxSamples")
    int getMaxSamples();

    @Override
    default void defineProperties(PhotonicsProperties properties) {
        stringDefine("PH_SHARP_ACTIVE", "");
    }
}
