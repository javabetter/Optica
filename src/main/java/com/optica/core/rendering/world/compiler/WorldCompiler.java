// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.world.compiler;

import com.optica.api.mc.Minecraft;
import com.optica.api.mc.core.IBlockPos;
import com.optica.api.mc.world.level.ILevel;
import com.optica.core.Photonics;
import com.optica.core.iris.pipeline.uniform.IDynamicUniformHolder;
import com.optica.core.iris.pipeline.uniform.IUniformHolder;
import com.optica.core.iris.pipeline.uniform.IUniformUpdateFrequency;
import com.optica.core.rendering.RenderingComponent;
import com.optica.core.rendering.SectionCopy;
import com.optica.core.rendering.SectionManager;
import com.optica.core.rendering.UniformUpdater;
import com.optica.core.rendering.WorldOrigin;
import com.optica.core.rendering.NativeMemory;
import com.optica.core.rendering.world.IgnoredInterruptedException;
import com.optica.core.rendering.world.allocator.WorldAllocator;
import com.optica.core.rendering.world.block.palette.PaletteTexture;
import com.optica.core.rendering.world.registry.WorldRegistry;
import com.optica.core.rendering.world.tree.BlockMergeMode;
import com.optica.core.rendering.world.tree.VoxelTreeEntry;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class WorldCompiler implements Runnable, RenderingComponent {
    public static final int MAX_SECTIONS_PER_RUN = 48;

    private static final int THREAD_POOL_SIZE = 3;
    private static final ExecutorService THREAD_POOL;

    private final SectionManager.TaskQueue<ChunkCompiler.BuildResult> taskQueue;

    private final WorldAllocator worldAllocator;
    private final PaletteTexture paletteTexture;

    private final WorldRegistry registry;

    private final RegionIdManager regionIds = new RegionIdManager();
    private final TreeManager treeManager;

    // Optica: block positions of sections written since the last upload, and who wants to know when
    // they reach the GPU (the cached lighting mode recomputes lighting around them).
    private final List<Vector3i> writtenSections = new ArrayList<>();
    private volatile Consumer<List<Vector3i>> sectionUploadListener = null;
    // Optica: block hash of every loaded section (compiler thread only). Sections are often rebuilt with
    // the same blocks (sky light spreading after a chunk loads, neighbour updates); only real block
    // changes and newly loaded sections are reported.
    private final Map<Vector3i, Long> sectionBlockHashes = new HashMap<>();

    private final ReentrantLock uploadLock = new ReentrantLock();
    private final Condition uploadDone = uploadLock.newCondition();
    private boolean canUpload = true;

    private Vector3i iorigin = null;
    private WorldOrigin offset = null;

    private final Vector3i minBlock = new Vector3i();
    private final Vector3i maxBlock = new Vector3i();

    private final UniformUpdater uniformUpdater = new UniformUpdater();

    private WorldOrigin mostRecentOrigin = new WorldOrigin(0.0f, 0.0f, 0.0f);
    private Vector3d previousOrigin = null;

    private Vector3f mostRecentMinBounds = new Vector3f();
    private Vector3f mostRecentMaxBounds = new Vector3f();


    private Vector3f mostRecentMinBlock = new Vector3f();
    private Vector3f mostRecentMaxBlock = new Vector3f();

    private int mostRecentBlockContainerScale = 0;

    private final Thread compilerThread;
    // Optica: pool jobs of this compiler that are still running; close() waits for them.
    private final AtomicInteger jobsInFlight = new AtomicInteger();

    public WorldCompiler(
            int depth,
            WorldAllocator worldAllocator,
            PaletteTexture paletteTexture,
            SectionManager.TaskQueue<ChunkCompiler.BuildResult> taskQueue,
            WorldRegistry worldRegistry
    ) {
        this.worldAllocator = worldAllocator;
        this.paletteTexture = paletteTexture;

        this.taskQueue = taskQueue;
        this.registry = worldRegistry;

        this.treeManager = new TreeManager(BlockMergeMode.OVERWRITE, worldAllocator);

        this.compilerThread = new Thread(this, "Photonics World Compiler");
        this.compilerThread.start();
    }

    public WorldOrigin origin() {
        return mostRecentOrigin;
    }

    private void setOrigin(Vector3i origin) {
        this.iorigin = origin;
        this.offset = new WorldOrigin(origin.x, origin.y, origin.z);
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                taskQueue.awaitTask();

                var unloadedSections = taskQueue.drainUnloadQueue();
                if (!unloadedSections.isEmpty())
                    clearUnloadedSections(unloadedSections);


                var builtSections = taskQueue.drain(MAX_SECTIONS_PER_RUN);
                List<Vector3i> batch = new ArrayList<>(builtSections.size());
                for (var section : builtSections) {
                    Long previous = sectionBlockHashes.put(new Vector3i(section.chunkPos()), section.blockHash());
                    if (previous == null || previous != section.blockHash())
                        batch.add(new Vector3i(section.chunkBlockPos()));
                }

                if (!builtSections.isEmpty()) {
                    recenter();

                    clearPendingSections(builtSections);
                    insertSections(builtSections);
                }

                if (!unloadedSections.isEmpty() || !builtSections.isEmpty()) {
                    stopUpload();
                    writeSections();
                    awaitUpload(batch);

                    registry.freeUnusedObjects();
                }
            }
        } catch (Throwable t) {
            if (t instanceof InterruptedException) return;
            if (IgnoredInterruptedException.shouldIgnore(t)) return;

            Photonics.LOGGER.error("An error was thrown during world compilation!", t);
        }
    }


    // Compiler steps

    private void clearUnloadedSections(List<Vector3i> unloadedSections) {
        for (var section : unloadedSections) sectionBlockHashes.remove(section);

        if (iorigin == null) return;

        IntSet regions = new IntOpenHashSet(unloadedSections.size());
        for (var section : unloadedSections) {
            regions.add(regionIds.getId(section));
            regionIds.removeRegion(section);
        }

        treeManager.removeRegions(regions);
    }

    private void recenter() throws InterruptedException {
        var newOrigin = WorldOrigin.getAsVector3i();
        if (iorigin == null) {
            setOrigin(newOrigin);
            return;
        }

        if (iorigin.equals(newOrigin)) return;

        stopUpload();

        var offset = iorigin.sub(newOrigin, new Vector3i());
        treeManager.recenter(offset);

        setOrigin(newOrigin);
    }

    private void clearPendingSections(List<ChunkCompiler.BuildResult> sections) {
        IntSet regions = new IntOpenHashSet(sections.size());
        for (var section : sections)
            regions.add(regionIds.getId(section.chunkPos()));

        treeManager.removeRegions(regions);
    }

    private void insertSections(List<ChunkCompiler.BuildResult> sections) {
        BlockSorter blockSorter = new BlockSorter();
        Vector3i blockPos = new Vector3i();

        ILevel level = Minecraft.getLevel();
        if (level == null) return;

        for (var section : sections) {
            try (section) {
                blockSorter.reset();

                var chunkBlockPos = new Vector3i(section.chunkBlockPos())
                        .sub(iorigin);

                int region = regionIds.getId(section.chunkPos());
                section.forEachBlock((blockChunkOffset, blockState, blockModel) -> blockSorter.addBlock(
                        chunkBlockPos.add(blockChunkOffset, new Vector3i()),
                        blockState,
                        blockModel
                ));

                blockSorter.forEachBlock((block) -> {
                    var parts = block.blockModel().parts();
                    if (parts.isEmpty()) return;

                    var light = registry.lightRegistry().getWeak(block.blockState());

                    for (int i = 0; i < parts.size(); i++) {
                        var part = parts.get(i);

                        blockPos.set(block.x(), block.y(), block.z());
                        blockPos.add(part.offset());
                        blockPos.add(iorigin);

                        var skylight = SectionCopy.compileSkylight(level, IBlockPos.of(blockPos));
                        var entry = part.createEntry(region, skylight, light);

                        blockPos.sub(iorigin);

                        treeManager.insertBlock(
                                blockPos,
                                entry
                        );
                    }
                });
            }
        }
    }

    private void writeSections() throws InterruptedException {
        treeManager.uploadAll(() -> new MultiThreadTask(jobsInFlight));
        treeManager.findBounds(minBlock, maxBlock);
    }
    // Uploading

    private void stopUpload() throws InterruptedException {
        uploadLock.lockInterruptibly();

        try {
            canUpload = false;
        } finally {
            uploadLock.unlock();
        }
    }

    private void awaitUpload(List<Vector3i> writtenBatch) throws InterruptedException {
        uploadLock.lockInterruptibly();

        try {
            // Reported by the upload that publishes them (onFrameBegin), not before.
            if (sectionUploadListener != null) writtenSections.addAll(writtenBatch);
            canUpload = true;
            uniformUpdater.updateNextFrame();
            uploadDone.await();
        } finally {
            uploadLock.unlock();
        }
    }

    /** Optica: called on the render thread with the block positions of sections as they reach the GPU. */
    public void setSectionUploadListener(Consumer<List<Vector3i>> listener) {
        this.sectionUploadListener = listener;
    }

    @Override
    public void onFrameBegin() {
        uploadLock.lock();

        try {
            if (!canUpload) return;

            worldAllocator.upload();
            paletteTexture.upload();
            uniformUpdater.updateAll();

            mostRecentMinBounds = new Vector3f(treeManager.minBounds());
            mostRecentMaxBounds = new Vector3f(treeManager.maxBounds());

            mostRecentOrigin = offset == null ? new WorldOrigin(0, 0, 0) : offset;

            mostRecentMinBlock = new Vector3f(minBlock);
            mostRecentMaxBlock = new Vector3f(maxBlock);

            mostRecentBlockContainerScale = 21 - (treeManager.depth() - (VoxelTreeEntry.BLOCK_CONTAINER_DEPTH) << 1);

            var listener = sectionUploadListener;
            if (listener != null && !writtenSections.isEmpty()) {
                listener.accept(List.copyOf(writtenSections));
                writtenSections.clear();
            }

            uploadDone.signalAll();
        } finally {
            uploadLock.unlock();
        }
    }

    @Override
    public void registerUniforms(IUniformHolder uniforms) {
        // TODO: Replace this with actual values
        uniforms.uniform1i(IUniformUpdateFrequency.once(), "phFirstBuildTime", () -> 1);
        uniforms.uniform1i(IUniformUpdateFrequency.once(), "phLastBuildTime", () -> 1);
    }

    @Override
    public void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms) {
        dynamicUniforms.uniform3f(
                "world_offset",
                () -> {
                    var offset = mostRecentOrigin;
                    if (offset == null) return new Vector3f(0f);

                    return new Vector3f(offset);
                },
                uniformUpdater.newNotifier()
        );

        dynamicUniforms.uniform3d(
                IUniformUpdateFrequency.perFrame(),
                "delta_world_offset",
                () -> {
                    var previous = previousOrigin;
                    var current = new Vector3d(mostRecentOrigin);

                    previousOrigin = current;
                    if (previous == null || current == null)
                        return new Vector3d();

                    return current.sub(previous, new Vector3d());
                }
        );

        dynamicUniforms.uniform3f("world_min_block", () -> new Vector3f(mostRecentMinBlock), uniformUpdater.newNotifier());
        dynamicUniforms.uniform3f("world_max_block", () -> new Vector3f(mostRecentMaxBlock), uniformUpdater.newNotifier());

        dynamicUniforms.uniform3f("world_tree_size", () -> new Vector3f(mostRecentMaxBounds).sub(mostRecentMinBounds), uniformUpdater.newNotifier());
        dynamicUniforms.uniform1i("world_block_scale_exp", () -> mostRecentBlockContainerScale, uniformUpdater.newNotifier());

        dynamicUniforms.uniform3f(
                IUniformUpdateFrequency.perFrame(),
                "rt_camera_position",
                () -> {
                    var offset = mostRecentOrigin;
                    if (offset == null) return new Vector3f(0f);

                    var pos = Minecraft.getCameraPos();
                    return new Vector3f(offset.applyOffset(new Vector3d(pos.x, pos.y, pos.z)));
                }
        );
    }

    @Override
    public void close() {
        compilerThread.interrupt();

        // The compiler and its pool jobs write into the world buffer, which is freed right after this.
        NativeMemory.join(compilerThread);
        NativeMemory.await(() -> jobsInFlight.get() == 0, "Optica world compiler jobs");
    }

    private static class MultiThreadTask extends CompletableFuture<Void> implements CompilerTask {
        private final AtomicInteger pendingTasks = new AtomicInteger();
        private final AtomicInteger jobsInFlight;

        private MultiThreadTask(AtomicInteger jobsInFlight) {
            this.jobsInFlight = jobsInFlight;
        }

        @Override
        public void queueJob(Runnable task) {
            pendingTasks.incrementAndGet();
            jobsInFlight.incrementAndGet();

            THREAD_POOL.execute(() -> {
                try {
                    task.run();
                } finally {
                    jobsInFlight.decrementAndGet();
                    if (pendingTasks.decrementAndGet() == 0)
                        complete(null);
                }
            });
        }

        @Override
        public void awaitCompletion() throws InterruptedException {
            try {
                if (pendingTasks.get() != 0)
                    get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class SingleThreadTask implements CompilerTask {
        @Override
        public void queueJob(Runnable task) {
            task.run();
        }

        @Override
        public void awaitCompletion() {

        }
    }

    static {
        AtomicInteger count = new AtomicInteger(0);
        THREAD_POOL = Executors.newFixedThreadPool(THREAD_POOL_SIZE, (r) ->
                new Thread(r, "Photonics World Worker #" + count.getAndIncrement()));
    }
}
