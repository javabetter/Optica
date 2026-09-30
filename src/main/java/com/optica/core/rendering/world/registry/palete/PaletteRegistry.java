// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.registry.palete;

import com.optica.core.rendering.world.registry.object.WeakValue;
import com.optica.core.rendering.world.block.palette.PaletteEntry;
import com.optica.core.rendering.world.block.palette.PaletteTexture;
import com.optica.core.rendering.world.registry.object.ObjectRegistry;

import java.util.concurrent.locks.ReadWriteLock;

public class PaletteRegistry extends ObjectRegistry<PaletteObject> {
    private final PaletteTexture paletteTexture;

    public PaletteRegistry(
            ReadWriteLock lock,
            PaletteTexture paletteTexture
    ) {
        super(lock);
        this.paletteTexture = paletteTexture;
    }
    
    public @WeakValue PaletteObject allocate(PaletteEntry entry) {
        if (entry instanceof MutablePaletteEntry me) me.makeWhole();
        entry.computeHashCode();
        
        return cacheObject(
                entry,
                e -> new PaletteObject(this, e),
                e -> e.allocate(paletteTexture)
        );
    }
}
