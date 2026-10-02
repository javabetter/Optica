// Optica settings. Iris lists these in the shader pack's settings menu (on the pack's Photonics page, or
// an Optica page); Optica reads the chosen values when the pack loads. They do nothing in shader code.

#define OPTICA_LIGHTING_CACHE 0 // [0 1]
#define OPTICA_CACHE_REFRESH 1.0 // [0.25 0.5 1.0 2.0 3.0 5.0 10.0]
#define OPTICA_CACHE_DETAIL 4 // [1 2 4 8]
#define OPTICA_CACHE_MEMORY 64 // [32 64 128 256 512]
#define OPTICA_CACHE_GI_SAMPLES 2 // [0 1 2 4]
#define OPTICA_CACHE_PROFILER 0 // [0 1]
#define OPTICA_CACHE_DEBUG_VIEW 0 // [0 1 2]
#define OPTICA_LOD_QUALITY 1.0 // [0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0]
#define OPTICA_LIGHT_MERGING 0 // [0 1 2 3 4]
#define OPTICA_SHADOW_UPDATE 1 // [1 2 3 4 6 8 12 16]
