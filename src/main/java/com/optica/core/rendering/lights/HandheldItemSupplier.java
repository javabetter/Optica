// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.lights;

import com.optica.api.mc.world.level.IBlockState;

import java.util.Optional;

public interface HandheldItemSupplier {
    boolean isLeftHanded();

    Optional<HandheldItem> getMainHand();

    Optional<HandheldItem> getOffHand();
}
