#version 430

// Optica cached lighting, pass 3 of 4: interpolate the cached samples around every pixel. Direct light
// goes to sharp_direct (read by the pack through the BASIC samplers), GI to cached_indirect.
//
// Samples fade in over a few frames once computed. Until a pixel's samples are all there (joining a
// world, newly revealed surfaces), the missing share is filled with live BASIC-style lighting from the
// brightest few lights, so lighting is present from the first frame and blends into the cached result
// instead of appearing block by block.

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/cached/surface.glsl"
#include "/photonics/rendering/cached/direct.glsl"

// Lights evaluated (with shadow rays) for the stand-in lighting. It only runs where samples are
// missing, so it costs nothing once the cache has filled.
const int ph_cache_fallback_lights = 4;

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

        float weight = weights[i] * ph_cache_fade_in(slots[i]);
        if (weight <= 0.0f) continue;

        vec3 sample_direct;
        vec3 sample_indirect;
        ph_cache_load(slots[i], sample_direct, sample_indirect);

        direct += sample_direct * weight;
        indirect += sample_indirect * weight;
        weight_sum += weight;
    }

    // The corner weights add up to 1, so weight_sum is the share of the pixel the cache covers.
    float coverage = clamp(weight_sum, 0.0f, 1.0f);

    if (weight_sum > 0.0001f) {
        direct /= weight_sum;
        indirect /= weight_sum;
    }

    if (coverage < 0.999f) {
        vec3 position = frag_is_hand ? rt_camera_position : frag_data_rt_pos(_frag_data);
        vec3 fallback = ph_cache_direct_light(position + frag_geo_normal * 0.05f, frag_geo_normal, ph_cache_fallback_lights);

        direct = mix(fallback, direct, coverage);
        // No stand-in for GI: it fades in with the samples.
        indirect *= coverage;
    }

#if defined PH_CACHE_COMBINED_GI
    direct += indirect;
#endif

    sharp_direct_out = vec4(direct, 1.0f);
    cached_indirect_out = vec4(indirect, 1.0f);
}
