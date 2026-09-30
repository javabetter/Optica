// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.mc.world.level.block;

import com.optica.api.mc.core.IHolderLookup;
import com.optica.api.mc.nbt.ICompoundTag;

public interface IBlockEntity {
    ICompoundTag ph$saveWithFullMetadata(IHolderLookup.Provider provider);
}
