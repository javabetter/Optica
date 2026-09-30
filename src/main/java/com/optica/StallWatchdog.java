package com.optica;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Optica: logs what the render thread is doing when the game stops ticking for a while (for example
 * during a long freeze on a dimension change), so a log shows where the time goes. Diagnostic only.
 */
final class StallWatchdog implements Runnable {
    private static final long STALL_MS = 5_000;
    private static final long REPEAT_MS = 10_000;
    private static final int MAX_FRAMES = 45;

    private final Thread renderThread;
    private volatile long lastTick = 0; // 0 until the first tick: startup loading is not a stall

    private StallWatchdog(Thread renderThread) {
        this.renderThread = renderThread;
    }

    static void start() {
        var watchdog = new StallWatchdog(Thread.currentThread());
        ClientTickEvents.START_CLIENT_TICK.register(client -> watchdog.lastTick = System.currentTimeMillis());

        var thread = new Thread(watchdog, "Optica Stall Watchdog");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    @Override
    public void run() {
        long lastReport = 0;

        while (renderThread.isAlive()) {
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                return;
            }

            if (lastTick == 0) continue;

            long now = System.currentTimeMillis();
            long stalledFor = now - lastTick;

            if (stalledFor < STALL_MS) {
                lastReport = 0;
                continue;
            }

            if (lastReport != 0 && now - lastReport < REPEAT_MS) continue;
            lastReport = now;

            var runtime = Runtime.getRuntime();
            long usedMb = (runtime.totalMemory() - runtime.freeMemory()) >> 20;

            String stack = Arrays.stream(renderThread.getStackTrace())
                    .limit(MAX_FRAMES)
                    .map(frame -> "\tat " + frame)
                    .collect(Collectors.joining("\n"));

            OpticaClient.LOGGER.warn(
                    "Render thread has not ticked for {} s (heap used {} MB). It is currently at:\n{}",
                    stalledFor / 1000, usedMb, stack
            );
        }
    }
}
