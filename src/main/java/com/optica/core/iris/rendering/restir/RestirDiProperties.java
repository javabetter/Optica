// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.rendering.restir;

import com.optica.core.iris.properties.annotations.DefaultValue;
import com.optica.core.iris.properties.annotations.Defines;
import com.optica.core.iris.properties.annotations.IntRange;
import com.optica.core.iris.properties.annotations.Key;

public interface RestirDiProperties {
    @DefaultValue("true")
    @Defines("PH_RESTIR_SOFT_SHADOWS")
    @Key(legacy = "photonics.restirSoftShadows")
    boolean useSoftShadows();

    @DefaultValue("4")
    @IntRange(min = 1)
    @Defines("PH_RESTIR_INITIAL_SAMPLES")
    @Key(legacy = "photonics.restirInitialSamples")
    int getInitialCandidates();
}
