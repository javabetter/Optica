#version 430

// Optica cached lighting, pass 3 of 4: interpolate the cached samples around every pixel. Direct light
// goes to sharp_direct (read by the pack through the BASIC samplers), GI to cached_indirect.
//
// Where some of a pixel's samples are not available this frame (not computed yet, the queue was full,
// the entry was replaced), the missing share keeps what the pixel showed before (its reprojected
// history), so lighting never drops out. Only surfaces with no history (just revealed, or joining a
// world) use live BASIC-style lighting from the brightest lights as a stand-in. Fully covered pixels
// use the samples directly.

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/utility/projection.glsl"
#include "/photonics/rendering/cached/surface.glsl"
#include "/photonics/rendering/cached/direct.glsl"

// Lights evaluated (with shadow rays) for the stand-in lighting. It only runs where samples are
// missing, so it costs nothing once the cache has filled.
const int ph_cache_fallback_lights = 8;

#ifndef PH_CACHE_DEBUG_VIEW
#define PH_CACHE_DEBUG_VIEW 0
#endif

uniform usampler2D cache_slots;
uniform usampler2D cache_slots_fine;
uniform sampler2D prev_sharp_direct;
uniform sampler2D prev_cached_indirect;

layout(location = 0) out vec4 sharp_direct_out;
layout(location = 1) out vec4 cached_indirect_out;

// Reprojects last frame's output. Returns false when no neighbouring texel is the same surface.
bool load_history(out vec3 direct, out vec3 indirect) {
    direct = vec3(0.0f);
    indirect = vec3(0.0f);

    vec3 center = ph_reproject_player_pos(frag_player_pos, frag_is_hand, get_taa_jitter());
    if (any(lessThan(center.xy, vec2(0.0f))) || any(greaterThan(center.xy, vec2(1.0f)))) return false;
    center.xy = center.xy * PH_VIEW_SIZE - 0.5f;

    ivec2 texel = ivec2(floor(center.xy));
    vec2 mix_factors = fract(center.xy);

    const ivec2[4] offsets = ivec2[](ivec2(0, 0), ivec2(1, 0), ivec2(0, 1), ivec2(1, 1));
    const vec2[4] weights = vec2[](vec2(1.0f, 1.0f), vec2(0.0f, 1.0f), vec2(1.0f, 0.0f), vec2(0.0f, 0.0f));

    float weight_sum = 0.0f;

    for (int i = 0; i < 4; i++) {
        ivec2 p = texel + offsets[i];

        FragData prev_frag;
        frag_data_load_previous(prev_frag, p);
        if (!frag_data_is_in_world(prev_frag)) continue;
        if (frag_data_is_hand(prev_frag) != frag_is_hand) continue;

        if (dot(frag_data_geo_normal(prev_frag), frag_geo_normal) < 0.99f) continue;

        vec3 dist = frag_player_pos - frag_data_player_pos(prev_frag);
        if (abs(dot(dist, frag_geo_normal)) > 0.1f) continue;

        vec4 prev = texelFetch(prev_sharp_direct, p, 0);
        if (prev.a <= 0.0f) continue;

        vec2 mix_weights = abs(weights[i] - mix_factors);
        float weight = mix_weights.x * mix_weights.y + 0.0001f;

        direct += prev.rgb * weight;
        indirect += texelFetch(prev_cached_indirect, p, 0).rgb * weight;
        weight_sum += weight;
    }

    if (weight_sum <= 0.0f) return false;

    direct /= weight_sum;
    indirect /= weight_sum;
    return true;
}

// Adds the computed samples around the pixel on `surface` (scaled by `scale`) to the sums.
void accumulate(CacheSurface surface, uvec4 slots, float scale, inout vec3 direct, inout vec3 indirect, inout float weight_sum) {
    ivec2 cells[4];
    vec4 weights;
    ph_cache_corners(surface, cells, weights);
    weights *= scale;

    for (int i = 0; i < 4; i++) {
        if (weights[i] <= 0.0f) continue;
        if (slots[i] == ph_cache_none) {
            PH_PROFILE_ADD(PH_STAT_SLOT_NONE, 1);
            continue;
        }

        // The slot may have been given to another key since the request pass (rare races).
        if (!ph_cache_matches(slots[i], ph_cache_key(surface, cells[i]))) {
            PH_PROFILE_ADD(PH_STAT_SLOT_MISMATCH, 1);
            continue;
        }
        if (!ph_cache_is_computed(slots[i])) {
            PH_PROFILE_ADD(PH_STAT_NOT_COMPUTED, 1);
            continue;
        }

        vec3 sample_direct;
        vec3 sample_indirect;
        ph_cache_load(slots[i], sample_direct, sample_indirect);

        direct += sample_direct * weights[i];
        indirect += sample_indirect * weights[i];
        weight_sum += weights[i];
    }
}

