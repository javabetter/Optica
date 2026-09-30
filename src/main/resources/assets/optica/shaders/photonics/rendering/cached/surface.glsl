#ifndef PH_CACHE_PIXEL_SURFACE_INCLUDE
#define PH_CACHE_PIXEL_SURFACE_INCLUDE

// Optica: the cache surface of the current pixel (after setup_frag_data). Shared by the request and
// resolve passes so both derive exactly the same keys.

#include "/photonics/rendering/cached/cache.glsl"

CacheSurface ph_cache_pixel_surface() {
    // The hand is drawn in its own space; light it from the block the camera is in.
    if (frag_is_hand)
        return ph_cache_surface(cameraPosition, frag_geo_normal, 0.0f, true);

    return ph_cache_surface(frag_player_pos + cameraPosition, frag_geo_normal, length(frag_player_pos), false);
}

#endif
