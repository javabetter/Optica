// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config.lights.intensity;

import com.optica.core.config.Variable;

public class IntensityVariable extends Variable<LightIntensity> implements LightIntensity {
    protected IntensityVariable(String name) {
        super(name, LightIntensity.TYPE);
    }

    @Override
    public float get() {
        return actual().get();
    }
}
