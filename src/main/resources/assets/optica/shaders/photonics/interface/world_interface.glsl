//HEAD

//TODO: DEPRECATED; REMOVE IN FUTURE RELEASE
//ph_required: uniform sampler2D depthtex1;
// Optica: packs' interfaces read depthtex0 too (Eclipse, Shrimple) and expect Photonics to declare it.
//ph_required: uniform sampler2D depthtex0;

// Optica: marks Photonics' own passes, as opposed to the pack's programs (see photonics.glsl).
#ifndef PH_IN_PHOTONICS_PASS
#define PH_IN_PHOTONICS_PASS
#endif

#define PH_DEFER_LEGACY_API
#include "/photonics/deprecated/shader_interface.glsl"
#undef PH_DEFER_LEGACY_API

//bool is_in_world();

// Optica: route through the Photonics 0.3.x ph_is_hand() hook. Packs override it (EP rescales the fetch
// for PHOTONICS_RENDER_SCALE); the old direct fetch read the wrong pixel when the render scale was < 1.
#include "/photonics/internal/impl/is_hand.glsl"

// Optica: Photonics 0.3.x set a ph_frag_is_hand global in its passes; packs' light modifiers still use
// it (e.g. Eclipse).
#ifndef ph_frag_is_hand
#define ph_frag_is_hand ph_is_hand()
#endif

// Optica: the 0.3.x API the pack's interface asked for (see photonics.glsl), now that the pack's own
// helpers and ph_is_hand() exist.
#ifdef PH_LEGACY_API_REQUESTED
#include "/photonics/photonics.glsl"
#endif

bool is_hand_at() {
    return ph_is_hand();
}

vec3 load_player_position() {
    return load_world_position() - cameraPosition;
}

void load_fragment_data(
    out vec3 geometry_normal,
    out vec3 texture_normal
) {
    vec3 temp;

    load_fragment_variables(temp, temp, geometry_normal, texture_normal);
}

//vec2 get_taa_jitter();

