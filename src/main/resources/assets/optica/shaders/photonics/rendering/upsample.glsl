#ifndef PH_UPSAMPLE_INCLUDE
#define PH_UPSAMPLE_INCLUDE

// Optica: depth-aware upsampling for lighting rendered at PH_RENDER_SCALE < 1.
//
// Plain bilinear filtering blends lighting across silhouettes (a mob in front of a wall gets some of
// the wall's light, and the other way round) and makes the low resolution visible as jagged edges.
// Each full-resolution pixel instead weights the four nearest low-resolution texels by how close their
// depth is to its own, so light only comes from the same surface.

// The pack's depth texture. A pack can replace depthtex0 with its own texture in some stages (Photon puts
// a 3D atmosphere table there in deferred); Optica then picks another one (ShaderPatcher.adaptForPack).
#if !defined PH_UPSAMPLE_DEPTH
//ph_required: uniform sampler2D depthtex0;
#define PH_UPSAMPLE_DEPTH depthtex0
#elif PH_UPSAMPLE_DEPTH_INDEX == 1
//ph_required: uniform sampler2D depthtex1;
#else
//ph_required: uniform sampler2D depthtex2;
#endif
//ph_required: uniform sampler2D fast_frag_data;
//ph_required: uniform float near, far;

struct PhUpsample {
    ivec2 texels[4];
    vec4 weights;
    bool exact; // same resolution: a single texel with weight 1
};

PhUpsample ph_upsample(vec2 tex_coord) {
    PhUpsample result;

    ivec2 low_size = textureSize(fast_frag_data, 0);
    ivec2 full_size = textureSize(PH_UPSAMPLE_DEPTH, 0);

    if (low_size == full_size) {
        ivec2 texel = clamp(ivec2(tex_coord * vec2(low_size)), ivec2(0), low_size - 1);
        result.texels = ivec2[4](texel, texel, texel, texel);
        result.weights = vec4(1.0f, 0.0f, 0.0f, 0.0f);
        result.exact = true;
        return result;
    }

    result.exact = false;

    float depth = texture(PH_UPSAMPLE_DEPTH, tex_coord).r;
    float linear_depth = near * far / (far + depth * (near - far));
    float tolerance = max(linear_depth, 0.05f) * 0.03f;

    vec2 p = tex_coord * vec2(low_size) - 0.5f;
    ivec2 base = ivec2(floor(p));
    vec2 f = p - vec2(base);

    const ivec2[4] offsets = ivec2[](ivec2(0, 0), ivec2(1, 0), ivec2(0, 1), ivec2(1, 1));
    vec4 bilinear = vec4((1.0f - f.x) * (1.0f - f.y), f.x * (1.0f - f.y), (1.0f - f.x) * f.y, f.x * f.y);

    float best_diff = 1e30f;
    int best = 0;

    for (int i = 0; i < 4; i++) {
        ivec2 texel = clamp(base + offsets[i], ivec2(0), low_size - 1);
        result.texels[i] = texel;

        float diff = abs(texelFetch(fast_frag_data, texel, 0).x - linear_depth);
        if (diff < best_diff) {
            best_diff = diff;
            best = i;
        }

        result.weights[i] = bilinear[i] * exp(-diff / tolerance);
    }

    float sum = dot(result.weights, vec4(1.0f));
    if (sum < 1e-4f) {
        // No low-resolution sample lies on this surface (thin geometry): take the closest in depth.
        result.weights = vec4(0.0f);
        result.weights[best] = 1.0f;
    } else {
        result.weights /= sum;
    }

    return result;
}

#define PH_UPSAMPLE_FETCH(u, FETCH) ( \
    (u).exact ? FETCH((u).texels[0]) : \
    FETCH((u).texels[0]) * (u).weights.x + FETCH((u).texels[1]) * (u).weights.y + \
    FETCH((u).texels[2]) * (u).weights.z + FETCH((u).texels[3]) * (u).weights.w)

#endif
