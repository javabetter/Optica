// Optica settings. Iris lists these in the shader pack's settings menu (Optica page); Optica reads the
// chosen values when the pack loads. They do nothing in shader code.

#define OPTICA_LIGHTING_MODE 0 // [0 1]
#define OPTICA_CACHE_REFRESH 1.0 // [0.25 0.5 1.0 2.0 3.0 5.0 10.0]
#define OPTICA_CACHE_DETAIL 4 // [1 2 4 8]
#define OPTICA_CACHE_MEMORY 64 // [32 64 128 256 512]
#define OPTICA_CACHE_GI_SAMPLES 2 // [0 1 2 4]
