// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris.pipeline.renderer;

import com.optica.common.iris.pipeline.CompositeRendererPassExt;
import com.optica.core.rendering.GpuPassTimer;
import com.optica.common.mixins.iris.pipeline.passes.composite.CompositeRendererAccessor;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.image.GlImage;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.pathways.CenterDepthSampler;
import net.irisshaders.iris.pipeline.CompositePass;
import net.irisshaders.iris.pipeline.CompositeRenderer;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ComputeSource;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.targets.BufferFlipper;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class PhotonicsRenderer extends CompositeRenderer {
    private final String name;

    public PhotonicsRenderer(
            String name,
            WorldRenderingPipeline pipeline,
            PackDirectives packDirectives,
            ProgramSource[] sources,
            ComputeSource[][] computes,
            RenderTargets renderTargets,
            ShaderStorageBufferHolder holder,
            TextureAccess noiseTexture,
            FrameUpdateNotifier updateNotifier,
            CenterDepthSampler centerDepthSampler,
            BufferFlipper bufferFlipper,
            Supplier<ShadowRenderTargets> shadowTargetsSupplier,
            Object2ObjectMap<String, TextureAccess> customTextureIds,
            Object2ObjectMap<String, TextureAccess> irisCustomTextures,
            Set<GlImage> customImages,
            CustomUniforms customUniforms,
            List<DeferredIrisRenderer.Pass> passes
    ) {
        super(
                pipeline,
                CompositePass.DEFERRED,
                packDirectives,
                sources,
                computes,
                renderTargets,
                holder,
                noiseTexture,
                updateNotifier,
                centerDepthSampler,
                bufferFlipper,
                shadowTargetsSupplier,
                TextureStage.DEFERRED,
                customTextureIds,
                irisCustomTextures,
                customImages,
                ImmutableMap.of(),
                customUniforms
        );

        this.name = name;
        for (CompositeRendererPassExt pass : getPasses()) {
            var definition = passes.get(pass.getIndex());

            pass.setDebugName(definition.name());
            pass.setActions(definition.actions());
            pass.setFramebuffer(definition.framebuffer());
        }
    }

    public String getName() {
        return name;
    }

    private List<CompositeRendererPassExt> getPasses() {
        return ((CompositeRendererAccessor) this).getPasses();
    }

    // Optica: Iris only resizes the renderers it owns, so this one checks the window size itself.
    private int lastWidth = -1;
    private int lastHeight = -1;

    @Override
    public void renderAll() {
        var window = Minecraft.getInstance().getWindow();
        var main = Minecraft.getInstance().getMainRenderTarget();
        int width = window.getWidth() ^ (main.width << 16);
        int height = window.getHeight() ^ (main.height << 16);

        if (width != lastWidth || height != lastHeight) {
            lastWidth = width;
            lastHeight = height;
            recalculateSizes();
        }

        GpuPassTimer.beginGroup(name);
        super.renderAll();
        GpuPassTimer.endGroup();
    }

    @Override
    public void recalculateSizes() {
        // Passes writing into the pack's render targets: Iris recreated those targets, so rebuild the
        // framebuffers and viewports that point at them.
        super.recalculateSizes();

        // Passes writing into Optica's own framebuffers: resize them and follow their size.
        for (CompositeRendererPassExt pass : getPasses())
            pass.updateSize();
    }
}
