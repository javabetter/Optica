#ifndef PH_LOD_INCLUDE
#define PH_LOD_INCLUDE

// Optica: distance level of detail. Passes pick their detail from this distance rather than the real
// one; the "LOD Quality" setting scales it (PH_LOD_SCALE > 1 keeps full detail further out). At LOD
// Quality 1.0 (the default) PH_LOD_SCALE is huge and every pass renders at full detail.

#ifndef PH_LOD_SCALE
#define PH_LOD_SCALE 1000.0
#endif

// False when the level of detail is off (LOD Quality 1.0).
const bool ph_lod_enabled = float(PH_LOD_SCALE) < 100.0f;

float ph_lod_distance(float distance) {
    return distance / float(PH_LOD_SCALE);
}

float ph_lod_distance(vec3 player_pos) {
    return ph_lod_distance(length(player_pos));
}

#endif
