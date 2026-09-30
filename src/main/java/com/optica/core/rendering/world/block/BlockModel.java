// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.block;

import com.optica.api.Disposable;
import com.optica.core.rendering.world.registry.light.WorldLight;
import com.optica.core.rendering.world.registry.object.WeakValue;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.Collection;
import java.util.List;

public interface BlockModel extends Disposable {
    List<Part> parts();

    interface Part extends Disposable {
        Vector3i offset();

        BlockEntry createEntry(
                int region,
                int skylight,
                @Nullable @WeakValue WorldLight light
        );
    }
}
