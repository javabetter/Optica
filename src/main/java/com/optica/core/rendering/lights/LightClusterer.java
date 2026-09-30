package com.optica.core.rendering.lights;

import com.optica.api.mc.Id;
import com.optica.api.mc.world.level.IBlock;
import com.optica.core.config.lights.BlockLightInfo;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Optica: merges dense groups of identical lights (lava lakes, glowstone ceilings, rows of lanterns) into
 * one light per small cell. A lava lake otherwise contributes one light per surface block and fills the
 * light limit on its own, and every fragment near it pays for each of them.
 *
 * <p>Cells grow with distance from the camera (a level of detail for lights): nearby lights stay precise,
 * distant ones are merged coarsely. The merged light sits on the member block closest to the group's
 * centre, so shadow rays still end at a real light block, and uses {@link BlockLightInfo#clustered}: the
 * members' summed brightness, softened by how far they spread.
 */
final class LightClusterer {
    private static final IBlock BLOCK_LAVA = IBlock.fromIdOrThrow(Id.fromNamespaceAndPath("minecraft", "lava"));

    private static final int MAX_CELL_SIZE = 64;
    /** Coarsening passes when the merged list still exceeds the light budget. */
    private static final int MAX_PASSES = 3;

    /** Cell edge length in blocks for a light at the given distance from the camera. */
    static int cellSize(double distance) {
        if (distance < 8) return 2;
        if (distance < 24) return 4;
        if (distance < 48) return 8;
        if (distance < 96) return 16;
        return 32;
    }

    /** Groups smaller than this are left alone; small cells keep ordinary torches and lamps separate. */
    static int minClusterSize(int cellSize) {
        return cellSize <= 4 ? 3 : 2;
    }

    private LightClusterer() {
    }

    private record Key(int cellSize, int x, int y, int z, int blockId, BlockLightInfo lightInfo) {
    }

    /**
     * @param gainExponent brightness of a merged light relative to one member is {@code count^gainExponent}.
     *                     1 keeps the total (ReSTIR, which sums every light); BASIC only sums the brightest
     *                     few lights per fragment, and 0.5 keeps its look roughly unchanged.
     */
    static TracedLightPosition[] cluster(TracedLightPosition[] lights, Vector3d camera, float gainExponent, int maxLights) {
        // Coarsen further while the result would not fit the light budget: the lights that get dropped
        // otherwise are the distant ones, and merging them loses less than dropping them.
        TracedLightPosition[] result = lights;
        for (int pass = 0, scale = 1; pass < MAX_PASSES; pass++, scale *= 2) {
            result = clusterPass(lights, camera, gainExponent, scale);
            if (result.length <= maxLights) break;
        }

        return result;
    }

    private static TracedLightPosition[] clusterPass(TracedLightPosition[] lights, Vector3d camera, float gainExponent, int scale) {
        Map<Key, List<TracedLightPosition>> groups = new LinkedHashMap<>();

        for (var light : lights) {
            var pos = light.pos();
            int cellSize = cellSize(pos.distance(camera)) * scale;
            // Lava lakes are large, flat and evenly lit: merge them one level coarser.
            if (light.blockState().ph$is(BLOCK_LAVA)) cellSize *= 2;
            cellSize = Math.min(cellSize, MAX_CELL_SIZE);

            var key = new Key(
                    cellSize,
                    Math.floorDiv((int) Math.floor(pos.x), cellSize),
                    Math.floorDiv((int) Math.floor(pos.y), cellSize),
                    Math.floorDiv((int) Math.floor(pos.z), cellSize),
                    light.blockId(),
                    light.lightInfo()
            );

            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(light);
        }

        if (groups.size() == lights.length) return lights;

        List<TracedLightPosition> result = new ArrayList<>(groups.size());

        for (var entry : groups.entrySet()) {
            var group = entry.getValue();
            if (group.size() < minClusterSize(entry.getKey().cellSize())) {
                result.addAll(group);
                continue;
            }

            result.add(merge(group, gainExponent));
        }

        return result.toArray(TracedLightPosition[]::new);
    }

    private static TracedLightPosition merge(List<TracedLightPosition> group, float gainExponent) {
        var centre = new Vector3d();
        for (var light : group) centre.add(light.pos());
        centre.div(group.size());

        TracedLightPosition representative = group.getFirst();
        double bestDistance = Double.MAX_VALUE;

        for (var light : group) {
            double distance = light.pos().distanceSquared(centre);
            if (distance < bestDistance) {
                bestDistance = distance;
                representative = light;
            }
        }

        double spread = 0.0;
        for (var light : group) spread += light.pos().distanceSquared(representative.pos());
        spread /= group.size();

        return new TracedLightPosition(
                representative.blockId(),
                representative.pos(),
                representative.blockState(),
                representative.lightInfo().clustered((float) Math.pow(group.size(), gainExponent), (float) spread)
        );
    }
}
