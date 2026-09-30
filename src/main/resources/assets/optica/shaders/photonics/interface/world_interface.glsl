//HEAD

//TODO: DEPRECATED; REMOVE IN FUTURE RELEASE
//ph_required: uniform sampler2D depthtex1;

#include "/photonics/deprecated/shader_interface.glsl"

//bool is_in_world();

// Optica: route through the Photonics 0.3.x ph_is_hand() hook. Packs override it (EP rescales the fetch
// for PHOTONICS_RENDER_SCALE); the old direct fetch read the wrong pixel when the render scale was < 1.
#include "/photonics/internal/impl/is_hand.glsl"

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

