// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.mc.world.level;

import com.optica.api.mc.core.IRegistryAccess;

public interface ILevelReader extends IBlockAndTintGetter {
    IRegistryAccess ph$registryAccess();

    static ILevelReader createFacade(IBlockState blockState) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
