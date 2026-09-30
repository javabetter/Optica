// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config.lights.intensity;

public record Intensity(float value) implements LightIntensity {
    @Override
    public float get() {
        return value;
    }
}
