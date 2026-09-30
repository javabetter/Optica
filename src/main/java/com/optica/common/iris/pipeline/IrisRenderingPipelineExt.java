// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris.pipeline;

import com.optica.common.iris.buffers.GlBufferHolder;

public interface IrisRenderingPipelineExt {
    GlBufferHolder photonics$bufferHolder();

    void onSelect();
}
