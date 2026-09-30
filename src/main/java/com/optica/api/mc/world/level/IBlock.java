// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.mc.world.level;

import com.optica.api.mc.Id;
import com.optica.api.mc.world.level.block.state.IStateDefinition;

import java.util.Optional;

public interface IBlock {
    Id ph$id();

    IStateDefinition<IBlock, IBlockState> ph$stateDefinition();

    IBlockState ph$defaultBlockState();

    static Optional<IBlock> fromId(Id id) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IBlock fromIdOrThrow(Id id) {
        return fromId(id).orElseThrow();
    }
}
