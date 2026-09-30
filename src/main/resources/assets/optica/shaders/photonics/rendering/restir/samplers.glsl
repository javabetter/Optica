#if PH_RESTIR_DENOISER_PASSES > 0
//ph_required: uniform usampler2D denoise_result;
#else
//ph_required: uniform usampler2D diffuse_history;
#endif

#include "/photonics/rendering/upsample.glsl"

#if defined PH_ENABLE_HANDHELD_LIGHT
//ph_required: uniform bool off_hand_has_light, main_hand_has_light;
//ph_required: uniform sampler2D handheld_diffuse;
#endif

// Optica: depth-aware upsampling (see upsample.glsl); these are integer textures, which were fetched
// with no filtering at all before.
vec3 sample_photonics_direct(vec2 tex_coord) {
    PhUpsample u = ph_upsample(tex_coord);
#if PH_RESTIR_DENOISER_PASSES > 0
    #define PH_FETCH_RESTIR(t) uintBitsToFloat(texelFetch(denoise_result, t, 0).rgb)
#else
    #define PH_FETCH_RESTIR(t) uintBitsToFloat(texelFetch(diffuse_history, t, 0).rgb)
#endif
    return PH_UPSAMPLE_FETCH(u, PH_FETCH_RESTIR);
    #undef PH_FETCH_RESTIR
}

vec3 sample_photonics_handheld(vec2 tex_coord) {
#if defined PH_ENABLE_HANDHELD_LIGHT
    if (!main_hand_has_light && !off_hand_has_light) return vec3(0.0f);

    PhUpsample u = ph_upsample(tex_coord);
    #define PH_FETCH_HANDHELD(t) texelFetch(handheld_diffuse, t, 0).rgb
    return PH_UPSAMPLE_FETCH(u, PH_FETCH_HANDHELD);
    #undef PH_FETCH_HANDHELD
#else
    return vec3(0.0f);
#endif
}
