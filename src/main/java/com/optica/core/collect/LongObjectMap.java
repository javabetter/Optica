// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.collect;

import com.trivago.fastutilconcurrentwrapper.PrimitiveLongKeyMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;

import java.util.function.BiFunction;

public interface LongObjectMap<V> extends PrimitiveLongKeyMap {
    V get(long key);

    V put(long key, V value);

    V putIfAbsent(long key, V value);

    V remove(long key);

    boolean remove(long key, V value);

    V computeIfAbsent(long key, Long2ObjectFunction<V> mappingFunction);

    V computeIfPresent(long key, BiFunction<Long, V, V> mappingFunction);
}
