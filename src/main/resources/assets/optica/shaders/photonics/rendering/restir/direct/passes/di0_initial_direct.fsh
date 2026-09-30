#version 430

#define FRAG_USE_RT_POS
#define FRAG_USE_GEO_NORMAL
#define FRAG_USE_TEX_NORMAL

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/direct/reservoir.glsl"

layout(location = DIRECT_RESERVOIR_0) out vec4 di_reservoir_0;

void main() {
    setup_frag_data(0);
    if (!frag_is_in_world) discard;

    DirectReservoir reservoir = direct_reservoir_empty();

    float sample_weight = 0.0f;

    // Optica: level of detail by distance. Fewer light candidates far away; the visibility of the chosen
    // light is still checked in di3 (the pre-check here only helps reuse).
    float distance = length(frag_player_pos);
    int initial_samples = distance < 32.0f ? PH_RESTIR_INITIAL_SAMPLES
                        : distance < 64.0f ? max(PH_RESTIR_INITIAL_SAMPLES / 2, 1)
                        : max(PH_RESTIR_INITIAL_SAMPLES / 4, 1);

    if (light_list_size > 0) {
        for (int i = 0; i < initial_samples; i++) {
            DirectSample smple = direct_sample_random(frag_rnd_state);
            float weight = direct_sample_init_weight(smple, frag_rt_pos, frag_geo_normal, frag_tex_normal, frag_rnd_state);

            if (direct_reservoir_update(reservoir, smple, weight, 1.0f))
            sample_weight = weight;
        }
    }

    direct_reservoir_finalize_weight(reservoir, sample_weight);
    if (distance < 48.0f || frag_is_hand)
        direct_reservoir_validate_visiblity(reservoir, frag_rt_pos);
    direct_reservoir_encode(reservoir, di_reservoir_0);
}
