// Optica: default for the Photonics 0.3.x write_indirect() API. Shader packs that consume legacy GI
// override this file (e.g. writing to a render target or image); without an override the GI is dropped.

void write_indirect(vec3 color) {
}
