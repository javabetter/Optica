// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.mc;

import java.util.Optional;

public interface IProperty<T extends Comparable<T>> {
    Optional<T> ph$getValue(String name);
}
