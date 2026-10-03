#version 430

// Optica: filters the accumulated legacy GI and passes it to the shader pack's write_indirect()
// (the Photonics 0.3.x API). This pass renders into the pack's own framebuffer, as declared by the
// pack's write_indirect.glsl (e.g. /* RENDERTARGETS:9 */), or the pack writes an image itself.

#define FRAG_USE_RT_POS
#define FRAG_USE_GEO_NORMAL
#define FRAG_USE_TEX_NORMAL

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/write_indirect.glsl"
#include "/photonics/utility/lod.glsl"
#include "/photonics/rendering/frag/fast_data.glsl"

//ph_required: uniform mat4 gbufferModelView;
//ph_required: uniform mat4 gbufferProjection;

uniform sampler2D legacy_gi;

// View space position of a texel from its linear depth (projection offsets such as TAA jitter are
// ignored; this only weighs neighbours).
vec3 view_pos(ivec2 p, float linear_depth) {
    vec2 ndc = (vec2(p) + 0.5f) / PH_VIEW_SIZE * 2.0f - 1.0f;
    return vec3(ndc.x / gbufferProjection[0][0], ndc.y / gbufferProjection[1][1], -1.0f) * linear_depth;
}

void main() {
    setup_frag_data(0);
    if (!frag_is_in_world) {
        write_indirect(vec3(0.0f));
        return;
    }

    vec3 sum = vec3(0.0f);
    float weight_sum = 0.0f;

    // 5x5 edge-aware blur with a stride of 2 texels; beyond 64 blocks a 3x3 one with a stride of 3
    // (Optica LOD: far away GI detail is not visible, and this pass reads three textures per tap).
    bool far = ph_lod_distance(frag_player_pos) > 64.0f;
    int radius = far ? 1 : 2;
    int stride = far ? 3 : 2;

    // Optica: world pixels weigh their neighbours with the compact fast frag data (one RG32F fetch per
    // tap instead of the two full frag data textures); the hand, which fast data cannot tell apart,
    // keeps the full version.
    if (!frag_is_hand) {
        vec3 center_view = view_pos(frag_tex_coord, fast_frag_fetch(frag_tex_coord).depth);
        vec3 normal_view = mat3(gbufferModelView) * frag_geo_normal;

        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                ivec2 p = frag_tex_coord + ivec2(x, y) * stride;

                FastFrag other = fast_frag_fetch(p);
                if (!fast_frag_in_world(other)) continue;

                float normal_weight = pow(max(dot(fast_frag_geo_normal(other), frag_geo_normal), 0.0f), 16.0f);
                float plane_weight = exp(-abs(dot(view_pos(p, other.depth) - center_view, normal_view)) * 4.0f);
                float weight = normal_weight * plane_weight * exp(-0.1f * float(x * x + y * y) * float(stride * stride) / 4.0f);

                sum += texelFetch(legacy_gi, p, 0).rgb * weight;
                weight_sum += weight;
            }
        }

        write_indirect(weight_sum > 0.0001f ? sum / weight_sum : vec3(0.0f));
        return;
    }

    for (int y = -radius; y <= radius; y++) {
        for (int x = -radius; x <= radius; x++) {
            ivec2 p = frag_tex_coord + ivec2(x, y) * stride;

            FragData other;
            frag_data_load(other, p);
            if (!frag_data_is_in_world(other)) continue;
            // The hand is not part of the world: never blend its GI with what is behind it.
            if (frag_data_is_hand(other) != frag_is_hand) continue;

            float normal_weight = pow(max(dot(frag_data_geo_normal(other), frag_geo_normal), 0.0f), 16.0f);
            float plane_weight = exp(-abs(dot(frag_data_player_pos(other) - frag_player_pos, frag_geo_normal)) * 4.0f);
            float weight = normal_weight * plane_weight * exp(-0.1f * float(x * x + y * y) * float(stride * stride) / 4.0f);

            sum += texelFetch(legacy_gi, p, 0).rgb * weight;
            weight_sum += weight;
        }
    }

    write_indirect(weight_sum > 0.0001f ? sum / weight_sum : vec3(0.0f));
}
