#version 430

// Optica cached lighting, pass 4 of 4: hand the cached GI to the pack's write_indirect() (the Photonics
// 0.3.x API). Renders into the pack's own framebuffer, as declared by its write_indirect.glsl.

#include "/photonics/rendering/frag/common.glsl"
#include "/photonics/write_indirect.glsl"

uniform sampler2D cached_indirect;

void main() {
    setup_frag_data(0);

    if (!frag_is_in_world) {
        write_indirect(vec3(0.0f));
        return;
    }

    write_indirect(texelFetch(cached_indirect, frag_tex_coord, 0).rgb);
}
