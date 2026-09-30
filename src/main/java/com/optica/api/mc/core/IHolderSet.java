// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.mc.core;

import java.util.stream.Stream;

public interface IHolderSet<T> extends Iterable<IHolder<T>> {
    Stream<IHolder<T>> ph$stream();
}
