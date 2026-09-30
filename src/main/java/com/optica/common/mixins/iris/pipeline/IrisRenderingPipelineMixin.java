// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris.pipeline;

import com.optica.common.iris.IrisUtil;
import com.optica.common.iris.pipeline.IrisRenderingPipelineExt;
import com.optica.common.iris.buffers.GlBufferHolder;
import com.optica.common.mixins.iris.ShaderPackAccessor;
import com.optica.core.iris.IrisManager;
import com.optica.core.iris.rendering.PhotonicsPipeline;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.image.GlImage;
import net.irisshaders.iris.pathways.CenterDepthSampler;
import net.irisshaders.iris.pipeline.CustomTextureManager;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.programs.ComputeSource;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.targets.BufferFlipper;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(IrisRenderingPipeline.class)
public abstract class IrisRenderingPipelineMixin implements IrisRenderingPipelineExt {
    @Shadow
    private WorldRenderingPhase phase;

    @Shadow
    @Final
    private RenderTargets renderTargets;

    @Shadow
    private ShaderStorageBufferHolder shaderStorageBufferHolder;

    @Shadow
    @Final
    private CustomTextureManager customTextureManager;

    @Shadow
    @Final
    private FrameUpdateNotifier updateNotifier;

    @Shadow
    @Final
    private CenterDepthSampler centerDepthSampler;

    @Shadow
    @Final
    private Supplier<ShadowRenderTargets> shadowTargetsSupplier;

    @Shadow
    @Final
    private Set<GlImage> customImages;

    @Shadow
    @Final
    private CustomUniforms customUniforms;


    @Unique
    private GlBufferHolder bufferHolder;
    @Unique
    private List<com.optica.common.iris.pipeline.renderer.PhotonicsRenderer> phRenderers;
    @Unique
    private int phGeneration;

    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/pipeline/CompositeRenderer;<init>(Lnet/irisshaders/iris/pipeline/WorldRenderingPipeline;Lnet/irisshaders/iris/pipeline/CompositePass;Lnet/irisshaders/iris/shaderpack/properties/PackDirectives;[Lnet/irisshaders/iris/shaderpack/programs/ProgramSource;[[Lnet/irisshaders/iris/shaderpack/programs/ComputeSource;Lnet/irisshaders/iris/targets/RenderTargets;Lnet/irisshaders/iris/gl/buffer/ShaderStorageBufferHolder;Lnet/irisshaders/iris/gl/texture/TextureAccess;Lnet/irisshaders/iris/uniforms/FrameUpdateNotifier;Lnet/irisshaders/iris/pathways/CenterDepthSampler;Lnet/irisshaders/iris/targets/BufferFlipper;Ljava/util/function/Supplier;Lnet/irisshaders/iris/shaderpack/texture/TextureStage;Lit/unimi/dsi/fastutil/objects/Object2ObjectMap;Lit/unimi/dsi/fastutil/objects/Object2ObjectMap;Ljava/util/Set;Lcom/google/common/collect/ImmutableMap;Lnet/irisshaders/iris/uniforms/custom/CustomUniforms;)V",
                    ordinal = 0
            )
    )
    private void init(ProgramSet programSet, CallbackInfo ci, @Local BufferFlipper flipper) {
        bufferHolder = new GlBufferHolder();
        phRenderers = List.of();
        phGeneration = IrisManager.getGeneration();

        IrisManager.registerBuffers(bufferHolder);

        var renderers = IrisUtil.getPipelineManager().getRenderers();
        var phRenderers = ImmutableList.<com.optica.common.iris.pipeline.renderer.PhotonicsRenderer>builder();

        for (var renderer : renderers) {
            var passes = renderer.getPasses();

            var compositeSources = new ProgramSource[passes.size()];
            var computeSources = new ComputeSource[passes.size()][];

            for (int i = 0; i < passes.size(); i++) {
                var pass = passes.get(i);
                compositeSources[i] = new ProgramSource(
                        pass.hasFragmentShader() ? cleanUpFragmentName(pass.fragmentShader()) : pass.name(),
                        readSource(pass.vertexShader()),
                        null,
                        null,
                        null,
                        readSource(pass.fragmentShader()),
                        programSet,
                        null,
                        null
                );

                //TODO: For future use
                computeSources[i] = new ComputeSource[0];
            }

            phRenderers.add(
                    new com.optica.common.iris.pipeline.renderer.PhotonicsRenderer(
                            renderer.name(),
                            (IrisRenderingPipeline) (Object) this,
                            programSet.getPackDirectives(),
                            compositeSources,
                            computeSources,
                            renderTargets,
                            shaderStorageBufferHolder,
                            customTextureManager.getNoiseTexture(),
                            updateNotifier,
                            centerDepthSampler,
                            flipper,
                            shadowTargetsSupplier,
                            customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.DEFERRED, Object2ObjectMaps.emptyMap()),
                            customTextureManager.getIrisCustomTextures(),
                            customImages,
                            customUniforms,
                            passes
                    )
            );
        }

        this.phRenderers = phRenderers.build();
    }

    @Inject(
            method = "beginTranslucents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/pipeline/CompositeRenderer;renderAll()V"
            )
    )
    public void beginTranslucents(CallbackInfo ci) {
        IrisManager.onRender();
    }

    @Override
    public GlBufferHolder photonics$bufferHolder() {
        return bufferHolder;
    }

    @Override
    public void onSelect() {
        // Optica: this Iris pipeline was built against a Photonics pipeline that has since been replaced
        // or destroyed. Its passes point at freed resources, so run without them and have the pipeline
        // manager rebuild everything before the next selection (never in the middle of this one).
        if (phGeneration != IrisManager.getGeneration() && !phRenderers.isEmpty()) {
            IrisUtil.getPipelineManager().setRenderers(null);
            IrisUtil.getPipelineManager().requestRebuild();
            return;
        }

        IrisUtil.getPipelineManager().setRenderers(phRenderers);
    }

    @Unique
    private static String readSource(@Nullable String fileName) {
        if (fileName == null) return null;

        ShaderPack shaderPack = Iris.getCurrentPack().orElse(null);
        if (shaderPack == null)
            return null;

        AbsolutePackPath path = AbsolutePackPath.fromAbsolutePath(fileName.startsWith("/") ? fileName : "/" + fileName);
        return ((ShaderPackAccessor) shaderPack).getSourceProvider().apply(path);
    }

    @Unique
    private static String cleanUpFragmentName(String fragment) {
        fragment = fragment.substring(fragment.lastIndexOf("/") + 1);
        fragment = fragment.substring(0, fragment.lastIndexOf("."));

        return fragment;
    }

}
