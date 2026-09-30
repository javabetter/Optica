// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.mc.world.level.block.state;

import com.optica.api.mc.IProperty;
import org.jetbrains.annotations.Nullable;

public interface IStateDefinition<K, V> {
    @Nullable IProperty<?> ph$getProperty(String string);
}
