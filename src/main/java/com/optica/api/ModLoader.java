// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api;

import java.nio.file.Path;

public interface ModLoader {
    static Path getGameDir() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static Path getConfigDir() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
