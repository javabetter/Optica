#file "/photonics/modifiers/light_modifier.glsl"

#replace "#define PH_LIGHT_MODIFIER_DISABLED"
// Optica: lights come from Photonics at their configured intensity (a torch is 0.12), which packs scale
// to their own brightness in this modifier (Euphoria Patches swaps in full-strength colors). BSL had
// none, so its Photonics light was far dimmer than BSL's own block light (a torch lit about 3 blocks).
// 8x keeps the lights' relative brightness and roughly matches BSL's vanilla block light.
void modify_light(inout Light light, vec3 world_pos) {
    light.color *= 8.0f;
}
#endreplace
