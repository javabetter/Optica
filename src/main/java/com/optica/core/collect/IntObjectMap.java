// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.collect;

import com.trivago.fastutilconcurrentwrapper.PrimitiveIntKeyMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;

import java.util.function.BiFunction;

public interface IntObjectMap<V> extends PrimitiveIntKeyMap {
    V get(int key);

    V put(int key, V value);

    V remove(int key);

    boolean remove(int key, V value);

    V computeIfAbsent(int key, Int2ObjectFunction<V> mappingFunction);

    V computeIfPresent(int key, BiFunction<Integer, V, V> mappingFunction);
}
