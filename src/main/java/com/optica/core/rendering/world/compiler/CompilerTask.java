// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.compiler;

public interface CompilerTask {
    void queueJob(Runnable task);

    void awaitCompletion() throws InterruptedException;
}
