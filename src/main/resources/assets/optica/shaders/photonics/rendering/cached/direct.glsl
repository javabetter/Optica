#ifndef PH_CACHE_DIRECT_INCLUDE
#define PH_CACHE_DIRECT_INCLUDE

// Optica cached lighting: BASIC-style direct light at a point (the brightest lights that reach it, each
// with a shadow ray). Used to compute cache samples (c1) and as the stand-in for pixels whose samples
// are not computed yet (c2).

#include "/photonics/light_list.glsl"
#include "/photonics/tracing.glsl"
#include "/photonics/utility/color.glsl"
#include "/photonics/rendering/sharp/light_bins.glsl"

#ifndef PH_MAX_SAMPLES
#define PH_MAX_SAMPLES 20
#endif

const int ph_cache_shadow_iterations = 64;

vec3 ph_cache_direct_light(vec3 rt_pos, vec3 normal, int max_lights) {
#if defined PH_ENABLE_BLOCKLIGHT
    if (light_list_size <= 0) return vec3(0.0f);

    int first, last;
    if (!light_bins_lookup(rt_pos, first, last)) return vec3(0.0f);

    // The max_lights (at most PH_MAX_SAMPLES) lights with the largest unshadowed contribution, as in
    // BASIC mode.
    max_lights = clamp(max_lights, 1, PH_MAX_SAMPLES);
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

        if (chosen_count < max_lights) {
            chosen_index[chosen_count] = light_index;
            chosen_weight[chosen_count] = weight;
            if (weight < chosen_weight[weakest]) weakest = chosen_count;
            chosen_count++;
            continue;
        }

        if (weight <= chosen_weight[weakest]) continue;

        chosen_index[weakest] = light_index;
        chosen_weight[weakest] = weight;

        for (int k = 0; k < max_lights; k++)
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
        if (trace_light_vis(rt_pos, light.position - rt_pos, light.position, ph_cache_shadow_iterations, tint_color, light_transmittance))
            total += color * tint_color * light_transmittance;
    }

    return total;
#else
    return vec3(0.0f);
#endif
}

#endif
