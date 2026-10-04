// Optica: replaces Eclipse's trace_wsr() (voxel reflections). Eclipse walked Photonics 0.3.5's world
// data directly (root_array, cb_array), which Optica's voxel world does not have, so the pack failed to
// compile with Voxel Reflections on (its default). This does the same on Optica's ray tracer: hit
// position, normal, color, block id and skylight of the first solid voxel, and (with
// TRANSLUCENT_IN_REFLECTIONS) the translucent blocks passed on the way. Mirror iron is not supported
// (iron blocks reflect as solid blocks).
void trace_wsr(inout RayJob job, bool transparency, inout translucentHit[10] translucentHitRays, inout uint translucentHits, out int block_id, out float skylightmap) {
    job.direction = normalize(ph_signed_nudge(job.direction));
    job.result_hit = false;
    job.result_normal = vec3(0.0f);
    job.result_color = vec3(0.0f);

    result_tint_color = vec3(1.0f);
    block_id = -1;
    skylightmap = 0.0f;
    ray_iteration_bound_reached = false;

    RayIterator ray;
    ray_iter_begin(ray, job.origin, job.direction);
    ray.iterations = RAY_ITERATION_COUNT;

    RayResult hit = missed_ray_result();
    int previous_block_id = -1;

    while (true) {
        hit = ray_iter_next(ray);
        if (!ray_result_is_hit(hit)) {
            ray_iteration_bound_reached = ray.iterations <= 0;
            break;
        }

#ifdef TRANSLUCENT_IN_REFLECTIONS
        if (ray_result_is_transparent(hit)) {
            VoxelData translucent = ray_result_voxel_data(hit);
            int translucent_id = voxel_data_block_id(translucent);
            vec4 color = voxel_data_albedo(translucent);

            if (translucent_id != previous_block_id && color.a > 0.01f && translucentHits < 10u) {
                translucentHitRays[translucentHits].hitNormal = ray_result_normal(hit);
                translucentHitRays[translucentHits].hitPos = ray_result_position(hit);
                translucentHitRays[translucentHits].hitColor = color;
                translucentHitRays[translucentHits].hitID = translucent_id;
                translucentHitRays[translucentHits].skylight = float(ray_result_skylight(hit)) / 15.0f;
                translucentHits += 1u;
            }

            previous_block_id = translucent_id;
            ray_iter_skip_block(ray);
            ray_iter_offset_position(ray, ray.direction * 0.03f);
            continue;
        }
#endif

        break;
    }

    if (!ray_result_is_hit(hit)) {
        job.result_position = vec3(47823934.0f) - world_offset;
        return;
    }

    VoxelData voxel_data = ray_result_voxel_data(hit);

    job.result_hit = true;
    job.result_position = ray_result_position(hit);
    job.result_normal = ray_result_normal(hit);
    job.result_color = voxel_data_albedo(voxel_data).rgb;

    block_id = voxel_data_block_id(voxel_data);
    result_block_id = block_id;
    ph_result_sky_brightness = int(ray_result_skylight(hit));
    skylightmap = float(ph_result_sky_brightness) / 15.0f;
}
