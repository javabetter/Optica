//HEAD

//TODO: DEPRECATED; REMOVE IN FUTURE RELEASE
#define NO_SHADOW_MAPPING

vec3 get_sun_direction() {
    return sun_direction;
}

#ifdef PH_PACK_HAS_NO_INDIRECT_LIGHT_COLOR
// Optica: the pack declares no indirect_light_color (ShaderPatcher.adaptForPack); use its 0.3.x
// get_sky_color() for the light reaching a surface from the sky.
vec3 ph_pack_indirect_light_color(vec3 player_pos, vec3 direction) {
    return get_sky_color(ivec2(gl_FragCoord.xy), player_pos + cameraPosition, direction);
}
#else
// Optica: packs may set indirect_light_color in load_fragment_variables() (Photon does) instead of
// where they declare it. Photonics' GI pass always called that first; Optica's reads the fragment from
// fast_frag_data instead, which left the sky light at zero (pitch black shadows), so call it once here.
bool ph_indirect_light_color_loaded = false;

vec3 ph_pack_indirect_light_color(vec3 player_pos, vec3 direction) {
    if (!ph_indirect_light_color_loaded) {
        ph_indirect_light_color_loaded = true;
        vec3 ph_unused_albedo, ph_unused_pos, ph_unused_normal, ph_unused_mapped;
        load_fragment_variables(ph_unused_albedo, ph_unused_pos, ph_unused_normal, ph_unused_mapped);
    }
    return indirect_light_color;
}
#endif

vec3 get_sun_color(vec3 player_pos, vec3 direction) {
    return ph_pack_indirect_light_color(player_pos, direction) * 4.0f;
}

vec3 get_sky_color(vec3 player_pos, vec3 direction) {
    return ph_pack_indirect_light_color(player_pos, direction);
}

bool sample_sun_color(vec3 player_pos, vec3 geo_normal, inout vec3 sun_color) {
    return true;
}
