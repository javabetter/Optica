#ifndef PH_CACHE_PROFILE_INCLUDE
#define PH_CACHE_PROFILE_INCLUDE

// Optica: lighting cache profiler counters (Lighting Cache Settings > Profiler). The passes add to
// them with atomics; SurfaceCache.java reads them back and writes optica-profile.log. Without
// PH_CACHE_PROFILE every PH_PROFILE_* call compiles to nothing.
//
// Indices must match CacheProfiler.java.

#define PH_STAT_FRAMES              0
#define PH_STAT_PIXELS              1   // c0: pixels in the world
#define PH_STAT_CORNERS             2   // c0: samples requested (up to 4 per pixel)
#define PH_STAT_FOUND               3   // found an existing entry
#define PH_STAT_CREATED             4   // claimed a slot and queued it
#define PH_STAT_FAIL_PROBE          5   // probe window full of recently used entries (table too full)
#define PH_STAT_FAIL_QUEUE          6   // this frame's queue was already full
#define PH_STAT_FAIL_RACE           7   // another pixel claimed the slot for a different key
#define PH_STAT_FAIL_OVERFLOW       8   // claimed, then lost the race for the last queue places
#define PH_STAT_REFRESH_OUTDATED    9   // re-queued: seen again after being out of view
#define PH_STAT_REFRESH_DIRTY      10   // re-queued: predates a block or light change
#define PH_STAT_REFRESH_REJECTED   11   // re-queue refused, queue full
#define PH_STAT_LEVEL0             12   // c0: pixels per lattice level (12..15 = level 0..3)
#define PH_STAT_COARSE             16   // c0: pixels using a coarse sample (not axis aligned, hand)
#define PH_STAT_QUEUE_REQUESTED    17   // c1: queue entries asked for (can exceed the queue size)
#define PH_STAT_QUEUE_PROCESSED    18   // c1: queue entries computed
#define PH_STAT_BUDGET             19   // c1: rotating refresh slots scheduled
#define PH_STAT_CURSOR_EMPTY       20   // c1: rotating refresh visited an empty slot
#define PH_STAT_CURSOR_IDLE        21   // c1: ... an entry not seen recently (skipped)
#define PH_STAT_CURSOR_COMPUTED    22   // c1: ... an entry it recomputed
#define PH_STAT_ZERO_DIRECT        23   // samples computed with no direct light at all
#define PH_STAT_BINS_MISS          24   // direct light: position outside the light bins
#define PH_STAT_COVERED            25   // c2: pixels with all samples available
#define PH_STAT_PARTIAL            26   // c2: pixels with some samples available
#define PH_STAT_UNCOVERED          27   // c2: pixels with none available
#define PH_STAT_HISTORY            28   // c2: pixels with a valid history
#define PH_STAT_FALLBACK           29   // c2: pixels using the stand-in lighting (no history)
#define PH_STAT_SLOT_NONE          30   // c2: sample had no slot (request failed)
#define PH_STAT_SLOT_MISMATCH      31   // c2: slot holds another key now
#define PH_STAT_NOT_COMPUTED       32   // c2: slot not computed yet
#define PH_STAT_LIGHTS_MAX         33   // max light_list_size seen
#define PH_STAT_VIEW_W_MAX         34   // max framebuffer width seen (cache resolution)
#define PH_STAT_VIEW_H_MAX         35
#define PH_STAT_COUNT              36

#if defined PH_CACHE_PROFILE
layout (std430) restrict buffer ph_surface_cache_profile {
    uint ph_cache_profile[];
};

#define PH_PROFILE_ADD(stat, n) atomicAdd(ph_cache_profile[stat], uint(n))
#define PH_PROFILE_MAX(stat, n) atomicMax(ph_cache_profile[stat], uint(n))
#else
#define PH_PROFILE_ADD(stat, n)
#define PH_PROFILE_MAX(stat, n)
#endif

#endif
