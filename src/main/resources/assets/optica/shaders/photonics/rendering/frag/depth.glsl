// Deprecated: Remove for 0.4 release

#if !defined PH_DEPTH_FETCH
#include "/photonics/modifiers/restir_denoiser_depth_fetch_modifier.glsl"

#ifdef PH_RESTIR_DENOISER_DEPTH_FETCH_MODIFIER_DISABLED
    // Optica: passes run at PH_RENDER_SCALE, depthtex0 is full resolution.
    #ifdef PH_RENDER_SCALE
    #define DEPTH_MODIFIER(p) ivec2(vec2(p) / PH_RENDER_SCALE + 0.5f)
    #else
    #define DEPTH_MODIFIER(p) p
    #endif
#else
#define DEPTH_MODIFIER(p) modify_denoiser_depth_fetch(p)
#endif

//ph_required: uniform sampler2D depthtex0;
float load_depth() {
    return texelFetch(depthtex0, DEPTH_MODIFIER(ivec2(gl_FragCoord.xy)), 0).r;
}
#endif
