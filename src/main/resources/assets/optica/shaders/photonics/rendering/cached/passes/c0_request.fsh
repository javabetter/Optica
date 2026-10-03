#version 430

// Optica cached lighting, pass 1 of 4: find (or create) the four cache samples around every pixel and
// remember their slots for the resolve pass. New samples, old ones seen again after a while out of
// view, and ones computed before a nearby block or light change are queued and computed in the update
// pass of this same frame.

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/cached/surface.glsl"

layout(location = 0) out uvec4 cache_slots_out;
// Slots of the next finer level, for pixels that blend towards it (ph_cache_finer).
layout(location = 1) out uvec4 cache_slots_fine_out;

// Finds or creates the four samples around the pixel on `surface`, and queues the outdated ones.
uvec4 request(CacheSurface surface, uint dirty_age) {
    uvec4 slots = uvec4(ph_cache_none);

    ivec2 cells[4];
    vec4 weights;
    ph_cache_corners(surface, cells, weights);

    for (int i = 0; i < 4; i++) {
        // Corners with no weight (the pixel sits exactly on a sample row) need no sample.
        if (weights[i] <= 0.0f) continue;

        PH_PROFILE_ADD(PH_STAT_CORNERS, 1);
        uint slot = ph_cache_acquire(ph_cache_key(surface, cells[i]));
        slots[i] = slot;
        if (slot == ph_cache_none) continue;

        // Recompute now rather than whenever the rotating update gets to it: samples seen again after
        // being out of view, and samples computed before a block or light changed nearby.
        bool outdated = ph_cache_is_outdated(slot) || ph_cache_retry_due(slot);
        bool predates_change = !outdated && ph_cache_predates_change(slot, dirty_age);

        if (outdated || predates_change) {
            if (outdated) PH_PROFILE_ADD(PH_STAT_REFRESH_OUTDATED, 1);
            else PH_PROFILE_ADD(PH_STAT_REFRESH_DIRTY, 1);

            ph_cache_request_refresh(slot, predates_change);
        }
    }

    return slots;
}

void main() {
    cache_slots_out = uvec4(ph_cache_none);
    cache_slots_fine_out = uvec4(ph_cache_none);

    setup_frag_data(0);
    if (!frag_is_in_world) return;

    CacheSurface surface = ph_cache_pixel_surface();

    PH_PROFILE_ADD(PH_STAT_PIXELS, 1);
    if (surface.coarse) PH_PROFILE_ADD(PH_STAT_COARSE, 1);
    else PH_PROFILE_ADD(PH_STAT_LEVEL0 + clamp(surface.level, 0, 3), 1);

    // The newest block or light change around this pixel, if any.
    vec3 world_pos = frag_is_hand ? cameraPosition : frag_player_pos + cameraPosition;
    uint dirty_age = ph_cache_dirty_age(ivec3(floor(world_pos)));

    cache_slots_out = request(surface, dirty_age);

    float finer_weight;
    CacheSurface finer = ph_cache_finer(surface, finer_weight);
    if (finer_weight > 0.0f) {
        PH_PROFILE_ADD(PH_STAT_BLENDED, 1);
        cache_slots_fine_out = request(finer, dirty_age);
    }
}
