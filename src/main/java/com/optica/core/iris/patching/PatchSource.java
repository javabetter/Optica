// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.patching;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface PatchSource {
    Stream<Path> streamPatches();

    void onChanged(Runnable listener);
}
