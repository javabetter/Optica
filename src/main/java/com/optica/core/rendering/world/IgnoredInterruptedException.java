// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world;

public class IgnoredInterruptedException extends RuntimeException {
    public static boolean shouldIgnore(Throwable t) {
        if (t == null) return true;

        while (t != null) {
            if (t instanceof IgnoredInterruptedException) return true;
            t = t.getCause();
        }

        return false;
    }
}
