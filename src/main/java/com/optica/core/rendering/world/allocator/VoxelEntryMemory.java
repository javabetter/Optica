// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.allocator;

import com.optica.api.Disposable;

public interface VoxelEntryMemory extends Disposable {
    void setEntryFlag(boolean flag);

    void setEntryData(int entryData);

    void setChildMask(long mask);

    void setExtraFields(int... extra);

    void upload();
}
