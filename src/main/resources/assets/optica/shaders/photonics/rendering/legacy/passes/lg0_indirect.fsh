#version 430

// Optica: legacy (Photonics 0.3.x style) global illumination. Traces one GI path per fragment and
// accumulates it over time; lg1_write_indirect filters the result and hands it to the pack's
// write_indirect(). Used by BASIC mode, and by ReSTIR mode when combined GI is disabled.

#define FRAG_USE_RT_POS
#define FRAG_USE_GEO_NORMAL
#define FRAG_USE_TEX_NORMAL

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/utility/projection.glsl"
#include "/photonics/rendering/indirect_lighting.glsl"
#include "/photonics/utility/lod.glsl"

uniform sampler2D prev_legacy_gi;

layout(location = 0) out vec4 legacy_gi_out;

const float ph_legacy_gi_max_frames = 32.0f;

vec4 load_history() {
    vec3 center = ph_reproject_player_pos(frag_player_pos, frag_is_hand, get_taa_jitter());
    center.xy = center.xy * PH_VIEW_SIZE - 0.5f;

    ivec2 texel = ivec2(floor(center.xy));
    vec2 mix_factors = fract(center.xy);

    const ivec2[4] offsets = ivec2[](ivec2(0, 0), ivec2(1, 0), ivec2(0, 1), ivec2(1, 1));
    const vec2[4] weights = vec2[](vec2(1.0f, 1.0f), vec2(0.0f, 1.0f), vec2(1.0f, 0.0f), vec2(0.0f, 0.0f));

    vec4 history = vec4(0.0f);
    float weight_sum = 0.0f;

    for (int i = 0; i < 4; i++) {
        ivec2 p = texel + offsets[i];

        FragData prev_frag;
        frag_data_load_previous(prev_frag, p);
        if (!frag_data_is_in_world(prev_frag)) continue;
        if (frag_data_is_hand(prev_frag) != frag_is_hand) continue;

        if (dot(frag_data_geo_normal(prev_frag), frag_geo_normal) < 0.99f) continue;

        vec3 dist = frag_player_pos - frag_data_player_pos(prev_frag);
        if (abs(dot(dist, frag_geo_normal)) > 0.25f) continue;

        vec2 mix_weights = abs(weights[i] - mix_factors);
        float weight = mix_weights.x * mix_weights.y;

        history += texelFetch(prev_legacy_gi, p, 0) * weight;
        weight_sum += weight;
    }

    return weight_sum > 0.0001f ? history / weight_sum : vec4(0.0f);
}

void main() {
    legacy_gi_out = vec4(0.0f);

    setup_frag_data(1);
    if (!frag_is_in_world) return;

    vec4 history = load_history();

    // Optica: level of detail by distance. A GI path is traced every other frame nearby and more rarely
    // further away (interleaved over a 4x4 tile); in between the accumulated history is reused. Pixels
    // with little history (just revealed) always trace.
    float distance = ph_lod_distance(frag_player_pos);
    bool interleave = ph_lod_enabled;
#ifdef PH_CACHE_DETAIL
    // Lighting cache: the mode is about speed, so GI is always interleaved (by real distance when LOD
    // Quality is 1.0). Its history already averages 32 paths, so this mostly shows as slightly slower
    // GI changes.
    if (!ph_lod_enabled) distance = length(frag_player_pos);
    interleave = true;
#endif
    int interval = !interleave ? 1 : distance < 48.0f ? 2 : distance < 96.0f ? 4 : 8;
#ifdef PH_CACHE_DETAIL
    // Profiled on a user's GPU, these paths were most of the lighting cache's frame time (6.8 of
    // 11.4 ms), so the cache traces half as often again.
    interval *= 2;
#endif
    ivec2 pixel = ivec2(gl_FragCoord.xy);
    int phase = (pixel.x & 3) | ((pixel.y & 3) << 2);

    if (!frag_is_hand && history.a >= 4.0f && ((frameCounter + phase) % interval) != 0) {
        legacy_gi_out = history;
        return;
    }

    vec3 indirect = vec3(0.0f);
    vec3 hit_position;
    vec3 hit_normal;

    sample_indirect(indirect, frag_rt_pos, frag_tex_normal, frag_rnd_state, hit_position, hit_normal);
    if (any(isnan(indirect)) || any(isinf(indirect))) indirect = vec3(0.0f);
    float frames = min(history.a + 1.0f, ph_legacy_gi_max_frames);

    legacy_gi_out = vec4(mix(history.rgb, indirect, 1.0f / frames), frames);
}
