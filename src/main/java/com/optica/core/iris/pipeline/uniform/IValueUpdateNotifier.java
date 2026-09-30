// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.pipeline.uniform;

@FunctionalInterface
public interface IValueUpdateNotifier {
    void setListener(Runnable var1);
}
