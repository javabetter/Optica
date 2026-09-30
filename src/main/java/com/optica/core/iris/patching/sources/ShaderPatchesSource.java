// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.patching.sources;

import com.optica.core.iris.patching.PatchSource;

import java.nio.file.Path;
import java.util.stream.Stream;

public class ShaderPatchesSource implements PatchSource {
    public static final ShaderPatchesSource INSTANCE = new ShaderPatchesSource();

    private ShaderPatchesSource() {}

    @Override
    public Stream<Path> streamPatches() {
        //TODO
        return Stream.empty();
    }

    @Override
    public void onChanged(Runnable listener) {
        //TODO
    }
}
