// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.mc.core.registries;

import com.optica.api.mc.core.IHolderLookup;
import com.optica.api.mc.world.level.IBlock;

public interface Registries {
    static IHolderLookup<IBlock> block() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
