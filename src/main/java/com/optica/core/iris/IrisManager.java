// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris;

import com.optica.core.Photonics;
import com.optica.core.config.OpticaSettings;
import com.optica.core.iris.patching.ShaderPatcher;
import com.optica.core.iris.pipeline.DefineHolder;
import com.optica.core.iris.pipeline.IrisPipeline;
import com.optica.core.iris.pipeline.buffer.IBufferHolder;
import com.optica.core.iris.pipeline.texture.ISamplerHolder;
import com.optica.core.iris.pipeline.uniform.IDynamicUniformHolder;
import com.optica.core.iris.pipeline.uniform.IUniformHolder;
import com.optica.core.iris.properties.PhotonicsProperties;
import com.optica.core.iris.properties.impl.PropertiesManager;
import com.optica.core.iris.rendering.PhotonicsPipeline;
import com.optica.core.iris.rendering.PhotonicsRenderer;
import com.optica.core.rendering.lights.HandheldItemSupplier;
import com.optica.core.rendering.world.bakery.texture.AtlasDownloader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

public class IrisManager {
    private static final PropertiesManager propertiesManager = new PropertiesManager();

    private static @Nullable ShaderPatcher activePatcher = null;
    private static @Nullable PhotonicsProperties activeProperties = null;
    private static @Nullable PhotonicsPipeline activePipeline = null;

    // Optica: the properties the active setup was built from, and a counter that changes whenever the
    // Photonics pipeline is created or destroyed. Iris pipelines remember the generation they were built
    // against, so a cached Iris pipeline that outlived its Photonics pipeline can be detected.
    private static @Nullable Properties activeRawProperties = null;
    private static int generation = 0;

    public static int getGeneration() {
        return generation;
    }

    public static Optional<ShaderPatcher> getShaderPatcher() {
        return  Optional.ofNullable(activePatcher);
    }

    public static ShaderPatcher getShaderPatcherOrThrow() {
        return getShaderPatcher().orElseThrow();
    }

    public static Optional<PhotonicsProperties> getProperties() {
        return Optional.ofNullable(activeProperties);
    }

    public static PhotonicsProperties getPropertiesOrThrow() {
        return getProperties().orElseThrow();
    }

    public static boolean hasPipeline() {
        return activePipeline != null;
    }

    public static void setupShaderPatcher(@NonNls IrisPack pack, boolean patchEnabled) {
        Objects.requireNonNull(pack, "pack");

        activePatcher = new ShaderPatcher(pack);
        propertiesManager.setForceEnabled(!pack.ph$supportsPhotonics() && activePatcher.hasPatch() && patchEnabled);
    }

    public static void setupProperties(@NonNls Properties properties, @NonNls Logger logger) {
        Objects.requireNonNull(properties, "properties");
        Objects.requireNonNull(logger, "logger");

        // Optica: ShaderProperties is also constructed outside of shader reloads (e.g. by other mods that
        // read the pack). Parsing properties must never tear down the running pipeline underneath Iris: a
        // real change of shader settings goes through an Iris reload, which destroys the pipelines (and
        // with them the Photonics pipeline) anyway. The new properties apply to the next pipeline.
        // Optica: apply config/optica.properties (e.g. the cached lighting mode) on top of the pack's own
        // properties. Re-read on every load, so a shader reload picks up changes to that file.
        properties = OpticaSettings.load().applyTo(properties);

        if (activeProperties != null && properties.equals(activeRawProperties)) return;
        activeRawProperties = (Properties) properties.clone();

        propertiesManager.setProperties(properties, logger);

        activeProperties = propertiesManager.getProperties(PhotonicsProperties.class);
    }
    
    public static void setupPipeline(
            @NonNls Supplier<AtlasDownloader> atlasDownloaderSupplier,
            @NonNls Supplier<HandheldItemSupplier> handheldItemSupplierSupplier,
            @NonNls IrisPipeline irisPipeline
    ) {
        Objects.requireNonNull(atlasDownloaderSupplier, "atlasDownloaderSupplier");
        Objects.requireNonNull(handheldItemSupplierSupplier, "handheldItemSupplierSupplier");
        Objects.requireNonNull(irisPipeline, "irisPipeline");

        if (activeProperties == null) throw new IllegalStateException("The renderer has not been set up");
        if (activePipeline != null) throw new IllegalStateException("Pipeline has already been created");
        
        activePipeline = PhotonicsRenderer.createPipeline(
                propertiesManager,
                atlasDownloaderSupplier,
                handheldItemSupplierSupplier,
                irisPipeline
        );
        generation++;
    }

    public static void onRender() {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.onRender();
    }

    public static void onFrameBegin() {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.onFrameBegin();
    }

    public static void onSectionAdded(int x, int y, int z) {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.onSectionAdded(x, y, z);
    }

    public static void onSectionChanged(int x, int y, int z) {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.onSectionChanged(x, y, z);
    }

    public static void registerVersionDefines(DefineHolder defines) {
        defines.stringDefine("PHOTONICS", "");
        defines.stringDefine("PHOTONICS_VERSION", Photonics.getVersionString());
    }

    public static void registerDefines(DefineHolder defines) {
        propertiesManager.registerDefines(defines);
    }

    public static void registerUniforms(IUniformHolder uniforms) {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.registerUniforms(uniforms);
    }

    public static void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms) {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.registerDynamicUniforms(dynamicUniforms);
    }

    public static void registerBuffers(IBufferHolder buffers) {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.registerBuffers(buffers);
    }

    public static void registerCustomTextures(ISamplerHolder samplers) {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.registerCustomTextures(samplers);
    }

    public static void destroyEverything() {
        destroyEverything(true);
    }

    private static void destroyEverything(boolean destroyPatcher) {
        var pipeline = activePipeline;
        if (pipeline == null) return;

        activePatcher = destroyPatcher ? null : activePatcher;
        activePipeline = null;
        generation++;
        pipeline.close();
    }

    private IrisManager() {
    }
}
