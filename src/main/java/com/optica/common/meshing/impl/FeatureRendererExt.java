// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.meshing.impl;

public interface FeatureRendererExt {
    void setRenderShadows(boolean enabled);

    void setRenderFlames(boolean enabled);

    void setRenderNametags(boolean enabled);

    void setRenderText(boolean enabled);

    void setRenderParticles(boolean enabled);
}
