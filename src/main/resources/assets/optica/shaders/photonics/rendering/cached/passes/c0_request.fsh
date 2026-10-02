#version 430

// Optica cached lighting, pass 1 of 4: find (or create) the four cache samples around every pixel and
// remember their slots for the resolve pass. New samples, old ones seen again after a while out of
// view, and ones computed before a nearby block or light change are queued and computed in the update
// pass of this same frame.

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/cached/surface.glsl"

layout(location = 0) out uvec4 cache_slots_out;

void main() {
    cache_slots_out = uvec4(ph_cache_none);

    setup_frag_data(0);
    if (!frag_is_in_world) return;

    CacheSurface surface = ph_cache_pixel_surface();

    ivec2 cells[4];
    vec4 weights;
    ph_cache_corners(surface, cells, weights);

    // The newest block or light change around this pixel, if any.
    vec3 world_pos = frag_is_hand ? cameraPosition : frag_player_pos + cameraPosition;
    uint dirty_age = ph_cache_dirty_age(ivec3(floor(world_pos)));

    for (int i = 0; i < 4; i++) {
        // Corners with no weight (the pixel sits exactly on a sample row) need no sample.
        if (weights[i] <= 0.0f) continue;

        uint slot = ph_cache_acquire(ph_cache_key(surface, cells[i]));
        cache_slots_out[i] = slot;

        // Recompute now rather than whenever the rotating update gets to it: samples seen again after
        // being out of view, and samples computed before a block or light changed nearby.
        if (slot != ph_cache_none && (ph_cache_is_outdated(slot) || ph_cache_predates_change(slot, dirty_age)))
            ph_cache_request_refresh(slot);
    }
}
