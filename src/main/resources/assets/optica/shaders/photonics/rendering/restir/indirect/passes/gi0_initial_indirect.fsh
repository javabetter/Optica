#version 430

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/rendering/restir/indirect/reservoir.glsl"
#include "/photonics/rendering/indirect_lighting.glsl"
#include "/photonics/utility/lod.glsl"

layout(location = INDIRECT_RESERVOIR_0) out vec4 gi_reservoir_0;
layout(location = INDIRECT_RESERVOIR_1) out uvec3 gi_reservoir_1;

void main() {
    setup_frag_data(0);
    if (!frag_is_in_world) discard;

    // Optica: level of detail by distance. Far pixels trace a GI path every 2nd/4th frame (interleaved);
    // on the other frames they contribute no new sample and temporal reuse (gi1) carries the history.
    float distance = ph_lod_distance(frag_player_pos);
    int interval = distance < 48.0f ? 1 : distance < 96.0f ? 2 : 4;
    ivec2 pixel = ivec2(gl_FragCoord.xy);
    int phase = (pixel.x & 1) | ((pixel.y & 1) << 1);

    if (!frag_is_hand && ((frameCounter + phase) % interval) != 0) {
        indirect_reservoir_encode(indirect_reservoir_empty(), gi_reservoir_0, gi_reservoir_1);
        return;
    }

    vec3 indirect_result = vec3(0.0f);
    vec3 hit_normal;
    vec3 hit_position;

    // Needs this for compatability
    uint rnd_state = frag_rnd_state;
    sample_indirect(
            indirect_result,
            frag_rt_pos,
            frag_tex_normal,
            rnd_state,

            hit_position,
            hit_normal
    );

    indirect_result *= get_exposure();

    IndirectReservoir reservoir = indirect_reservoir_empty();
    indirect_sample_set_color(reservoir.smple, indirect_result);
    indirect_sample_set_hit_normal(reservoir.smple, hit_normal);
    indirect_sample_set_hit_point(reservoir.smple, hit_position, frag_rt_pos, frag_tex_normal, frag_rnd_state);

    reservoir.weight = indirect_sample_weight(reservoir.smple);
    reservoir.total_samples = 1.0f;

    indirect_reservoir_finalize_weight(reservoir, reservoir.weight);
    indirect_reservoir_encode(reservoir, gi_reservoir_0, gi_reservoir_1);
}
