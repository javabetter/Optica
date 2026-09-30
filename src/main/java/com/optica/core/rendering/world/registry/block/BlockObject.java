// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.registry.block;

import com.optica.core.rendering.world.allocator.WorldAllocator;
import com.optica.core.rendering.world.registry.object.WorldObject;

public interface BlockObject extends WorldObject {
    void allocate(WorldAllocator allocator);
}
