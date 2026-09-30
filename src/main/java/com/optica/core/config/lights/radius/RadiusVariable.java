// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config.lights.radius;

import com.optica.core.config.Variable;

public class RadiusVariable extends Variable<LightRadius> implements LightRadius {
    protected RadiusVariable(String name) {
        super(name, LightRadius.TYPE);
    }

    @Override
    public float get() {
        return actual().get();
    }
}
