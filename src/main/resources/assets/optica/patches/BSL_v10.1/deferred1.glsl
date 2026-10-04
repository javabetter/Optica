#file "/program/deferred1.glsl"

#replace "//Common Variables//"
// Optica: after BSL's own uniforms, which Photonics' sampling code uses (depthtex0, near, far); GLSL
// needs them declared before that code. Upstream included it at the start of FSH.
#include "/photonics/ph_samplers.glsl"

uniform sampler2D colortex10;

#ifndef PH_RESTIR_COMBINED_GI
uniform sampler2D colortex12;
#endif

//Common Variables//
#endreplace

#replace "vec4 color = texture2D(colortex0, texCoord);"
vec4 color = texture2D(colortex0, texCoord);
color.rgb += (
    #ifndef PH_RESTIR_COMBINED_GI
    texture2D(colortex12, texCoord).rgb + // indirect
    #endif
    sample_photonics_handheld(texCoord) +
    sample_photonics_direct(texCoord)
) * texture2D(colortex10, texCoord).rgb; // albedo
#endreplace