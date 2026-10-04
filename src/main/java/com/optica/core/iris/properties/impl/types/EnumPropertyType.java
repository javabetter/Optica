// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.properties.impl.types;

import com.optica.core.iris.pipeline.DefineHolder;
import com.optica.core.iris.properties.impl.PropertyType;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class EnumPropertyType implements PropertyType<Enum<?>> {
    public static final EnumPropertyType INSTANCE = new EnumPropertyType();

    private EnumPropertyType() {

    }

    private static Enum<?> parseEnumValue(Class<?> enumType, String value, Runnable badEnum) {
        var enumConstants = (Enum<?>[]) Objects.requireNonNull(enumType.getEnumConstants());

        for (var enumValue : enumConstants) {
            if (enumValue.name().equalsIgnoreCase(value)) {
                return enumValue;
            }
        }

        // Optica: upstream never called this, so an unknown value fell back silently.
        badEnum.run();
        return enumConstants[0];
    }

    @Override
    public Enum<?> defaultValue(Method method) {
        return (Enum<?>) Objects.requireNonNull(method.getReturnType().getEnumConstants())[0];
    }

    @Override
    public Enum<?> parse(String key, String value, Method method, Logger logger) {
        return parseEnumValue(method.getReturnType(), value, () -> {
            var enumType = method.getReturnType();

            logger.warn(
                    "Unexpected value for {} key {} in shaders.properties: got {}, but expected one of {}",
                    enumType.getSimpleName(),
                    key,
                    value,
                    Arrays.stream((Enum<?>[]) enumType.getEnumConstants())
                            .map(Enum::name)
                            .toList()
            );
        });
    }

    @Override
    public boolean validate(String key, Enum<?> value, Method method, Logger logger) {
        return true;
    }

    @Override
    public void registerDefine(DefineHolder defineHolder, String key, Enum<?> value) {
        defineHolder.enumDefine(key, value);
    }
}
