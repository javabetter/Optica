// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris.pipeline;

import com.optica.core.iris.IrisManager;
import com.optica.core.iris.IrisPack;
import com.optica.common.AtlasDownloaderImpl;
import com.optica.common.HandheldLightSupplierImpl;
import com.optica.common.iris.IrisPackLightsImpl;
import com.optica.common.iris.pipeline.IrisPipelineImpl;
import com.optica.common.iris.pipeline.IrisRenderingPipelineExt;
import com.optica.common.iris.pipeline.PipelineManagerExt;
import com.optica.common.iris.pipeline.renderer.DeferredIrisRenderer;
import com.optica.common.meshing.MinecraftBlockMesher;
import com.optica.common.mixins.iris.ShaderPackAccessor;
import com.optica.core.Photonics;
import com.optica.core.config.PhConfig;
import com.optica.core.config.lights.LightsProvider;
import com.optica.core.iris.AbstractIrisPackLights;
import com.optica.core.iris.rendering.PhotonicsPipeline;
import com.optica.core.rendering.world.bakery.BlockMesher;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.PipelineManager;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(PipelineManager.class)
public abstract class PipelineManagerMixin implements PipelineManagerExt {
    @Shadow
    private WorldRenderingPipeline pipeline;

    @Shadow
    @Final
    private Map<NamespacedId, WorldRenderingPipeline> pipelinesPerDimension;

    @Unique
    private @Nullable LightsProvider lightsProvider;

    @Shadow
    private int versionCounterForSodiumShaderReload;

    @Shadow
    private void resetTextureState() {
        throw new AssertionError();
    }

    @Unique
    private final List<DeferredIrisRenderer> renderers = new ArrayList<>();

    // Optica: the level the Photonics pipeline was built for, and a pending request to rebuild everything.
    @Unique
    private @Nullable Object ph$level;
    @Unique
    private boolean ph$rebuildRequested;
    @Unique
    private long ph$lastRequestedRebuild;


    @Inject(method = "preparePipeline", at = @At("HEAD"))
    private void preparePipeline(NamespacedId currentDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        // Optica: the Photonics pipeline holds a voxel copy of one level. Stock Iris destroys all
        // pipelines when the level changes, but mods can prevent that (seen on Hypixel), which left a
        // stale Photonics pipeline under Iris pipelines kept alive. Tear everything down here instead,
        // at the same point Iris would (right before selecting a pipeline).
        Object level = net.minecraft.client.Minecraft.getInstance().level;
        boolean levelChanged = IrisManager.hasPipeline() && level != null && level != ph$level;

        if (levelChanged || ph$rebuildRequested) {
            ph$rebuildRequested = false;
            ph$destroyAll();
        }

        if (IrisManager.hasPipeline()) return;

        // Optica: Iris calls preparePipeline every frame and only builds a pipeline when the dimension
        // has none. Without this check, a pack with Photonics disabled (no Photonics pipeline) re-parsed
        // its lights and registered another light provider every frame.
        if (pipelinesPerDimension.containsKey(currentDimension)) return;

        //TODO Add to more sensible spot
        BlockMesher.REGISTRY.addDefault(new MinecraftBlockMesher());

        var shaderPack = (IrisPack) Iris.getCurrentPack().orElse(null);
        if (shaderPack == null) return;

        // Optica: renderers of a Photonics pipeline that was destroyed without Iris' knowledge.
        clearRenderers();
        renderers.clear();

        if (lightsProvider != null) PhConfig.removeLightProvider(lightsProvider);

        lightsProvider = readLightsProvider(shaderPack);
        if (lightsProvider != null)
            PhConfig.registerLightProvider(lightsProvider);

        IrisManager.setupPipeline(
                AtlasDownloaderImpl::new,
                HandheldLightSupplierImpl::new,
                new IrisPipelineImpl(renderers)
        );
        ph$level = level;
    }

    /** Destroys every Iris pipeline and the Photonics pipeline, like {@code destroyPipeline} without its hooks. */
    @Unique
    private void ph$destroyAll() {
        pipelinesPerDimension.forEach((dimension, irisPipeline) -> {
            resetTextureState();
            irisPipeline.destroy();
        });
        pipelinesPerDimension.clear();
        pipeline = null;
        versionCounterForSodiumShaderReload++;

        ph$destroyPhotonics();
    }

    @Override
    public void requestRebuild() {
        // At most once per 10 s, so something that keeps replacing the Photonics pipeline cannot cause a
        // rebuild loop; Photonics then stays off for that Iris pipeline instead.
        long now = System.currentTimeMillis();
        if (now - ph$lastRequestedRebuild < 10_000) return;

        ph$lastRequestedRebuild = now;
        ph$rebuildRequested = true;
        Photonics.LOGGER.info("Iris pipeline is out of sync with Photonics; rebuilding pipelines");
    }

    @Unique
    private @Nullable LightsProvider readLightsProvider(IrisPack pack) {
        var contents = ((ShaderPackAccessor) pack).getSourceProvider()
                .apply(AbsolutePackPath.fromAbsolutePath("/ph_lights.json"));

        if (contents == null)
            return null;

        try {
            var lights = AbstractIrisPackLights.parse(contents, IrisPackLightsImpl.class);
            lights.setShaderPack((ShaderPack) pack);

            return lights;
        } catch (Exception e) {
            Photonics.LOGGER.error("Error while parsing ph_lights.json for {}", pack.ph$name(), e);
            return null;
        }
    }

    @Inject(method = "preparePipeline", at = @At("TAIL"))
    private void selectPipeline(NamespacedId currentDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        if (pipeline instanceof IrisRenderingPipeline ext)
            ((IrisRenderingPipelineExt) ext).onSelect();
        else
            clearRenderers();
    }

    @Override
    public List<DeferredIrisRenderer> getRenderers() {
        return renderers;
    }

    @Unique
    private void clearRenderers() {
        for (var renderer : renderers)
            renderer.setActive(null);
    }

    @Override
    public void setRenderers(@Nullable List<com.optica.common.iris.pipeline.renderer.PhotonicsRenderer> activeRenderers) {
        if (activeRenderers == null) {
            clearRenderers();
            return;
        }

        if (activeRenderers.size() != renderers.size()) {
            // Optica: the Iris pipeline was built for a different Photonics pipeline. Render without
            // Photonics instead of crashing; IrisRenderingPipeline.onSelect schedules a reload.
            Photonics.LOGGER.warn("Iris pipeline does not match the Photonics pipeline ({} vs {} renderers)", activeRenderers.size(), renderers.size());
            clearRenderers();
            return;
        }

        for (int i = 0; i < activeRenderers.size(); i++)
            renderers.get(i).setActive(activeRenderers.get(i));
    }

    @Inject(method = "destroyPipeline", at = @At("HEAD"))
    private void destroyEverything(CallbackInfo ci) {
        ph$destroyPhotonics();
    }

    @Unique
    private void ph$destroyPhotonics() {
        if (IrisManager.hasPipeline()) {
            IrisManager.destroyEverything();
            renderers.clear();
        }

        if (lightsProvider != null) {
            PhConfig.removeLightProvider(lightsProvider);
            lightsProvider = null;
        }
    }
}
