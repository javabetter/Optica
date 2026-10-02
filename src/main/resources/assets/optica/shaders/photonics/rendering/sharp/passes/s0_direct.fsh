#version 430

// Optica: BASIC ("sharp") direct lighting. For each fragment, pick the PH_MAX_SAMPLES lights with the
// largest unshadowed contribution from the light bins, then trace a shadow ray to each one.
//
// Like Photonics 0.3.x, the result is cached over time: a pixel whose reprojected history is valid
// reuses it, and only re-traces its lights once every PH_SHARP_REFRESH_INTERVAL frames (interleaved
// over a 4x4 tile, longer far away). Newly revealed pixels are traced immediately.

#define FRAG_USE_RT_POS
#define FRAG_USE_GEO_NORMAL
#define FRAG_USE_TEX_NORMAL

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/utility/projection.glsl"
#include "/photonics/light_list.glsl"
#include "/photonics/tracing.glsl"
#include "/photonics/utility/color.glsl"
#include "/photonics/rendering/sharp/light_bins.glsl"
#include "/photonics/utility/lod.glsl"

#ifndef PH_MAX_SAMPLES
#define PH_MAX_SAMPLES 20
#endif

// Upstream Photonics always traced shadow rays with 100 steps (it ignored the parameter); fewer can
// make long rays give up and count as shadowed.
#define PH_SHARP_SHADOW_ITERATIONS 100

#ifndef PH_SHARP_REFRESH_INTERVAL
#define PH_SHARP_REFRESH_INTERVAL 1
#endif

uniform sampler2D prev_sharp_direct;

layout(location = 0) out vec4 sharp_direct_out;

// Reprojects last frame's result. Returns false when no neighbouring texel is the same surface.
bool load_history(out vec3 history) {
    history = vec3(0.0f);

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

        history += prev.rgb * weight;
        weight_sum += weight;
    }

    if (weight_sum <= 0.0f) return false;

    history /= weight_sum;
    return true;
}

void main() {
    sharp_direct_out = vec4(0.0f);

    setup_frag_data(0);
    if (!frag_is_in_world) return;

    sharp_direct_out = vec4(0.0f, 0.0f, 0.0f, 1.0f);
    if (light_list_size <= 0) return;

    // Optica: level of detail by distance. Far pixels refresh less often and consider fewer lights;
    // at a distance neither the latency nor the missing dim lights are noticeable.
    float distance = ph_lod_distance(frag_player_pos);
    int refresh_interval = distance < 32.0f ? PH_SHARP_REFRESH_INTERVAL
                         : distance < 80.0f ? PH_SHARP_REFRESH_INTERVAL * 2
                         : PH_SHARP_REFRESH_INTERVAL * 4;
    int max_samples = distance < 32.0f ? PH_MAX_SAMPLES
                    : distance < 64.0f ? max((PH_MAX_SAMPLES * 3) / 4, 6)
                    : max(PH_MAX_SAMPLES / 2, 4);
    max_samples = min(max_samples, PH_MAX_SAMPLES);

    ivec2 pixel = ivec2(gl_FragCoord.xy);
    int phase = (pixel.x & 3) | ((pixel.y & 3) << 2); // interleaved over a 4x4 tile
    bool refresh = frag_is_hand || ((frameCounter + phase) % refresh_interval) == 0;

    vec3 history = vec3(0.0f);
    if (!refresh && load_history(history)) {
        sharp_direct_out = vec4(history, 1.0f);
        return;
    }

    vec3 tex_normal = frag_is_hand ? frag_geo_normal : frag_tex_normal;

    int first, last;
    if (!light_bins_lookup(frag_rt_pos, first, last)) return;

    // Keep the PH_MAX_SAMPLES most important lights (insertion into a small unsorted set).
    int chosen_index[PH_MAX_SAMPLES];
    float chosen_weight[PH_MAX_SAMPLES];
    int chosen_count = 0;
    int weakest = 0;

    for (int i = first; i < last; i++) {
        int light_index = ph_light_bins_array[i];
        if (light_index < 0 || light_index >= light_list_size) continue;

        Light light = light_list_get(light_index);
        float weight = ph_luminance(light_sample_at(light, frag_rt_pos, light.position, frag_geo_normal, tex_normal));
        if (weight < 0.0001f) continue;

        if (chosen_count < max_samples) {
            chosen_index[chosen_count] = light_index;
            chosen_weight[chosen_count] = weight;
            if (weight < chosen_weight[weakest]) weakest = chosen_count;
            chosen_count++;
            continue;
        }

        if (weight <= chosen_weight[weakest]) continue;

        chosen_index[weakest] = light_index;
        chosen_weight[weakest] = weight;

        for (int k = 0; k < max_samples; k++)
            if (chosen_weight[k] < chosen_weight[weakest]) weakest = k;
    }

    vec3 total = vec3(0.0f);

    for (int k = 0; k < chosen_count; k++) {
        Light light = light_list_get(chosen_index[k]);
        vec3 color = light_sample_at(light, frag_rt_pos, light.position, frag_geo_normal, tex_normal);

        // Fragments inside the light's own block (e.g. the light's faces) are always lit.
        if (floor(light.position) == floor(frag_rt_pos)) {
            total += color;
            continue;
        }

        vec3 tint_color;
        float light_transmittance;
        if (trace_light_vis(frag_rt_pos, light.position - frag_rt_pos, light.position, PH_SHARP_SHADOW_ITERATIONS, tint_color, light_transmittance))
            total += color * tint_color * light_transmittance;
    }

    sharp_direct_out = vec4(total, 1.0f);
}
