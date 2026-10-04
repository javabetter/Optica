// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.rendering;

import com.optica.api.gpu.textures.ITextureFormat;
import com.optica.core.iris.pipeline.IrisPipeline;
import com.optica.core.iris.properties.PhotonicsProperties;
import com.optica.core.rendering.HandheldLightComponent;
import com.optica.core.rendering.lights.HandheldItemSupplier;

import static com.optica.core.iris.pipeline.texture.AttachmentUsage.CREATE_SAMPLER;
import static com.optica.core.iris.pipeline.texture.AttachmentUsage.FLIP;

public class Pipelines {
    public static String DEFAULT_VERTEX_SHADER = "/photonics/rendering/shared/screen.vsh";

    private Pipelines() {

    }

    public static void fragData(PhotonicsPipeline ext, PhotonicsProperties properties, IrisPipeline irisPipeline) {
        var framebuffer = irisPipeline.newFramebuffer(properties.getRenderScale())
                .addAttachment("frag_data0", ITextureFormat.rgba32f(), CREATE_SAMPLER | FLIP)
                .addAttachment("frag_data1", ITextureFormat.rgba32ui(), CREATE_SAMPLER | FLIP)
                .addAttachment("fast_frag_data", ITextureFormat.rg32f(), CREATE_SAMPLER | FLIP)
                .build(ext::registerComponent);

        irisPipeline.newRenderer()
                .debugGroup("frag data")
                .withFragmentPrefix("/photonics/rendering/frag/passes/")
                .withFramebuffer(framebuffer)
                .thenFlip(framebuffer)
                .deferredPass("frag data", "f0_load_frag.fsh", null)
                .build(ext::registerRenderer);
    }

    public static void handheldLighting(
            PhotonicsPipeline ext,
            HandheldItemSupplier handheldItemSupplier,
            PhotonicsProperties properties,
            IrisPipeline irisPipeline
    ) {
        if (!properties.getHandheldProperties().isEnabled()) return;

        var handheldComponent = ext.registerComponent(new HandheldLightComponent(handheldItemSupplier, properties));

        var framebuffer = irisPipeline.newFramebuffer(properties.getRenderScale())
                .addAttachment("handheld_diffuse", ITextureFormat.rgb32f(), CREATE_SAMPLER)
                .build(ext::registerComponent);

        var pipeline = irisPipeline.newRenderer()
                .debugGroup("handheld")
                .withFragmentPrefix("/photonics/rendering/shared/")
                .withFramebuffer(framebuffer)
                .deferredPass("handheld", "h0_handheld.fsh", null)
                .build();

        ext.registerRenderer(() -> {
            if (handheldComponent.hasItem())
                pipeline.renderAll();
        });
    }

    /**
     * Optica: Photonics 0.3.x-style GI for packs that consume it through write_indirect() (e.g. Euphoria
     * Patches with combined GI off). One GI path per fragment, accumulated over time, then filtered and
     * handed to the pack's write_indirect(), which renders into the pack's own framebuffer.
     */
    public static void legacyIndirect(PhotonicsPipeline ext, PhotonicsProperties properties, IrisPipeline irisPipeline) {
        if (!properties.getGiProperties().isEnabled()) return;

        var framebuffer = irisPipeline.newFramebuffer(properties.getRenderScale())
                .addAttachment("legacy_gi", ITextureFormat.rgba16f(), CREATE_SAMPLER | FLIP)
                .build(ext::registerComponent);

        irisPipeline.newRenderer()
                .debugGroup("legacy gi")
                .withFragmentPrefix("/photonics/rendering/legacy/passes/")
                .withFramebuffer(framebuffer)
                .thenFlip(framebuffer) // before the pass, so lg1 reads this frame's legacy_gi
                .deferredPass("legacy gi", "lg0_indirect.fsh", null)
                .withFramebuffer(writeIndirectFramebuffer(ext, properties, irisPipeline))
                .deferredPass("write indirect", "lg1_write_indirect.fsh", null)
                .build(ext::registerRenderer);
    }

    /**
     * Optica: the framebuffer for passes that call the pack's write_indirect(). Null (the pack's own
     * framebuffer, from write_indirect's RENDERTARGETS) unless the pack stores the GI in an image itself;
     * then a full-size framebuffer the pass does not write, so the pass covers the whole screen.
     */
    public static @org.jetbrains.annotations.Nullable com.optica.core.iris.pipeline.texture.IrisFramebuffer writeIndirectFramebuffer(
            PhotonicsPipeline ext,
            PhotonicsProperties properties,
            IrisPipeline irisPipeline
    ) {
        boolean usesImage = com.optica.core.iris.IrisManager.getShaderPatcher()
                .map(patcher -> patcher.writeIndirectUsesImage())
                .orElse(false);
        if (!usesImage) return null;

        return irisPipeline.newFramebuffer(properties.getRenderScale())
                .addAttachment("write_indirect_target", ITextureFormat.r8(), 0)
                .build(ext::registerComponent);
    }

    public static void exposureHistory(PhotonicsPipeline ext, IrisPipeline irisPipeline) {
        var framebuffer = irisPipeline.newFramebuffer(1, 1)
                .addAttachment("prev_exposure", ITextureFormat.r32f(), CREATE_SAMPLER)
                .build(ext::registerComponent);

        irisPipeline.newRenderer()
                .debugGroup("exposure")
                .withFragmentPrefix("/photonics/rendering/frag/passes/")
                .withFramebuffer(framebuffer)
                .thenFlip(framebuffer)
                .deferredPass("record exposure", "e0_record_exposure.fsh", null)
                .build(ext::registerRenderer);
    }
}
