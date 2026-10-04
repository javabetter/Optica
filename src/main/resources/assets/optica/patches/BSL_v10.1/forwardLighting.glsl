#file "/lib/lighting/forwardLighting.glsl"

#replace "vec3 sceneLighting = mix(ambientCol * lightmap.y, lightCol, fullShadow * shadowMult);"
#ifdef PHOTONICS_ENABLED
float dist = clamp(length(worldPos) * 0.005, 0.0f, 1.0f);
vec3 sceneLighting = mix(dist * ambientCol * lightmap.y, lightCol, fullShadow * shadowMult);
#else
vec3 sceneLighting = mix(ambientCol * lightmap.y, lightCol, fullShadow * shadowMult);
#endif
#endreplace

#replace "vec3 blockLighting = blocklightCol * newLightmap * newLightmap;"
// Optica: only with Photonics on; with it off in BSL's settings, BSL's own block light was gone too.
#ifdef PHOTONICS_ENABLED
vec3 blockLighting = vec3(0.0f);
#else
vec3 blockLighting = blocklightCol * newLightmap * newLightmap;
#endif
#endreplace