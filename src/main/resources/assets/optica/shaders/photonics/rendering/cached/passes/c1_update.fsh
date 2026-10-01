#version 430

// Optica cached lighting, pass 2 of 4: compute cache samples. Runs on a fixed 512 x 192 grid, one
// sample per pixel: first the samples created this frame, then a slice of the table so that every
// sample in use is recomputed about once per PH_CACHE_REFRESH_SECONDS.

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/light_list.glsl"
#include "/photonics/tracing.glsl"
#include "/photonics/utility/color.glsl"
#include "/photonics/rendering/sharp/light_bins.glsl"
#include "/photonics/rendering/indirect_lighting.glsl"
#include "/photonics/rendering/cached/cache.glsl"

//ph_required: uniform float frameTime;

#ifndef PH_MAX_SAMPLES
#define PH_MAX_SAMPLES 20
#endif

#ifndef PH_CACHE_REFRESH_SECONDS
#define PH_CACHE_REFRESH_SECONDS 1.0
#endif

#ifndef PH_CACHE_GI_SAMPLES
#define PH_CACHE_GI_SAMPLES 2
#endif

#define PH_CACHE_SHADOW_ITERATIONS 64
// GI is averaged over this many updates, so it converges over a few refreshes and still adapts.
const float ph_cache_gi_history = 8.0f;
// Samples not used for this many frames are not refreshed (they are off screen).
const uint ph_cache_refresh_max_age = 120u;
// Sample points sit slightly in front of their face.
const float ph_cache_surface_offset = 0.05f;

layout(location = 0) out float scratch_out;

vec3 cache_direct_light(vec3 rt_pos, vec3 normal) {
#if defined PH_ENABLE_BLOCKLIGHT
    if (light_list_size <= 0) return vec3(0.0f);

    int first, last;
    if (!light_bins_lookup(rt_pos, first, last)) return vec3(0.0f);

    // The PH_MAX_SAMPLES lights with the largest unshadowed contribution (as in BASIC mode).
    int chosen_index[PH_MAX_SAMPLES];
    float chosen_weight[PH_MAX_SAMPLES];
    int chosen_count = 0;
    int weakest = 0;

    for (int i = first; i < last; i++) {
        int light_index = ph_light_bins_array[i];
        if (light_index < 0 || light_index >= light_list_size) continue;

        Light light = light_list_get(light_index);
        float weight = ph_luminance(light_sample_at(light, rt_pos, light.position, normal, normal));
        if (weight < 0.0001f) continue;

        if (chosen_count < PH_MAX_SAMPLES) {
            chosen_index[chosen_count] = light_index;
            chosen_weight[chosen_count] = weight;
            if (weight < chosen_weight[weakest]) weakest = chosen_count;
            chosen_count++;
            continue;
        }

        if (weight <= chosen_weight[weakest]) continue;

        chosen_index[weakest] = light_index;
        chosen_weight[weakest] = weight;

        for (int k = 0; k < PH_MAX_SAMPLES; k++)
            if (chosen_weight[k] < chosen_weight[weakest]) weakest = k;
    }

    vec3 total = vec3(0.0f);

    for (int k = 0; k < chosen_count; k++) {
        Light light = light_list_get(chosen_index[k]);
        vec3 color = light_sample_at(light, rt_pos, light.position, normal, normal);

        if (floor(light.position) == floor(rt_pos)) {
            total += color;
            continue;
        }

        vec3 tint_color;
        float light_transmittance;
        if (trace_light_vis(rt_pos, light.position - rt_pos, light.position, PH_CACHE_SHADOW_ITERATIONS, tint_color, light_transmittance))
            total += color * tint_color * light_transmittance;
    }

    return total;
#else
    return vec3(0.0f);
#endif
}

vec3 cache_indirect_light(vec3 rt_pos, vec3 normal, uint slot) {
    vec3 total = vec3(0.0f);

#if defined PH_ENABLE_GI && PH_CACHE_GI_SAMPLES > 0
    for (int s = 0; s < PH_CACHE_GI_SAMPLES; s++) {
        uint rnd_state = ph_new_rand_state(vec2(slot & 0xFFFFu, slot >> 16), frameCounter, s);

        vec3 indirect = vec3(0.0f);
        vec3 hit_position;
        vec3 hit_normal;
        sample_indirect(indirect, rt_pos, normal, rnd_state, hit_position, hit_normal);

        if (!any(isnan(indirect)) && !any(isinf(indirect)))
            total += indirect;
    }

    total /= float(PH_CACHE_GI_SAMPLES);
#endif

    return total;
}

void main() {
    scratch_out = 0.0f;

    uint id = uint(gl_FragCoord.x) + uint(gl_FragCoord.y) * ph_cache_update_width;
    uint parity = ph_cache_parity();
    uint other = parity ^ 1u;

    uint new_count = min(ph_cache_state[parity], ph_cache_queue_max);

    // The share of the table to revisit this frame, so all of it is covered once per refresh period.
    uint threads = ph_cache_update_width * ph_cache_update_height;
    uint budget = uint(ceil(float(ph_cache_capacity) * clamp(frameTime, 0.0f, 1.0f) / float(PH_CACHE_REFRESH_SECONDS)));
    budget = min(budget, threads - min(new_count, threads));

    uint cursor = ph_cache_state[2u + parity];

    if (id == 0u) {
        // Next frame uses the other counters: empty its queue and advance its cursor.
        ph_cache_state[other] = 0u;
        ph_cache_state[2u + other] = (cursor + budget) & ph_cache_mask;
    }

    uint slot;
    bool is_new;

    if (id < new_count) {
        slot = ph_cache_state[ph_cache_state_header + id];
        is_new = true;
    } else {
        uint r = id - new_count;
        if (r >= budget) return;

        slot = (cursor + r) & ph_cache_mask;
        is_new = false;
    }

    uint base = ph_cache_base(slot);
    CacheKey key = CacheKey(ph_cache[base], ph_cache[base + 1u]);
    if (key.k1 == 0u) return;

    uint flags = ph_cache[base + 3u];
    bool computed = (flags & ph_cache_computed_bit) != 0u;

    // Refresh only samples that were on screen recently; new or never computed ones always.
    if (!is_new && computed && ph_cache_age(ph_cache[base + 2u]) > ph_cache_refresh_max_age) return;

    vec3 world_pos;
    vec3 normal;
    ph_cache_decode(key, world_pos, normal);

    // Samples sit at exact fractions of a block, and so do lights (block centres). Whole rows of samples
    // then cast their shadow rays exactly through the edge where two blocks touch diagonally (the steps
    // of the End fountain's rim); the tracer settles such ties on one side, so light leaked through those
    // edges as streaks, in two of the four diagonal directions. A tiny irregular in-plane offset moves
    // every sample off those lines.
    vec3 jitter = vec3(0.00137f, 0.00291f, 0.00213f);
    jitter -= normal * dot(jitter, normal);

    vec3 rt_pos = world_pos + jitter + normal * ph_cache_surface_offset - cameraPosition + rt_camera_position;

    vec3 direct = cache_direct_light(rt_pos, normal);
    vec3 indirect_sample = cache_indirect_light(rt_pos, normal, slot);

    vec3 old_direct;
    vec3 old_indirect;
    ph_cache_load(slot, old_direct, old_indirect);

    uint gi_samples = computed ? (flags & 0xFFFFu) : 0u;
    float blend = 1.0f / min(float(gi_samples) + 1.0f, ph_cache_gi_history);
    vec3 indirect = mix(old_indirect, indirect_sample, blend);

    ph_cache_store(slot, direct, indirect, gi_samples + 1u);
}
