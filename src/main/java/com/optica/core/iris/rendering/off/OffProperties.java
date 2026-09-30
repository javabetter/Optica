// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.rendering.off;

import com.optica.core.iris.properties.PhotonicsProperties;
import com.optica.core.iris.properties.PropertyDefines;

public interface OffProperties extends PropertyDefines {
    @Override
    default void defineProperties(PhotonicsProperties properties) {
        stringDefine("PH_OFF_ACTIVE", "");
    }
}
