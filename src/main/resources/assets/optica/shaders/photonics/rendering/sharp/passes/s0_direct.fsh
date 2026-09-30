#version 430

// Optica: BASIC ("sharp") direct lighting. For each fragment, pick the PH_MAX_SAMPLES lights with the
// largest unshadowed contribution from the light bins, then trace a shadow ray to each one.

#define FRAG_USE_RT_POS
#define FRAG_USE_GEO_NORMAL
#define FRAG_USE_TEX_NORMAL

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/light_list.glsl"
#include "/photonics/tracing.glsl"
#include "/photonics/utility/color.glsl"
#include "/photonics/rendering/sharp/light_bins.glsl"

#ifndef PH_MAX_SAMPLES
#define PH_MAX_SAMPLES 20
#endif

#define PH_SHARP_SHADOW_ITERATIONS 40

layout(location = 0) out vec4 sharp_direct_out;

void main() {
    sharp_direct_out = vec4(0.0f, 0.0f, 0.0f, 1.0f);

    setup_frag_data(0);
    if (!frag_is_in_world || light_list_size <= 0) return;

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
