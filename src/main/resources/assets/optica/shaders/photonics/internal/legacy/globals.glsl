#ifndef PH_LEGACY_GLOBALS_INCLUDE
#define PH_LEGACY_GLOBALS_INCLUDE

// Optica: globals from the Photonics 0.3.x shader API (ph_raytracing.glsl). Shader packs written for
// 0.3.x (e.g. Euphoria Patches) read these from their modifier overrides, so they must exist in every
// program that includes the palette/tracing code.

// The block id (blocks.properties) of the most recently hit/sampled voxel.
int result_block_id = -1;

// The cumulative tint applied by transparent voxels during the last legacy trace_ray call.
vec3 result_tint_color = vec3(1.0f);

// Packed per-face skylight of the last hit block, read through get_result_sky_light().
int ph_result_sky_brightness = 0;

// When set, legacy trace_ray stops once the ray leaves this block (rt space). ivec3(-9999) disables it.
ivec3 ray_constraint = ivec3(-9999);

#endif
