// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.rendering.sharp;

import com.optica.api.gpu.textures.ITextureFormat;
import com.optica.core.iris.pipeline.IrisPipeline;
import com.optica.core.iris.properties.PhotonicsProperties;
import com.optica.core.iris.rendering.PhotonicsPipeline;
import com.optica.core.iris.rendering.Pipelines;
import com.optica.core.rendering.lights.HandheldItemSupplier;
import com.optica.core.rendering.world.bakery.texture.AtlasDownloader;

import static com.optica.core.iris.pipeline.texture.AttachmentUsage.CREATE_SAMPLER;
import static com.optica.core.iris.pipeline.texture.AttachmentUsage.FLIP;

/**
 * The BASIC ("sharp") lighting mode: every fragment evaluates up to {@code PH_MAX_SAMPLES} of the
 * brightest lights that can reach it (found through the light bins) and traces a shadow ray to each.
 * Noise-free, but limited in how many lights affect one spot. Results are reused across frames and
 * refreshed on a rotating schedule, as 0.3.x did, which keeps the per-frame shadow ray count low.
 * Photonics 0.4-dev left this mode as a
 * stub; Optica implements it because shader packs written for 0.3.x (e.g. Euphoria Patches) default
 * to it.
 */
public class SharpPipeline extends PhotonicsPipeline {
    public SharpPipeline(
            PhotonicsProperties phProperties,
            SharpProperties sharpProperties,
            AtlasDownloader atlasDownloader,
            HandheldItemSupplier handheldItemSupplier,
            IrisPipeline irisPipeline
    ) {
        super(phProperties, atlasDownloader, irisPipeline);

        Pipelines.fragData(this, phProperties, irisPipeline);
        Pipelines.handheldLighting(this, handheldItemSupplier, phProperties, irisPipeline);

        directPipeline(irisPipeline);
        Pipelines.legacyIndirect(this, phProperties, irisPipeline);
    }

    private void directPipeline(IrisPipeline irisPipeline) {
        if (!properties.getBlockLightProperties().isEnabled()) return;

        var framebuffer = irisPipeline.newFramebuffer(properties.getRenderScale())
                .addAttachment("sharp_direct", ITextureFormat.rgba16f(), CREATE_SAMPLER | FLIP)
                .build(this::registerComponent);

        irisPipeline.newRenderer()
                .debugGroup("sharp direct")
                .withFragmentPrefix("/photonics/rendering/sharp/passes/")
                .withFramebuffer(framebuffer)
                // Flip first: this frame writes sharp_direct (what the pack samples) while last frame's
                // result stays readable as prev_sharp_direct. Flipping after the pass would leave the
                // pack reading last frame's light, which trails behind the camera.
                .thenFlip(framebuffer)
                .deferredPass("sharp direct", "s0_direct.fsh", null)
                .build(this::registerRenderer);
    }
}
