// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.patching;

import com.optica.api.ModLoader;
import com.optica.core.iris.IrisManager;
import com.optica.core.iris.IrisPackPath;
import com.optica.core.iris.IrisPack;
import com.optica.core.Photonics;
import com.optica.core.iris.patching.sources.DevEnvSource;
import com.optica.core.iris.patching.sources.JarSource;
import com.optica.core.iris.patching.sources.ShaderPatchesSource;
import com.optica.core.util.Fs;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class ShaderPatcher {
    // TODO replace with comment at the top of the file
    private static final Set<String> AUTO_REPLACED_FILES = Set.of(
            "light.glsl",
            "light_list.glsl",
            "palette.glsl",
            "samplers.glsl",
            "tracing.glsl",
            "uniforms.glsl",

            // LEGACY FILE NAMES
            "photonics.glsl",
            "ph_samplers.glsl"
    );
    private static final Path PATCHED_DEBUG_PATH = ModLoader.getGameDir().resolve(".ph-patched-shaders");
    private static final Path PHOTONICS_SHADERS_PATH = getPhotonicsShadersPath();

    private final IrisPack pack;
    private final @Nullable Patch patch;

    public ShaderPatcher(IrisPack pack) {
        this.pack = pack;

        if (pack.ph$supportsPhotonics()) {
            patch = null;
            return;
        }

        wipeDebug();
        patch = getPatchList().loadPatch(pack).orElse(null);
    }

    public boolean hasPatch() {
        return patch != null;
    }

    public List<IrisPackPath> getCreatedFiles() {
        List<IrisPackPath> createdFiles = new ArrayList<>();

        if (patch != null)
            createdFiles.addAll(patch.getFiles());

        Path includedShaders = PHOTONICS_SHADERS_PATH
                .resolve("photonics");

        // Optica: the settings file adds Optica's options to the pack's settings menu; only for packs
        // Optica runs with.
        boolean addSettings = pack.ph$supportsPhotonics() || patch != null;

        try (Stream<Path> shaders = Files.walk(includedShaders)) {
            shaders.forEach(file -> {
                if (Files.isDirectory(file)) return;
                if (!addSettings && file.getFileName().toString().equals("optica_settings.glsl")) return;

                var relativePath = includedShaders.relativize(file);
                createdFiles.add(
                        IrisPackPath.fromAbsolutePath("/photonics")
                                .ph$resolve(relativePath.toString().replace('\\', '/'))
                );
            });
        } catch (IOException e) {
            Photonics.LOGGER.error("An error occurred loading Photonics's shaders", e);
        }

        return createdFiles;
    }

    private static Path getPhotonicsShadersPath() {
        if (Photonics.isDevEnvironment()) {
            // Dev runs happen in <project>/run, so read shaders straight from the source tree to allow hot reloading.
            return ModLoader.getGameDir().resolve("../src/main/resources/assets/optica/shaders")
                    .normalize();
        }

        return Photonics.getAssetsPath()
                .resolve("optica")
                .resolve("shaders")
                .normalize();
    }

    public @Nullable String readPhotonicsFile(
            IrisPackPath packPath,
            Function<IrisPackPath, @Nullable String> shaderSourceSupplier
    ) {
        Path realPath = packPath.ph$resolved(PHOTONICS_SHADERS_PATH);
        Path relativePath = PHOTONICS_SHADERS_PATH.resolve("photonics")
                .relativize(realPath);

        @Nullable String source = null;

        readFile:
        {
            // If no patch is applied check if the shader has a replacement
            if (patch == null && !AUTO_REPLACED_FILES.contains(relativePath.toString())) {
                source = shaderSourceSupplier.apply(packPath);
                if (source != null) break readFile;
            }

            // Try loading shader from assets in jar
            if (Files.exists(realPath)) {
                source = Fs.tryReadString(realPath).orElse(null);
                if (source != null) break readFile;
            }
        }

        if (patch == null) return source;
        @Nullable String loadedSource = source;

        source = patch.applyPatches(
                packPath,
                p -> {
                    if (p.equals(packPath))
                        return loadedSource;

                    return shaderSourceSupplier.apply(p);
                },
                IrisManager.getPropertiesOrThrow().isEnabled()
        );

        return source;
    }

    /**
     * Reads a potentially patched shader file, also responsible for loading Photonics' built in shader files.
     */
    public @Nullable String readShaderFile(
            IrisPackPath path,
            Function<IrisPackPath, @Nullable String> shaderSourceSupplier
    ) {
        if (path.ph$startsWith("/photonics")) return readPhotonicsFile(path, shaderSourceSupplier);
        if (patch == null) return shaderSourceSupplier.apply(path);

        return patch.applyPatches(
                path,
                shaderSourceSupplier,
                IrisManager.getPropertiesOrThrow().isEnabled()
        );
    }

    private static void wipeDebug() {
        try(var debugFiles = Files.list(PATCHED_DEBUG_PATH)) {
            var listed = debugFiles.toList();

            for (var file : listed)
                Files.delete(file);
        } catch (IOException e) {
            Photonics.LOGGER.error("An exception was thrown while wiping Photonics's debug output");
        }
    }

    public static void writeDebug(
            IrisPackPath file,
            String source
    ) {
        try {
            Files.writeString(
                    file.ph$resolved(PATCHED_DEBUG_PATH),
                    source
            );
        } catch (IOException e) {
            Photonics.LOGGER.error("An exception was thrown while writing patched output of '{}'", file);
        }
    }

    // Patch tracking
    private static final List<PatchSource> SOURCES;

    private static PatchList patchList = null;
    private static boolean needsReload = true;

    static {
        var sourcesBuilder = ImmutableList.<PatchSource>builder();

        sourcesBuilder.add(JarSource.INSTANCE);
        sourcesBuilder.add(ShaderPatchesSource.INSTANCE);

        if (Photonics.isDevEnvironment()) sourcesBuilder.add(new DevEnvSource());

        SOURCES = sourcesBuilder.build();

        // Reload is deferred so in use patches are not discarded
        for (var source : SOURCES)
            source.onChanged(() -> needsReload = true);
    }

    private static synchronized void checkForReload() {
        if (!needsReload) return;

        // Done in a weird order in case PatchList init throws an exception
        // If you were to first close the old list, it could be left with stale patch paths
        var oldList = patchList;

        patchList = new PatchList(SOURCES);
        needsReload = false;

        if (oldList != null) oldList.close();
    }

    public static PatchList getPatchList() {
        if (needsReload) checkForReload();

        return Objects.requireNonNull(patchList);
    }
}
