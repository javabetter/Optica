// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.registry.object;

import com.optica.api.Disposable;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class InnerWorldObject<M extends Disposable> extends AbstractWorldObject<M> {
    public InnerWorldObject(ObjectRegistry<?> registry) {
        super(registry);
    }

    public M setMemory(Supplier<M> memorySupplier) {
        return super.setMemory(memorySupplier);
    }

    public @Nullable M memoryOrNull() {
        return super.memoryOrNull();
    }

    public M memoryOrThrow() {
        return super.memoryOrThrow();
    }

    @Override
    protected abstract WorldObject getKey();
}
