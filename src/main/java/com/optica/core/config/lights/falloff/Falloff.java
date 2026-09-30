// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config.lights.falloff;

public record Falloff(float value) implements LightFalloff {
    @Override
    public float get() {
        return value;
    }
}
