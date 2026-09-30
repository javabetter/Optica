// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.registry.object;

import com.optica.api.Disposable;

public class NoMemory implements Disposable {
    public static final NoMemory INSTANCE = new NoMemory();

    private NoMemory() {

    }

    @Override
    public void close() {

    }
}
