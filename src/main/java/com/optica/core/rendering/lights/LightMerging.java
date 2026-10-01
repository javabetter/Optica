package com.optica.core.rendering.lights;

/**
 * Optica: how {@link LightClusterer} merges lights.
 *
 * @param gainExponent brightness of a merged light relative to one member is {@code count^gainExponent}.
 *                     1 keeps the total (ReSTIR, which sums every light); BASIC only sums the brightest
 *                     few lights per fragment, and 0.5 keeps its look roughly unchanged.
 * @param level        0 turns merging off (the default); 2 (Medium) uses the cells merging was tuned with,
 *                     every step up doubles them
 * @param lodScale     multiplies the distances at which merge cells grow
 */
public record LightMerging(float gainExponent, int level, float lodScale) {
    /** The level whose merge cells are {@link LightClusterer#cellSize} as is (Medium). */
    public static final int BASE_LEVEL = 2;

    public boolean isEnabled() {
        return level > 0;
    }
}
