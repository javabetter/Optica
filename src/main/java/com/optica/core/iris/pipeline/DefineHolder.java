// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.pipeline;

public interface DefineHolder {
    void stringDefine(String name, String value);

    void intDefine(String name, int value);

    void floatDefine(String name, float value);

    void enumDefine(String name, Enum<?> value);
}
