// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.block.palette;

import it.unimi.dsi.fastutil.ints.IntList;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;

public class BlockPalette extends AbstractList<PaletteEntry> {
    private final Map<MutablePaletteEntry, MutablePaletteEntry> mapping;
    private final List<MutablePaletteEntry> entries;
    private final IntList tints;

    public BlockPalette(
            Map<MutablePaletteEntry, MutablePaletteEntry> mapping,
            List<MutablePaletteEntry> entries,
            IntList tints
    ) {
        this.mapping = mapping;
        this.entries = entries;
        this.tints = tints;
    }

    public int getIndex(MutablePaletteEntry entry) {
        if (entry == null) return 0;

        return mapping.get(entry).getIndex(entry.tint) + 1;
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public PaletteEntry get(int index) {
        return entries.get(index);
    }

    public int getTint(int index) {
        return tints.getInt(index);
    }
}
