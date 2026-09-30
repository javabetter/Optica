// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config.lights;

public interface LightsProvider {
    void registerLights(LightRegistry lights);

    void registerChangeListener(Runnable consumer);

    void clearListeners();
}
