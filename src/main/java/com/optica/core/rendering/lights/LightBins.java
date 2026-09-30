package com.optica.core.rendering.lights;

import com.optica.core.rendering.WorldOrigin;
import org.joml.Vector3d;

import java.nio.ByteBuffer;

/**
 * A uniform grid that maps regions of the world to the lights that can reach them, used by the
 * BASIC ("sharp") lighting mode so each fragment only evaluates nearby lights. This restores the
 * light binning that Photonics 0.3.x had and the 0.4 rewrite dropped.
 *
 * <p>Layout of the {@code ph_light_bins} buffer (all ints, std430):
 * <pre>
 * [0..2]                    grid origin, in cells, in light-list space
 * [3]                       grid size (cells per axis)
 * [4 .. 4 + CELLS]          CELLS + 1 prefix offsets into the index list (relative to the index list start)
 * [INDEX_START ..]          light list indices
 * </pre>
 * Light-list space is the space light positions are stored in; a fragment's position in it is
 * {@code rt_pos + light_list_offset}.
 */
public final class LightBins {
    public static final int CELL_SIZE = 8;
    public static final int GRID_SIZE = 64;
    public static final int CELL_COUNT = GRID_SIZE * GRID_SIZE * GRID_SIZE;

    public static final int HEADER_INTS = 4;
    public static final int INDEX_START = HEADER_INTS + CELL_COUNT + 1;

    /** Lights are binned up to this many blocks away; beyond it their contribution is negligible. */
    public static final float MAX_RADIUS = 24.0f;
    private static final int MAX_CELLS_PER_AXIS = (int) Math.ceil(2 * MAX_RADIUS / CELL_SIZE) + 1;
    private static final int MAX_CELLS_PER_LIGHT = MAX_CELLS_PER_AXIS * MAX_CELLS_PER_AXIS * MAX_CELLS_PER_AXIS;

    private final int maxIndices;
    private final int[] offsets = new int[CELL_COUNT + 1];
    private final int[] cursors = new int[CELL_COUNT];

    public LightBins(int maxLights) {
        this.maxIndices = maxLights * MAX_CELLS_PER_LIGHT;
    }

    public static long byteSize(int maxLights) {
        return ((long) INDEX_START + (long) maxLights * MAX_CELLS_PER_LIGHT) * Integer.BYTES;
    }

    /**
     * Rebuilds the grid around {@code cameraPosition} and writes it to {@code buffer} (absolute puts).
     */
    public void build(ByteBuffer buffer, LightList lights, WorldOrigin origin, Vector3d cameraPosition) {
        Vector3d camera = origin.applyOffset(cameraPosition);
        int originX = Math.floorDiv((int) Math.floor(camera.x), CELL_SIZE) - GRID_SIZE / 2;
        int originY = Math.floorDiv((int) Math.floor(camera.y), CELL_SIZE) - GRID_SIZE / 2;
        int originZ = Math.floorDiv((int) Math.floor(camera.z), CELL_SIZE) - GRID_SIZE / 2;

        int lightCount = lights.size();
        int[][] ranges = new int[lightCount][];

        java.util.Arrays.fill(offsets, 0);

        // Pass 1: count lights per cell.
        long total = 0;
        for (int i = 0; i < lightCount; i++) {
            TracedLightPosition light = lights.get(i);
            if (light == null) continue;

            Vector3d pos = origin.applyOffset(light.pos());
            float radius = Math.min(light.lightInfo().radiusInBlocks(), MAX_RADIUS);

            int minX = clampCell(Math.floorDiv((int) Math.floor(pos.x - radius), CELL_SIZE) - originX);
            int minY = clampCell(Math.floorDiv((int) Math.floor(pos.y - radius), CELL_SIZE) - originY);
            int minZ = clampCell(Math.floorDiv((int) Math.floor(pos.z - radius), CELL_SIZE) - originZ);
            int maxX = Math.floorDiv((int) Math.floor(pos.x + radius), CELL_SIZE) - originX;
            int maxY = Math.floorDiv((int) Math.floor(pos.y + radius), CELL_SIZE) - originY;
            int maxZ = Math.floorDiv((int) Math.floor(pos.z + radius), CELL_SIZE) - originZ;

            if (maxX < 0 || maxY < 0 || maxZ < 0 || minX >= GRID_SIZE || minY >= GRID_SIZE || minZ >= GRID_SIZE)
                continue;

            maxX = clampCell(maxX);
            maxY = clampCell(maxY);
            maxZ = clampCell(maxZ);

            long cells = (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
            if (total + cells > maxIndices) break; // Lights are sorted by importance; drop the rest.
            total += cells;

            ranges[i] = new int[] {minX, minY, minZ, maxX, maxY, maxZ};
            for (int z = minZ; z <= maxZ; z++)
                for (int y = minY; y <= maxY; y++)
                    for (int x = minX; x <= maxX; x++)
                        offsets[cellIndex(x, y, z) + 1]++;
        }

        // Prefix sum -> start offset of each cell.
        for (int c = 0; c < CELL_COUNT; c++)
            offsets[c + 1] += offsets[c];

        System.arraycopy(offsets, 0, cursors, 0, CELL_COUNT);

        // Pass 2: scatter light indices.
        for (int i = 0; i < lightCount; i++) {
            int[] range = ranges[i];
            if (range == null) continue;

            for (int z = range[2]; z <= range[5]; z++)
                for (int y = range[1]; y <= range[4]; y++)
                    for (int x = range[0]; x <= range[3]; x++) {
                        int slot = cursors[cellIndex(x, y, z)]++;
                        buffer.putInt((INDEX_START + slot) * Integer.BYTES, i);
                    }
        }

        buffer.putInt(0, originX);
        buffer.putInt(Integer.BYTES, originY);
        buffer.putInt(2 * Integer.BYTES, originZ);
        buffer.putInt(3 * Integer.BYTES, GRID_SIZE);

        for (int c = 0; c <= CELL_COUNT; c++)
            buffer.putInt((HEADER_INTS + c) * Integer.BYTES, offsets[c]);
    }

    private static int clampCell(int cell) {
        return Math.clamp(cell, 0, GRID_SIZE - 1);
    }

    private static int cellIndex(int x, int y, int z) {
        return (z * GRID_SIZE + y) * GRID_SIZE + x;
    }
}
