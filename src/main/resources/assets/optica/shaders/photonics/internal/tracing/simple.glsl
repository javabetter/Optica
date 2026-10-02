bool trace_light_vis(
    vec3 rt_pos,
    vec3 direction,
    vec3 light_rt_pos,
    int max_iterations,
    out vec3 tint_color,
    out float light_transmittance
) {
    RayIterator ray;
    ray_iter_begin(ray, rt_pos, direction);
    ray.iterations = max_iterations; // Optica: the parameter used to be ignored (always 100)
    RayResult result = missed_ray_result();

    vec4 running_tint_color = vec4(0.0f);
    light_transmittance = 1.0f;

    while (true) {
        result = ray_iter_next_block(ray, light_rt_pos);

        if (ray_result_is_transparent(result)) {
            VoxelData voxel_data = ray_result_voxel_data(result);
            vec4 albedo = voxel_data_albedo(voxel_data);

            light_transmittance *= 1.0f - albedo.a;
            ray_iter_apply_transparency(running_tint_color, albedo);

            ray_iter_skip_block(ray);
            ray_iter_offset_position(ray, ray.direction * 0.03f);

            continue;
        }

        break;
    }

    if (!ray_result_is_hit(result)) return false;
    if (floor(ray_result_position(result)) != floor(light_rt_pos)) return false;

    tint_color = running_tint_color.a == 0.0f ? vec3(1.0f) : running_tint_color.rgb;

    return true;
}

// Optica: trace_light_vis that also reports why a light was not reached (for the cache profiler):
// 0 reached, 1 blocked by another block, 2 ran out of steps, 3 left the world, 4 missed.
bool trace_light_vis_reason(
    vec3 rt_pos,
    vec3 direction,
    vec3 light_rt_pos,
    int max_iterations,
    out vec3 tint_color,
    out float light_transmittance,
    out int reason
) {
    RayIterator ray;
    ray_iter_begin(ray, rt_pos, direction);
    ray.iterations = max_iterations;
    RayResult result = missed_ray_result();

    vec4 running_tint_color = vec4(0.0f);
    light_transmittance = 1.0f;

    while (true) {
        result = ray_iter_next_block(ray, light_rt_pos);

        if (ray_result_is_transparent(result)) {
            VoxelData voxel_data = ray_result_voxel_data(result);
            vec4 albedo = voxel_data_albedo(voxel_data);

            light_transmittance *= 1.0f - albedo.a;
            ray_iter_apply_transparency(running_tint_color, albedo);

            ray_iter_skip_block(ray);
            ray_iter_offset_position(ray, ray.direction * 0.03f);

            continue;
        }

        break;
    }

    if (!ray_result_is_hit(result)) {
        reason = !ray_iter_is_in_bounds(ray) ? 3 : ray.iterations <= 0 ? 2 : 4;
        return false;
    }

    if (floor(ray_result_position(result)) != floor(light_rt_pos)) {
        reason = 1;
        return false;
    }

    reason = 0;
    tint_color = running_tint_color.a == 0.0f ? vec3(1.0f) : running_tint_color.rgb;
    return true;
}
