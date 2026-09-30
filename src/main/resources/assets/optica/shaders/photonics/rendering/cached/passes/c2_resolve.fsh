#version 430

// Optica cached lighting, pass 3 of 4: interpolate the cached samples around every pixel. Direct light
// goes to sharp_direct (read by the pack through the BASIC samplers), GI to cached_indirect.

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/cached/surface.glsl"

uniform usampler2D cache_slots;

layout(location = 0) out vec4 sharp_direct_out;
layout(location = 1) out vec4 cached_indirect_out;

void main() {
    sharp_direct_out = vec4(0.0f);
    cached_indirect_out = vec4(0.0f);

    setup_frag_data(0);
    if (!frag_is_in_world) return;

    CacheSurface surface = ph_cache_pixel_surface();

    ivec2 cells[4];
    vec4 weights;
    ph_cache_corners(surface, cells, weights);

    uvec4 slots = texelFetch(cache_slots, frag_tex_coord, 0);

    vec3 direct = vec3(0.0f);
    vec3 indirect = vec3(0.0f);
    float weight_sum = 0.0f;

    for (int i = 0; i < 4; i++) {
        if (weights[i] <= 0.0f || slots[i] == ph_cache_none) continue;

        // The slot may have been given to another key since the request pass (rare races).
        if (!ph_cache_matches(slots[i], ph_cache_key(surface, cells[i]))) continue;
        if (!ph_cache_is_computed(slots[i])) continue;

        vec3 sample_direct;
        vec3 sample_indirect;
        ph_cache_load(slots[i], sample_direct, sample_indirect);

        direct += sample_direct * weights[i];
        indirect += sample_indirect * weights[i];
        weight_sum += weights[i];
    }

    if (weight_sum > 0.0001f) {
        direct /= weight_sum;
        indirect /= weight_sum;
    }

#if defined PH_CACHE_COMBINED_GI
    direct += indirect;
#endif

    sharp_direct_out = vec4(direct, 1.0f);
    cached_indirect_out = vec4(indirect, 1.0f);
}
