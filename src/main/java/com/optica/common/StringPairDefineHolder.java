// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common;

import com.optica.core.iris.pipeline.DefineHolder;
import net.irisshaders.iris.helpers.StringPair;

import java.util.List;

public record StringPairDefineHolder(List<StringPair> defines) implements DefineHolder {
    @Override
    public void stringDefine(String name, String value) {
        defines.add(new StringPair(name, value));
    }

    @Override
    public void intDefine(String name, int value) {
        defines.add(new StringPair(name, Integer.toString(value)));
    }

    @Override
    public void floatDefine(String name, float value) {
        defines.add(new StringPair(name, Float.toString(value)));
    }

    @Override
    public void enumDefine(String name, Enum<?> value) {
        defines.add(new StringPair(name, Integer.toString(value.ordinal())));
    }
}
