// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.pipeline.uniform;

public interface IUniformUpdateFrequency {
    static IUniformUpdateFrequency once() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IUniformUpdateFrequency perTick() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IUniformUpdateFrequency perFrame() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IUniformUpdateFrequency custom() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
