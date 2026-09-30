// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.mc;

import com.optica.api.mc.world.level.ILevel;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public interface Minecraft {
    /**
     * Schedules {@code runnable} to be executed on the render thread during the next frame.
     */
    static void schedule(Runnable runnable) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static @Nullable ILevel getLevel() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static Vector3d getCameraPos() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static int getRenderDistance() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
