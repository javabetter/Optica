// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.mc;

public interface Id {
    String ph$namespace();

    String ph$path();

    static Id fromNamespaceAndPath(String namespace, String path) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static Id parse(String string) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static Id withDefaultNamespace(String path) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
