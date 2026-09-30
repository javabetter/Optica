#ifndef PH_SAMPLERS_INCLUDE
#define PH_SAMPLERS_INCLUDE

#if defined PH_OFF_ACTIVE
#include "/photonics/rendering/off/samplers.glsl"
#elif defined PH_SHARP_ACTIVE
#include "/photonics/rendering/sharp/samplers.glsl"
#elif defined PH_CACHED_ACTIVE
// Optica: the cached mode resolves into the same sharp_direct texture as BASIC.
#include "/photonics/rendering/sharp/samplers.glsl"
#elif defined PH_RESTIR_ACTIVE
#include "/photonics/rendering/restir/samplers.glsl"
#endif

#endif
