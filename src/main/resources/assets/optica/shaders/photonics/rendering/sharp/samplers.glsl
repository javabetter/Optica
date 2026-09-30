// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
// Optica: samplers for the BASIC ("sharp") lighting mode.

//ph_required: uniform sampler2D sharp_direct;

#include "/photonics/rendering/upsample.glsl"

#if defined PH_ENABLE_HANDHELD_LIGHT
//ph_required: uniform bool off_hand_has_light, main_hand_has_light;
//ph_required: uniform sampler2D handheld_diffuse;
#endif

vec3 sample_photonics_direct(vec2 tex_coord) {
#if defined PH_ENABLE_BLOCKLIGHT
    PhUpsample u = ph_upsample(tex_coord);
    #define PH_FETCH_SHARP(t) texelFetch(sharp_direct, t, 0).rgb
    return PH_UPSAMPLE_FETCH(u, PH_FETCH_SHARP);
    #undef PH_FETCH_SHARP
#else
    return vec3(0.0f);
#endif
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
