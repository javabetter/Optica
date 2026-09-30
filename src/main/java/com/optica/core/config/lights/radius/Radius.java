// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config.lights.radius;

public record Radius(float value) implements LightRadius {
    @Override
    public float get() {
        return value;
    }
}
