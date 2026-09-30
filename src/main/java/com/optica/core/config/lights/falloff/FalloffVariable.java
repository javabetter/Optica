// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config.lights.falloff;

import com.optica.core.config.Variable;

public class FalloffVariable extends Variable<LightFalloff> implements LightFalloff {
    protected FalloffVariable(String name) {
        super(name, LightFalloff.TYPE);
    }

    @Override
    public float get() {
        return actual().get();
    }
}
