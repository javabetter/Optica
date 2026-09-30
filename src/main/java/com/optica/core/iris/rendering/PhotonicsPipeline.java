// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.rendering;

import com.optica.api.mc.Minecraft;
import com.optica.core.iris.pipeline.IrisPipeline;
import com.optica.core.iris.pipeline.IrisRenderer;
import com.optica.core.iris.properties.PhotonicsProperties;
import com.optica.core.iris.rendering.restir.RestirProperties;
import com.optica.core.rendering.AbstractRenderingComponent;
import com.optica.core.rendering.RenderingComponent;
import com.optica.core.rendering.SectionManager;
import com.optica.core.rendering.lights.BufferLightList;
import com.optica.core.rendering.lights.HandheldItemSupplier;
import com.optica.core.rendering.world.allocator.buffer.BufferPaletteTexture;
import com.optica.core.rendering.world.allocator.buffer.BufferWorldAllocator;
import com.optica.core.rendering.world.bakery.texture.AtlasDownloader;
import com.optica.core.rendering.world.compiler.ChunkCompiler;
import com.optica.core.rendering.world.compiler.WorldCompiler;
import com.optica.core.rendering.world.registry.WorldRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class PhotonicsPipeline extends AbstractRenderingComponent {
    private static final int ROOT_VOXEL_DEPTH = 3;

    protected final PhotonicsProperties properties;
    private final List<IrisRenderer> renderers = new ArrayList<>();

    public PhotonicsPipeline(
            PhotonicsProperties properties,
            AtlasDownloader atlasDownloader,
            IrisPipeline pipeline,
            @Nullable RenderingComponent... components
    ) {
        super(components);
        this.properties = properties;
        registerComponent(pipeline);

        registerResource(atlasDownloader);
        var sectionManager = registerComponent(new SectionManager(Minecraft::getRenderDistance));

        var worldAllocator = registerComponent(new BufferWorldAllocator(1 << 29));
        var paletteTexture = registerComponent(new BufferPaletteTexture(2048, 600));

        var worldRegistry = new WorldRegistry(worldAllocator, paletteTexture, atlasDownloader);

        var builtSectionQueue = sectionManager.<ChunkCompiler.BuildResult>newTaskQueue(WorldCompiler.MAX_SECTIONS_PER_RUN << 1, true);
        var worldCompiler = registerComponent(new WorldCompiler(
                ROOT_VOXEL_DEPTH,
                worldAllocator,
                paletteTexture,
                builtSectionQueue,
                worldRegistry
        ));

        registerComponent(new ChunkCompiler(
                sectionManager,
                builtSectionQueue,
                worldRegistry
        ));

        registerComponent(
                new BufferLightList(
                        sectionManager,
                        properties.getLightListProperties().getSize(),
                        worldCompiler::origin
                )
        );
    }

    public <T extends IrisRenderer> T registerRenderer(T component) {
        if (component != null) {
            if (component instanceof RenderingComponent renderingComponent)
                registerComponent(renderingComponent);

            renderers.add(component);
        }

        return component;
    }

    public void onRender() {
        renderers.forEach(IrisRenderer::renderAll);
    }

    @FunctionalInterface
    interface Supplier<T extends PhotonicsPipeline, P> {
        T create(PhotonicsProperties phProperties, P rendererProperties, AtlasDownloader atlasDownloader, HandheldItemSupplier handheldItemSupplier, IrisPipeline irisPipeline);
    }
}
