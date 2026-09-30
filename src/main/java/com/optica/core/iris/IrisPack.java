// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris;

import com.optica.api.mc.world.level.IBlockState;
import com.optica.core.iris.properties.PhotonicsProperties;

import java.util.Optional;

public interface IrisPack {
    /**
     * The name of the shader pack
     */
    String ph$name();

    /**
     * Return {@code true} when the shader pack natively support Photonics.
     */
    boolean ph$supportsPhotonics();

    int ph$getBlockId(IBlockState block);

    static Optional<IrisPack> getCurrentPack() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
