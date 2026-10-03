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

    // Optica: world regions (min xyz, max xyz exclusive, in blocks) whose voxel blocks changed in the
    // sections written since the last upload, and who wants to know when they reach the GPU (the cached
    // lighting mode recomputes lighting around them).
    private final List<int[]> writtenChanges = new ArrayList<>();
    private volatile Consumer<List<int[]>> sectionUploadListener = null;
    // Optica: per loaded section, a hash of the voxel blocks in each of its 4x4x4 cubes (compiler thread
    // only). Sections are rebuilt far more often than the blocks the tracer sees change (sky light,
    // neighbour and block state updates that do not change a block's shape); only cubes whose voxel
    // blocks changed are reported, so lighting is recomputed around them alone.
    private static final int CUBES = 64;
    private final Map<Vector3i, long[]> sectionCubeHashes = new HashMap<>();

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
                List<int[]> batch = new ArrayList<>();

                if (!builtSections.isEmpty()) {
                    recenter();

                    clearPendingSections(builtSections);
                    insertSections(builtSections, batch);
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
        for (var section : unloadedSections) sectionCubeHashes.remove(section);

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

    private void insertSections(List<ChunkCompiler.BuildResult> sections, List<int[]> changes) {
        BlockSorter blockSorter = new BlockSorter();
        Vector3i blockPos = new Vector3i();

        ILevel level = Minecraft.getLevel();
        if (level == null) return;

        for (var section : sections) {
            try (section) {
                blockSorter.reset();

                var chunkBlockPos = new Vector3i(section.chunkBlockPos())
                        .sub(iorigin);
                long[] cubes = new long[CUBES];

                int region = regionIds.getId(section.chunkPos());
                section.forEachBlock((blockChunkOffset, blockState, blockModel) -> blockSorter.addBlock(
                        chunkBlockPos.add(blockChunkOffset, new Vector3i()),
                        blockState,
                        blockModel
                ));

                blockSorter.forEachBlock((block) -> {
                    var parts = block.blockModel().parts();
                    if (parts.isEmpty()) return;

                    int lx = block.x() - chunkBlockPos.x, ly = block.y() - chunkBlockPos.y, lz = block.z() - chunkBlockPos.z;
                    if ((lx | ly | lz) >= 0 && lx < 16 && ly < 16 && lz < 16) {
                        int cube = (lx >> 2) | ((ly >> 2) << 2) | ((lz >> 2) << 4);
                        long h = (lx | (ly << 4) | (lz << 8)) * 0x9E3779B97F4A7C15L ^ block.blockState().hashCode() * 0xC2B2AE3D27D4EB4FL;
                        cubes[cube] += h ^ (h >>> 29);
                    }

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

                reportChangedCubes(section, cubes, changes);
            }
        }
    }

    /** Optica: adds the region of the section's cubes whose voxel blocks changed (unloaded counts as empty). */
    private void reportChangedCubes(ChunkCompiler.BuildResult section, long[] cubes, List<int[]> changes) {
        long[] previous = sectionCubeHashes.put(new Vector3i(section.chunkPos()), cubes);

        int minX = 4, minY = 4, minZ = 4, maxX = -1, maxY = -1, maxZ = -1;
        for (int i = 0; i < CUBES; i++) {
            if (cubes[i] == (previous == null ? 0L : previous[i])) continue;

            int x = i & 3, y = (i >> 2) & 3, z = i >> 4;
            minX = Math.min(minX, x); minY = Math.min(minY, y); minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x); maxY = Math.max(maxY, y); maxZ = Math.max(maxZ, z);
        }

        if (maxX < 0 || sectionUploadListener == null) return;

        var origin = section.chunkBlockPos();
        changes.add(new int[] {
                origin.x + minX * 4, origin.y + minY * 4, origin.z + minZ * 4,
                origin.x + maxX * 4 + 4, origin.y + maxY * 4 + 4, origin.z + maxZ * 4 + 4
        });
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

    private void awaitUpload(List<int[]> writtenBatch) throws InterruptedException {
        uploadLock.lockInterruptibly();

        try {
            // Reported by the upload that publishes them (onFrameBegin), not before.
            if (sectionUploadListener != null) writtenChanges.addAll(writtenBatch);
            canUpload = true;
            uniformUpdater.updateNextFrame();
            uploadDone.await();
        } finally {
            uploadLock.unlock();
        }
    }

    /** Optica: called on the render thread with the block positions of sections as they reach the GPU. */
    public void setSectionUploadListener(Consumer<List<int[]>> listener) {
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
            if (listener != null && !writtenChanges.isEmpty()) {
                listener.accept(List.copyOf(writtenChanges));
                writtenChanges.clear();
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
