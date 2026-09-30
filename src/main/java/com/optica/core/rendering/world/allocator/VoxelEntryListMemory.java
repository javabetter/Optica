// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.allocator;

import com.optica.api.Disposable;

public interface VoxelEntryListMemory extends Disposable {
    int entryData();

    void resize(int newSize);

    VoxelEntryMemory get(int index);

    void upload();
}
