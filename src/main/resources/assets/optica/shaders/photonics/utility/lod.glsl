#ifndef PH_LOD_INCLUDE
#define PH_LOD_INCLUDE

// Optica: distance level of detail. Passes pick their detail from this distance rather than the real
// one; the "LOD Quality" setting scales it (PH_LOD_SCALE > 1 keeps full detail further out).

#ifndef PH_LOD_SCALE
#define PH_LOD_SCALE 1.0
#endif

float ph_lod_distance(float distance) {
    return distance / float(PH_LOD_SCALE);
}

float ph_lod_distance(vec3 player_pos) {
    return ph_lod_distance(length(player_pos));
}

#endif
