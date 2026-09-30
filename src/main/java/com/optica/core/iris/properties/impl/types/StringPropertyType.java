// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.properties.impl.types;

import com.optica.core.iris.pipeline.DefineHolder;
import com.optica.core.iris.properties.impl.PropertyType;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public class StringPropertyType implements PropertyType<String> {
    public static final String DEFAULT_VALUE = "";
    public static final StringPropertyType INSTANCE = new StringPropertyType();

    private StringPropertyType() {

    }

    @Override
    public String defaultValue(Method method) {
        return DEFAULT_VALUE;
    }

    @Override
    public String parse(String key, String value, Method method, Logger logger) {
        return value;
    }

    @Override
    public boolean validate(String key, String value, Method method, Logger logger) {
        return true;
    }

    @Override
    public void registerDefine(DefineHolder defineHolder, String key, String value) {
        defineHolder.stringDefine(key, value);
    }
}
