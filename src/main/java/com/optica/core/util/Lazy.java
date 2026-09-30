// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.util;

import java.util.function.Supplier;

public class Lazy<T> {
    private static final Object NOT_PRESENT = new Object();

    private Object value = NOT_PRESENT;
    private Supplier<T> supplier;

    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @SuppressWarnings("unchecked")
    public T get() {
        if (value != NOT_PRESENT)
            return (T) value;

        var result = supplier.get();
        supplier = null;
        value = result;

        return result;
    }

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }
}
