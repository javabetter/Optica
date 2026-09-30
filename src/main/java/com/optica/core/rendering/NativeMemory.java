package com.optica.core.rendering;

import com.optica.core.Photonics;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.BooleanSupplier;

/**
 * Optica: explicitly managed CPU-side memory for the GPU buffer heaps.
 *
 * <p>Photonics used {@code ByteBuffer.allocateDirect}, which the JVM only frees when the garbage
 * collector gets around to it. The voxel world alone is 512 MB, so toggling shaders could hold
 * several copies at once outside the Java heap. These buffers are freed as soon as their heap is
 * closed instead. Worker threads that write into them are stopped first ({@link #join}); if one fails
 * to stop in time, freeing is skipped from then on, so a leak happens rather than a crash.
 */
public final class NativeMemory {
    private static final long SHUTDOWN_TIMEOUT_MS = 10_000;

    private static volatile boolean unsafeToFree = false;

    private NativeMemory() {
    }

    /** Zero-initialized, like {@code ByteBuffer.allocateDirect}. */
    public static ByteBuffer calloc(long byteSize) {
        return MemoryUtil.memCalloc(Math.toIntExact(byteSize))
                .order(ByteOrder.nativeOrder());
    }

    public static void free(ByteBuffer buffer) {
        if (unsafeToFree) return;

        MemoryUtil.memFree(buffer);
    }

    /** Waits for a worker thread that was just interrupted to exit. */
    public static void join(Thread thread) {
        if (thread == Thread.currentThread()) return;

        try {
            thread.join(SHUTDOWN_TIMEOUT_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (thread.isAlive()) markUnsafe(thread.getName());
    }

    /** Waits until {@code done} holds, e.g. until in-flight pool jobs have finished. */
    public static void await(BooleanSupplier done, String what) {
        long deadline = System.currentTimeMillis() + SHUTDOWN_TIMEOUT_MS;

        while (!done.getAsBoolean()) {
            if (System.currentTimeMillis() > deadline) {
                markUnsafe(what);
                return;
            }

            Thread.onSpinWait();
            Thread.yield();
        }
    }

    private static void markUnsafe(String what) {
        unsafeToFree = true;
        Photonics.LOGGER.warn("{} did not stop in time; buffer memory will be left to the garbage collector", what);
    }
}
