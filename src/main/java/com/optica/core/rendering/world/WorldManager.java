// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world;

import com.optica.core.rendering.world.tree.nodes.ChunkNode;

public interface WorldManager {
    void addChunk(ChunkNode chunk);

    void removeChunk(ChunkNode chunk);

    void queueUpload(int depth, Runnable job);
}
