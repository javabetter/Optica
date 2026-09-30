// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.mc.nbt;

public interface ICompoundTag {
    boolean ph$isEmpty();

    static boolean isEqual(ICompoundTag tag1, ICompoundTag tag2) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
