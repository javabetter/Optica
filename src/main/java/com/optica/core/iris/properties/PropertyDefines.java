// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.properties;

import com.optica.core.iris.properties.impl.annotations.Magic;

public interface PropertyDefines {
    @Magic default void stringDefine(String name, String value) {}
    @Magic default void intDefine(String name, int value) {}
    @Magic default void floatDefine(String name, float value) {}
    @Magic default <T extends Enum<T>> void enumDefine(String name, T value) {}

    default void defineProperties(PhotonicsProperties properties) {

    }
}
