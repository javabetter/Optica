// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.mc.world.level;

import com.optica.api.mc.IProperty;
import com.optica.api.mc.core.IBlockPos;

public interface IBlockState {
    IBlock ph$block();

    default boolean ph$is(IBlock block) {
        return ph$block() == block;
    }

    boolean ph$isAir();

    boolean ph$isSuffocating(IBlockGetter blockGetter, IBlockPos blockPos);

    boolean ph$isCollisionShapeFullBlock(IBlockGetter blockGetter, IBlockPos blockPos);

    boolean ph$hasProperty(IProperty<?> property);

    <T extends Comparable<T>> T ph$getValue(IProperty<T> property);
}
