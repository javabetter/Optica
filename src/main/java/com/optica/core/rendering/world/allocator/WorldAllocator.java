// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.allocator;

import com.optica.core.rendering.RenderingComponent;

public interface WorldAllocator extends RenderingComponent {
    VoxelEntryMemory allocateEntry(boolean useChildMask, int extraFields);

    VoxelEntryListMemory allocateEntryList(boolean useChildMask, int extraFields);

    WorldLightMemory allocateWorldLight();

    void upload();
}
