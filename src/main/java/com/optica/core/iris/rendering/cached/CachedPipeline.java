package com.optica.core.iris.rendering.cached;

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
 * Optica's cached lighting mode. Lighting (BASIC-style direct light and sky GI) is computed for points
 * on block faces in world space and stored in a GPU hash table ({@link SurfaceCache}). Every frame:
 * <ol>
 *   <li>c0 (per pixel): find the cache entries around each pixel, create missing ones.</li>
 *   <li>c1 (fixed size): compute new entries, then refresh a slice of the table so every entry is
 *       recomputed once per {@code cacheRefreshSeconds}.</li>
 *   <li>c2 (per pixel): interpolate the cached lighting (into {@code sharp_direct}, so the pack reads it
 *       through the usual BASIC samplers), fill in what is missing, and blend with the previous frames.</li>
 *   <li>c3: hand the cached GI to the pack's {@code write_indirect()}.</li>
 * </ol>
 * The per-pixel passes only read memory, so the frame cost barely depends on the number of lights.
 */
public class CachedPipeline extends PhotonicsPipeline {
    /** Size of the update pass: the most entries computed in one frame. Must match cache.glsl. */
    public static final int UPDATE_WIDTH = 512;
    public static final int UPDATE_HEIGHT = 192;

    public CachedPipeline(
            PhotonicsProperties phProperties,
            CachedProperties cachedProperties,
            AtlasDownloader atlasDownloader,
            HandheldItemSupplier handheldItemSupplier,
            IrisPipeline irisPipeline
    ) {
        super(phProperties, atlasDownloader, irisPipeline);

        var cache = registerComponent(new SurfaceCache(cachedProperties.getCapacityLog2()));
        // Recompute cached lighting where blocks or lights changed, as soon as the change reaches the GPU.
        worldCompiler.setSectionUploadListener(cache::onSectionsUploaded);
        lightList.setLightsChangedListener(cache::onLightsChanged);

        Pipelines.fragData(this, phProperties, irisPipeline);
        Pipelines.handheldLighting(this, handheldItemSupplier, phProperties, irisPipeline);

        cachePipeline(phProperties, cachedProperties, irisPipeline);
    }

    private void cachePipeline(PhotonicsProperties phProperties, CachedProperties cachedProperties, IrisPipeline irisPipeline) {
        var slots = irisPipeline.newFramebuffer(phProperties.getRenderScale())
                .addAttachment("cache_slots", ITextureFormat.rgba32ui(), CREATE_SAMPLER)
                .build(this::registerComponent);

        var update = irisPipeline.newFramebuffer(UPDATE_WIDTH, UPDATE_HEIGHT)
                .addAttachment("cache_update_scratch", ITextureFormat.r8(), 0)
                .build(this::registerComponent);

        var resolved = irisPipeline.newFramebuffer(phProperties.getRenderScale())
                .addAttachment("sharp_direct", ITextureFormat.rgba16f(), CREATE_SAMPLER | FLIP)
                .addAttachment("cached_indirect", ITextureFormat.rgba16f(), CREATE_SAMPLER | FLIP)
                .build(this::registerComponent);

        boolean writeIndirect = phProperties.getGiProperties().isEnabled()
                && cachedProperties.getGiSamples() > 0
                && !cachedProperties.isCombinedGi();

        var renderer = irisPipeline.newRenderer()
                .debugGroup("cached lighting")
                .withFragmentPrefix("/photonics/rendering/cached/passes/")
                .withFramebuffer(slots)
                .deferredPass("cache request", "c0_request.fsh", null)
                .withFramebuffer(update)
                .deferredPass("cache update", "c1_update.fsh", null)
                .withFramebuffer(resolved)
                // Flip first: this frame writes sharp_direct (what the pack samples) while last frame's
                // result stays readable as prev_sharp_direct for the history blend.
                .thenFlip(resolved)
                .deferredPass("cache resolve", "c2_resolve.fsh", null);

        if (writeIndirect) {
            renderer = renderer
                    .withFramebuffer(null) // the pack's framebuffer, from write_indirect's RENDERTARGETS
                    .deferredPass("write indirect", "c3_write_indirect.fsh", null);
        }

        renderer.build(this::registerRenderer);
    }
}
