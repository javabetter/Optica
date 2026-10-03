package com.optica.core.rendering;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Optica: GPU time of every Photonics pass, and of whole frames, for the lighting cache profiler.
 * Timestamp queries are read back once they are ready (never waiting on the GPU). Render thread only;
 * does nothing unless the profiler turns it on.
 */
public final class GpuPassTimer {
    /** Unread measurements beyond this are skipped (the GPU is far behind). */
    private static final int MAX_PENDING = 1024;

    private record Pending(String name, int start, int end) {
    }

    private static final class Total {
        long nanos;
        int samples;
    }

    private static boolean enabled = false;
    private static final ArrayDeque<Integer> freeQueries = new ArrayDeque<>();
    private static final List<Integer> allQueries = new ArrayList<>();
    private static final List<Pending> pending = new ArrayList<>();
    private static final Map<String, Total> totals = new LinkedHashMap<>();

    private static String group = null;
    private static int passStart = -1;
    private static int frameStart = -1;

    private GpuPassTimer() {
    }

    public static void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        if (value) return;

        for (int query : allQueries) GL15.glDeleteQueries(query);
        allQueries.clear();
        freeQueries.clear();
        pending.clear();
        totals.clear();
        group = null;
        passStart = -1;
        frameStart = -1;
    }

    /** Start of a group of passes (one Photonics renderer). */
    public static void beginGroup(String name) {
        if (!enabled) return;
        group = name;
        passStart = timestamp();
    }

    /** End of one pass of the current group: measured from the previous pass (or the group start). */
    public static void endPass(String name) {
        if (!enabled || group == null || passStart < 0) return;

        int end = timestamp();
        add(group + "/" + name, passStart, end);
        passStart = timestamp();
    }

    public static void endGroup() {
        if (!enabled) return;
        if (passStart >= 0) freeQueries.push(passStart);
        passStart = -1;
        group = null;
    }

    /** Marks the start of a frame: the time between two marks is the GPU time of a whole frame. */
    public static void frameMark() {
        if (!enabled) return;

        int now = timestamp();
        if (frameStart >= 0) add("frame", frameStart, now);
        frameStart = timestamp();
    }

    /** Average GPU milliseconds per measured run since the last drain, by name ("frame" first). */
    public static Map<String, Double> drain() {
        poll();

        Map<String, Double> result = new LinkedHashMap<>();
        var frame = totals.get("frame");
        if (frame != null) result.put("frame", average(frame));
        for (var entry : totals.entrySet())
            if (!entry.getKey().equals("frame")) result.put(entry.getKey(), average(entry.getValue()));

        totals.clear();
        return result;
    }

    private static double average(Total total) {
        return total.samples == 0 ? 0.0 : total.nanos / 1e6 / total.samples;
    }

    private static void add(String name, int start, int end) {
        if (pending.size() >= MAX_PENDING) {
            freeQueries.push(start);
            freeQueries.push(end);
            return;
        }
        pending.add(new Pending(name, start, end));
        if (pending.size() > 64) poll();
    }

    private static void poll() {
        for (Iterator<Pending> it = pending.iterator(); it.hasNext(); ) {
            var p = it.next();
            if (GL15.glGetQueryObjecti(p.end(), GL15.GL_QUERY_RESULT_AVAILABLE) == 0) continue;

            long start = GL33.glGetQueryObjecti64(p.start(), GL15.GL_QUERY_RESULT);
            long end = GL33.glGetQueryObjecti64(p.end(), GL15.GL_QUERY_RESULT);
            if (end > start) {
                var total = totals.computeIfAbsent(p.name(), ignored -> new Total());
                total.nanos += end - start;
                total.samples++;
            }

            freeQueries.push(p.start());
            freeQueries.push(p.end());
            it.remove();
        }
    }

    private static int timestamp() {
        Integer query = freeQueries.poll();
        if (query == null) {
            query = GL15.glGenQueries();
            allQueries.add(query);
        }

        GL33.glQueryCounter(query, GL33.GL_TIMESTAMP);
        return query;
    }
}