void main() {
    sharp_direct_out = vec4(0.0f);
    cached_indirect_out = vec4(0.0f);

    setup_frag_data(0);
    if (!frag_is_in_world) return;

    CacheSurface surface = ph_cache_pixel_surface();

    float finer_weight;
    CacheSurface finer = ph_cache_finer(surface, finer_weight);

    vec3 direct = vec3(0.0f);
    vec3 indirect = vec3(0.0f);
    float weight_sum = 0.0f;

    PH_PROFILE_MAX(PH_STAT_VIEW_W_MAX, uint(gl_FragCoord.x) + 1u);
    PH_PROFILE_MAX(PH_STAT_VIEW_H_MAX, uint(gl_FragCoord.y) + 1u);

    accumulate(surface, texelFetch(cache_slots, frag_tex_coord, 0), 1.0f - finer_weight, direct, indirect, weight_sum);
    if (finer_weight > 0.0f)
        accumulate(finer, texelFetch(cache_slots_fine, frag_tex_coord, 0), finer_weight, direct, indirect, weight_sum);

    // The corner weights add up to 1, so weight_sum is the share of the pixel the cache covers.
    float coverage = clamp(weight_sum, 0.0f, 1.0f);

    if (weight_sum > 0.0001f) {
        direct /= weight_sum;
        indirect /= weight_sum;
    }

    // The hand moves with the camera; its lighting is a single coarse sample and needs no smoothing.
    vec3 history_direct;
    vec3 history_indirect;
    // Only needed where samples are missing; fully covered pixels skip the reprojection.
    bool has_history = !frag_is_hand && coverage < 0.999f && load_history(history_direct, history_indirect);

    if (coverage >= 0.999f) PH_PROFILE_ADD(PH_STAT_COVERED, 1);
    else if (coverage > 0.0f) PH_PROFILE_ADD(PH_STAT_PARTIAL, 1);
    else PH_PROFILE_ADD(PH_STAT_UNCOVERED, 1);
    if (has_history) PH_PROFILE_ADD(PH_STAT_HISTORY, 1);
    else if (coverage < 0.999f) PH_PROFILE_ADD(PH_STAT_FALLBACK, 1);

#if defined PH_CACHE_COMBINED_GI
    if (has_history) history_direct = max(history_direct - history_indirect, vec3(0.0f));
#endif

    if (coverage >= 0.999f) {
        // All samples available: use them as they are. Cached direct light is deterministic, so it
        // needs no temporal smoothing, and re-sampling the previous frame while the camera moves left
        // stripes (lines that showed most when looking or moving sideways).
    } else if (has_history) {
        // Missing samples keep the previous result until they are computed.
        direct = mix(history_direct, direct, coverage);
        indirect = mix(history_indirect, indirect, coverage);
    } else {
        vec3 position = frag_is_hand ? rt_camera_position : frag_data_rt_pos(_frag_data);
        vec3 fallback = ph_cache_direct_light(position + frag_geo_normal * 0.05f, frag_geo_normal, ph_cache_fallback_lights);

        direct = mix(fallback, direct, coverage);
        // No stand-in for GI: it fades in with the samples.
        indirect *= coverage;
    }

#if defined PH_CACHE_COMBINED_GI
    // Combined GI is added to the direct light only on output, so the history keeps them apart.
    sharp_direct_out = vec4(direct + indirect, 1.0f);
#else
    sharp_direct_out = vec4(direct, 1.0f);
#endif
    cached_indirect_out = vec4(indirect, 1.0f);

#if PH_CACHE_DEBUG_VIEW == 1
    // Cache status: green = all samples available, yellow = some, red = none (keeping the previous
    // lighting), magenta = none and no previous lighting (stand-in lighting), blue = the hand.
    vec3 status = frag_is_hand ? vec3(0.1f, 0.3f, 1.0f)
                : coverage >= 0.999f ? vec3(0.1f, 1.0f, 0.1f)
                : coverage > 0.0f ? vec3(1.0f, 0.9f, 0.1f)
                : has_history ? vec3(1.0f, 0.1f, 0.1f) : vec3(1.0f, 0.1f, 1.0f);
    sharp_direct_out = vec4(status * 2.0f, 1.0f);
    cached_indirect_out = vec4(0.0f, 0.0f, 0.0f, 1.0f);
#elif PH_CACHE_DEBUG_VIEW == 2
    // Detail level: red = 1 sample per block edge, yellow = 2, green = 4, cyan = 8, blue = coarse.
    const vec3 level_colors[4] = vec3[](vec3(1.0f, 0.1f, 0.1f), vec3(1.0f, 0.9f, 0.1f), vec3(0.1f, 1.0f, 0.1f), vec3(0.1f, 1.0f, 1.0f));
    vec3 level_color = surface.coarse ? vec3(0.2f, 0.3f, 1.0f)
                     : mix(level_colors[clamp(surface.level, 0, 3)], level_colors[clamp(finer.level, 0, 3)], finer_weight);
    sharp_direct_out = vec4(level_color * 2.0f, 1.0f);
    cached_indirect_out = vec4(0.0f, 0.0f, 0.0f, 1.0f);
#endif
}
