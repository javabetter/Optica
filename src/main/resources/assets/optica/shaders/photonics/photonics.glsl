#ifndef PH_LEGACY_API_INCLUDE
#define PH_LEGACY_API_INCLUDE

// Optica: the Photonics 0.3.x public shader API (RayJob, trace_ray, load_light, ...), implemented on
// top of the 0.4 tracing internals. Shader packs written for Photonics 0.3.x include this file (and
// ship an empty stub of it so they load without the mod); Optica always replaces the pack's copy.
//
// Reference behaviour: Photonics 0.3.5 ph_core.glsl / ph_raytracing.glsl.

#include "/photonics/uniforms.glsl"
#include "/photonics/light.glsl"
#include "/photonics/light_list.glsl"
#include "/photonics/tracing.glsl"
#include "/photonics/internal/legacy/globals.glsl"

struct RayJob {
    vec3 origin;          // Ray origin in rt space (world position - world_offset).
    vec3 direction;       // Ray direction.

    vec3 result_position; // Position of the hit voxel in rt space.
    vec3 result_normal;   // Surface normal of the hit voxel.
    vec3 result_color;    // Colour (albedo) of the hit voxel.
    bool result_hit;      // Whether the ray hit a voxel.
};

// 0.3.x let packs tweak the iteration budget of the next trace through this global.
int RAY_ITERATION_COUNT = 100;

const vec3 ph_legacy_miss_position = vec3(47823934.0f);

void ph_legacy_apply_tint(vec4 color) {
#if defined PH_USE_CUSTOM_ALPHA
    result_tint_color *= PH_ALPHA_FUNC(color);
#else
    result_tint_color *= mix(color.rgb, vec3(step(color.a, 0.5f)), abs(color.a * 2.0f - 1.0f));
#endif
}

void trace_ray(inout RayJob job, bool transparency) {
    job.direction = normalize(ph_signed_nudge(job.direction));
    job.result_hit = false;
    job.result_normal = vec3(0.0f);
    job.result_color = vec3(0.0f);

    result_tint_color = vec3(1.0f);
    result_block_id = -1;
    ph_result_sky_brightness = 0;

    RayIterator ray;
    ray_iter_begin(ray, job.origin, job.direction);
    ray.iterations = RAY_ITERATION_COUNT;

    RayResult hit = missed_ray_result();
    vec3 previous_tint = vec3(-1.0f);

    while (true) {
        hit = ray_iter_next(ray);
        if (!ray_result_is_hit(hit)) break;

        vec3 hit_position = ray_result_position(hit);
        if (ray_constraint != ivec3(-9999) && ivec3(floor(hit_position)) != ray_constraint) {
            hit = missed_ray_result();
            break;
        }

        if (transparency && ray_result_is_transparent(hit)) {
            vec4 color = voxel_data_albedo(ray_result_voxel_data(hit));
            if (color.rgb != previous_tint) {
                ph_legacy_apply_tint(color);
                previous_tint = color.rgb;
            }

    #ifdef PH_FULL_TRANSPARENCY
            ray_iter_skip_voxel(ray);
            ray_iter_offset_position(ray, ray.direction * 0.002f);
    #else
            // 0.3.x default: the first transparent voxel tints the ray, the rest of the block is skipped.
            ray_iter_skip_block(ray);
            ray_iter_offset_position(ray, ray.direction * 0.03f);
    #endif
            continue;
        }

        break;
    }

    if (!ray_result_is_hit(hit)) {
        job.result_position = ph_legacy_miss_position - world_offset;
        return;
    }

    VoxelData voxel_data = ray_result_voxel_data(hit);

    job.result_hit = true;
    job.result_position = ray_result_position(hit);
    job.result_normal = ray_result_normal(hit);
    job.result_color = voxel_data_albedo(voxel_data).rgb;

    result_block_id = voxel_data_block_id(voxel_data);
    ph_result_sky_brightness = int(ray_result_skylight(hit));
}

void trace_ray(inout RayJob job) {
    trace_ray(job, false);
}

// The skylight level (0-15) of the last hit. 0.4 stores the skylight of the face that was hit, so the
// normal argument is accepted for compatibility but not needed.
int get_result_sky_light(vec3 normal) {
    return ph_result_sky_brightness;
}

// The block id at rt_pos, or -1 (PH_AIR_ID with PH_USE_CUSTOM_AIR_ID) when the block is empty.
// 0.4 stores block ids per voxel rather than per block, so this probes the block with a short
// constrained ray down its centre column; very thin blocks off the centre read as empty.
int get_block_id(vec3 rt_pos) {
    vec3 block = floor(rt_pos);

    RayJob job = RayJob(block + vec3(0.5f, 0.999f, 0.5f), vec3(0.0f, -1.0f, 0.0f), vec3(0.0f), vec3(0.0f), vec3(0.0f), false);

    ivec3 previous_constraint = ray_constraint;
    ray_constraint = ivec3(block);
    trace_ray(job);
    ray_constraint = previous_constraint;

    if (job.result_hit) return result_block_id;

#if defined PH_USE_CUSTOM_AIR_ID
    return PH_AIR_ID;
#else
    return -1;
#endif
}

Light load_light(int index) {
    return light_list_get(index);
}

Light load_main_hand_light() {
    return get_main_hand_light();
}

Light load_off_hand_light() {
    return get_off_hand_light();
}

#endif
