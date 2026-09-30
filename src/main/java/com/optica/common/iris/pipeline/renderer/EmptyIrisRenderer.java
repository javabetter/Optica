// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris.pipeline.renderer;

public class EmptyIrisRenderer implements IrisRenderer {
    public static final EmptyIrisRenderer INSTANCE = new EmptyIrisRenderer();

    private EmptyIrisRenderer() {

    }

    @Override
    public void renderAll() {

    }
}
