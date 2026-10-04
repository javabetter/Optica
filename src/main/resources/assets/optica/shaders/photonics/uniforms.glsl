#ifndef PH_UNIFORMS_INCLUDE
#define PH_UNIFORMS_INCLUDE

// tracing uniforms

//ph_required: uniform vec3 rt_camera_position;
//ph_required: uniform vec3 world_offset;
//ph_required: uniform vec3 delta_world_offset;
//ph_required: uniform vec3 world_max_block;
//ph_required: uniform vec3 world_min_block;
//ph_required: uniform vec3 world_tree_size;
//ph_required: uniform int world_block_scale_exp;


// light uniforms

//ph_required: uniform vec3 light_list_offset;
//ph_required: uniform int light_list_size;

//ph_required: uniform bool left_handed;

//ph_required: uniform bool off_hand_has_light;
//ph_required: uniform mat4 ph_off_hand_light;

//ph_required: uniform bool main_hand_has_light;
//ph_required: uniform mat4 ph_main_hand_light;

// Optica: Photonics 0.3.x names for the camera's world position, still used by packs written for it
// (e.g. Eclipse's shader_interface.glsl).
//ph_required: uniform vec3 cameraPosition;
//ph_required: uniform vec3 previousCameraPosition;
#ifndef world_camera_position
#define world_camera_position cameraPosition
#endif
#ifndef previous_world_camera_position
#define previous_world_camera_position previousCameraPosition
#endif

#endif
