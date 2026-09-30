#ifndef PH_IS_HAND_INCLUDE
#define PH_IS_HAND_INCLUDE

#include "/photonics/modifiers/is_hand_modifier.glsl"

#ifdef PH_IS_HAND_MODIFIER_DISABLED
//ph_required: uniform sampler2D depthtex0;
bool ph_is_hand() {
    #ifdef PH_RENDER_SCALE
    const float ph_rcp_render_scale = 1.0f / PH_RENDER_SCALE;
    #else
    const float ph_rcp_render_scale = 1.0f;
    #endif

    return texelFetch(depthtex0, ivec2(gl_FragCoord.xy * ph_rcp_render_scale + 0.5f), 0).x < 0.56;
}
#else
#define ph_is_hand modify_is_hand
#endif

#endif
