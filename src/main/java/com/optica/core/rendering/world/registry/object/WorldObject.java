// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.registry.object;

import com.optica.api.Disposable;
import org.jetbrains.annotations.Nullable;

public interface WorldObject extends Disposable {
    boolean isAllocated();

    void awaitAllocated();

    void acquireReference();

    boolean tryAcquireReference();

    interface Handle<T extends WorldObject> {
        @Nullable T free();
    }
}
