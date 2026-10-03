#version 430

// Optica cached lighting, pass 2 of 4: compute cache samples. Runs on a fixed 512 x 192 grid, one
// sample per pixel: first the samples created this frame, then (with a Refresh Time set) a slice of the
// table so that every sample in use is recomputed about once per PH_CACHE_REFRESH_SECONDS.

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/indirect_lighting.glsl"
#include "/photonics/rendering/cached/cache.glsl"
#include "/photonics/rendering/cached/direct.glsl"

//ph_required: uniform float frameTime;

#ifndef PH_CACHE_REFRESH_SECONDS
#define PH_CACHE_REFRESH_SECONDS 0.0
#endif

#ifndef PH_CACHE_GI_SAMPLES
#define PH_CACHE_GI_SAMPLES 0
#endif

// GI is averaged over this many updates, so it converges over a few refreshes and still adapts.
const float ph_cache_gi_history = 32.0f;
// A sample's first GI estimate (new, or after a nearby change) uses this many times the rays, so it
// does not start out noisy.
const int ph_cache_gi_first_multiplier = 4;
// Samples not used for this many frames are not refreshed (they are off screen).
const uint ph_cache_refresh_max_age = 120u;
// Sample points sit slightly in front of their face.
const float ph_cache_surface_offset = 0.05f;

layout(location = 0) out float scratch_out;

vec3 cache_indirect_light(vec3 rt_pos, vec3 normal, uint slot, int rays) {
    vec3 total = vec3(0.0f);

#if defined PH_ENABLE_GI && PH_CACHE_GI_SAMPLES > 0
    for (int s = 0; s < PH_CACHE_GI_SAMPLES * ph_cache_gi_first_multiplier; s++) {
        if (s >= rays) break;
        uint rnd_state = ph_new_rand_state(vec2(slot & 0xFFFFu, slot >> 16), frameCounter, s);

        vec3 indirect = vec3(0.0f);
        vec3 hit_position;
        vec3 hit_normal;
        sample_indirect(indirect, rt_pos, normal, rnd_state, hit_position, hit_normal);

        if (!any(isnan(indirect)) && !any(isinf(indirect)))
            total += indirect;
    }

    total /= float(max(rays, 1));
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
    // Refresh Time "Only on Changes" (0): no timed refresh, samples are recomputed only after a change.
    uint budget = PH_CACHE_REFRESH_SECONDS > 0.0
        ? uint(ceil(float(ph_cache_capacity) * clamp(frameTime, 0.0f, 1.0f) / float(PH_CACHE_REFRESH_SECONDS)))
        : 0u;
    budget = min(budget, threads - min(new_count, threads));

    uint cursor = ph_cache_state[2u + parity];

    if (id == 0u) {
        // Next frame uses the other counters: empty its queue and advance its cursor.
        ph_cache_state[other] = 0u;
        ph_cache_state[2u + other] = (cursor + budget) & ph_cache_mask;

        PH_PROFILE_ADD(PH_STAT_FRAMES, 1);
        PH_PROFILE_ADD(PH_STAT_QUEUE_REQUESTED, ph_cache_state[parity]);
        PH_PROFILE_ADD(PH_STAT_QUEUE_PROCESSED, new_count);
        PH_PROFILE_ADD(PH_STAT_BUDGET, budget);
        PH_PROFILE_MAX(PH_STAT_LIGHTS_MAX, max(light_list_size, 0));
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
    if (key.k1 == 0u) {
        if (!is_new) PH_PROFILE_ADD(PH_STAT_CURSOR_EMPTY, 1);
        return;
    }

    uint flags = ph_cache[base + 3u];
    bool computed = (flags & ph_cache_computed_bit) != 0u;

    // Refresh only samples that were on screen recently; new or never computed ones always.
    if (!is_new && computed && ph_cache_age(ph_cache[base + 2u]) > ph_cache_refresh_max_age) {
        PH_PROFILE_ADD(PH_STAT_CURSOR_IDLE, 1);
        return;
    }

    if (!is_new) PH_PROFILE_ADD(PH_STAT_CURSOR_COMPUTED, 1);

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

    vec3 direct = ph_cache_direct_light(rt_pos, normal, PH_MAX_SAMPLES);
    if (all(equal(direct, vec3(0.0f)))) PH_PROFILE_ADD(PH_STAT_ZERO_DIRECT, 1);
    // GI is averaged over many updates; it starts over (with more rays) for new samples and after a
    // nearby block or light change, so it still follows changes quickly.
    bool restart_gi = !computed || (flags & ph_cache_reset_gi_bit) != 0u;
    uint gi_samples = restart_gi ? 0u : (flags & 0xFFFFu);
    int rays = PH_CACHE_GI_SAMPLES * (gi_samples == 0u ? ph_cache_gi_first_multiplier : 1);
    vec3 indirect_sample = cache_indirect_light(rt_pos, normal, slot, rays);

    vec3 old_direct;
    vec3 old_indirect;
    ph_cache_load(slot, old_direct, old_indirect);

    float blend = 1.0f / min(float(gi_samples) + 1.0f, ph_cache_gi_history);
    vec3 indirect = mix(old_indirect, indirect_sample, blend);

    ph_cache_store(slot, direct, indirect, gi_samples + 1u);
}
