// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.properties;

import com.optica.core.iris.properties.impl.annotations.Magic;

import java.util.function.Function;

public interface PropertyOverrides {
    @Magic default <O, T> void override(Function<O, T> option, T value) {}

    default void overrideProperties(PhotonicsProperties properties) {

    }
}
