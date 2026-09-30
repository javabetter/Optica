#ifndef PH_LIGHT_BINS_INCLUDE
#define PH_LIGHT_BINS_INCLUDE

// Optica: spatial light bins (see LightBins.java). Maps 8^3-block cells around the camera to the
// indices of the lights that can reach them.

#include "/photonics/uniforms.glsl"

layout (std430) restrict readonly buffer ph_light_bins {
    int ph_light_bins_array[];
};

const int ph_light_bin_cell_size = 8;
const int ph_light_bin_header = 4;

// Returns false when the position is outside the binned region (or no bins exist yet).
bool light_bins_lookup(vec3 rt_pos, out int first, out int last) {
    first = 0;
    last = 0;

    int grid_size = ph_light_bins_array[3];
    if (grid_size <= 0) return false;

    ivec3 grid_origin = ivec3(ph_light_bins_array[0], ph_light_bins_array[1], ph_light_bins_array[2]);
    ivec3 cell = ivec3(floor((rt_pos + light_list_offset) / float(ph_light_bin_cell_size))) - grid_origin;
    if (any(lessThan(cell, ivec3(0))) || any(greaterThanEqual(cell, ivec3(grid_size)))) return false;

    int cell_index = (cell.z * grid_size + cell.y) * grid_size + cell.x;
    int index_start = ph_light_bin_header + grid_size * grid_size * grid_size + 1;

    first = index_start + ph_light_bins_array[ph_light_bin_header + cell_index];
    last = index_start + ph_light_bins_array[ph_light_bin_header + cell_index + 1];

    return true;
}

#endif
