// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.properties;

import com.optica.core.TransparencyMode;
import com.optica.core.iris.properties.annotations.DefaultValue;
import com.optica.core.iris.properties.annotations.Defines;
import com.optica.core.iris.properties.annotations.FloatRange;
import com.optica.core.iris.properties.annotations.Key;
import com.optica.core.iris.properties.components.LightListProperties;
import com.optica.core.iris.properties.rendering.BlockLightProperties;
import com.optica.core.iris.properties.rendering.GiProperties;
import com.optica.core.iris.properties.rendering.HandheldProperties;
import com.optica.core.iris.rendering.PhotonicsRenderer;

public interface PhotonicsProperties {
    @DefaultValue("false")
    boolean isEnabled();

    @DefaultValue("OFF")
    @Key(legacy = "photonics.lightingMode")
    PhotonicsRenderer getRenderer();

    @DefaultValue("1.0")
    @FloatRange(min = 0.0f)
    @Defines("PH_RENDER_SCALE")
    float getRenderScale();

    @DefaultValue("NONE")
    @Key(legacy = "photonics.alphaMode")
    TransparencyMode getTransparencyMode();

    LightListProperties getLightListProperties();

    BlockLightProperties getBlockLightProperties();

    GiProperties getGiProperties();

    HandheldProperties getHandheldProperties();
}
