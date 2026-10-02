#version 430

// Optica cached lighting, pass 4 of 4: hand the cached GI to the pack's write_indirect() (the Photonics
// 0.3.x API). Renders into the pack's own framebuffer, as declared by its write_indirect.glsl.
//
// Cached GI is a running average of a few random rays per sample, so neighbouring samples differ a
// little, and interpolating between them turns that noise into soft blotches the size of the sample
// spacing. An edge-aware blur (as for the legacy GI) evens it out. Its step follows the size of a
// cache sample on screen, so it covers about two samples in each direction near and far.

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/write_indirect.glsl"
#include "/photonics/rendering/cached/surface.glsl"

//ph_required: uniform float viewHeight;
//ph_required: uniform mat4 gbufferProjection;

uniform sampler2D cached_indirect;

// Blur step in texels: half a sample spacing, so the 5x5 taps span about two samples each way.
int blur_stride() {
    if (frag_is_hand) return 1;

    CacheSurface surface = ph_cache_pixel_surface();
    float samples_per_block = float(1 << surface.level);

    vec3 view = frag_player_pos;
    float distance = max(length(view), 1e-3f);
    float facing = max(abs(dot(frag_geo_normal, view)) / distance, 0.3f);

    // Size of one texel of this pass in blocks, on the surface.
    float texel_size = 2.0f * distance / max(gbufferProjection[1][1] * viewHeight * PH_RENDER_SCALE, 1.0f) / facing;
    float spacing_texels = 1.0f / (samples_per_block * texel_size);

    return clamp(int(round(0.5f * spacing_texels)), 1, 8);
}

void main() {
    setup_frag_data(0);

    if (!frag_is_in_world) {
        write_indirect(vec3(0.0f));
        return;
    }

    int stride = blur_stride();

    vec3 sum = vec3(0.0f);
    float weight_sum = 0.0f;

    for (int y = -2; y <= 2; y++) {
        for (int x = -2; x <= 2; x++) {
            ivec2 p = frag_tex_coord + ivec2(x, y) * stride;

            FragData other;
            frag_data_load(other, p);
            if (!frag_data_is_in_world(other)) continue;
            // The hand is not part of the world: never blend its GI with what is behind it.
            if (frag_data_is_hand(other) != frag_is_hand) continue;

            float normal_weight = pow(max(dot(frag_data_geo_normal(other), frag_geo_normal), 0.0f), 16.0f);
            float plane_weight = exp(-abs(dot(frag_data_player_pos(other) - frag_player_pos, frag_geo_normal)) * 4.0f);
            float weight = normal_weight * plane_weight * exp(-0.25f * float(x * x + y * y));

            sum += texelFetch(cached_indirect, p, 0).rgb * weight;
            weight_sum += weight;
        }
    }

    write_indirect(weight_sum > 0.0001f ? sum / weight_sum : texelFetch(cached_indirect, frag_tex_coord, 0).rgb);
}
