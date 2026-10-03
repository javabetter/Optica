#ifndef PH_SURFACE_CACHE_INCLUDE
#define PH_SURFACE_CACHE_INCLUDE

// Optica: the surface cache of the cached lighting mode (see CachedPipeline.java).
//
// Lighting is stored for points on a lattice laid over block faces in world space. A face is identified
// by its axis-aligned direction and its plane (in 1/16 block steps, so slabs and other partial blocks
// get their own plane); on the plane, samples sit at cell centres of an N x N grid per block. N follows
// the size of a block on screen (about one sample per ph_cache_pixels_per_sample pixels), capped at
// PH_CACHE_DETAIL, so the number of samples in view scales with the screen, not the render distance.
// A pixel interpolates the four samples around it, so neighbouring faces on the same plane blend
// smoothly.
//
// Surfaces that are not axis aligned (and the hand) use one coarse sample per block.
//
// The table is open addressing with a short linear probe. Entries not used for a while are replaced.

#include "/photonics/utility/lod.glsl"
#include "/photonics/rendering/cached/profile.glsl"

//ph_required: uniform vec3 cameraPosition;
//ph_required: uniform int frameCounter;
// The cache's own frame counter (SurfaceCache.java): dirty regions are stamped with it.
//ph_required: uniform int optica_cache_frame;
//ph_required: uniform float frameTime;
//ph_required: uniform float viewHeight;
//ph_required: uniform mat4 gbufferProjection;

#ifndef PH_CACHE_CAPACITY_LOG2
#define PH_CACHE_CAPACITY_LOG2 21
#endif

#ifndef PH_CACHE_DETAIL
#define PH_CACHE_DETAIL 4
#endif

#ifndef PH_CACHE_REFRESH_SECONDS
#define PH_CACHE_REFRESH_SECONDS 0.0
#endif

const uint ph_cache_capacity = 1u << PH_CACHE_CAPACITY_LOG2;
const uint ph_cache_mask = ph_cache_capacity - 1u;

// Must match SurfaceCache.java / CachedPipeline.java.
const uint ph_cache_entry_uints = 8u;
const uint ph_cache_state_header = 4u;
const uint ph_cache_queue_max = 65536u;
const uint ph_cache_update_width = 512u;
const uint ph_cache_update_height = 192u;

const int ph_cache_probe_length = 8;
const uint ph_cache_none = 0xFFFFFFFFu;
const uint ph_cache_computed_bit = 1u << 31;
// k1 of a slot while its new key is being written (valid keys always have bit 31 set). Other pixels
// neither match nor take a locked slot, so a half-written entry is never mistaken for another key.
const uint ph_cache_locked = 1u;
// Set when a refresh is for a nearby world change: the update restarts the entry's GI average.
const uint ph_cache_reset_gi_bit = 1u << 29;
// Set while an entry waits in the queue for a refresh, so it is queued only once.
const uint ph_cache_queued_bit = 1u << 30;
// Set when the last computation was unsure (a shadow ray left the voxel world): the request pass
// retries the sample every few frames.
const uint ph_cache_retry_bit = 1u << 28;
const uint ph_cache_retry_interval = 8u;
// Target spacing of samples on screen. Lower is sharper but needs more samples.
const float ph_cache_pixels_per_sample = 3.0f;

// optica_cache_frame wraps at this period (SurfaceCache.FRAME_PERIOD).
const uint ph_cache_frame_period = 720720u;
// An entry is recomputed for a world change at most this often.
const uint ph_cache_dirty_min_frames = 4u;
// Must match SurfaceCache.java.
// Entries not seen for this many frames may be replaced by new ones.
const uint ph_cache_stale_frames = 900u;

// Entry layout (uints):
//   0 key0, 1 key1 (0 = empty slot), 2 frame last used, 3 flags (computed bit | GI sample count),
//   4 direct.rg (half), 5 direct.b | indirect.r (half), 6 indirect.gb (half),
//   7 frame of the last computation (| computed bit; 0 = never computed)
layout (std430) restrict buffer ph_surface_cache {
    uint ph_cache[];
};

// State: [0..1] number of new entries queued this frame (by frame parity), [2..3] refresh cursor (by
// frame parity), then the queue of new entry slots.
layout (std430) restrict buffer ph_surface_cache_state {
    uint ph_cache_state[];
};

// Recently changed regions of the world (SurfaceCache.java) as a grid of 16-block cells around the
// camera: [0] 1 if there are any, [1..3] grid origin in cells, then per cell the frame of its newest
// change (-1 = none), x fastest.
layout (std430) restrict readonly buffer ph_surface_cache_dirty {
    int ph_cache_dirty[];
};

struct CacheKey {
    uint k0;
    uint k1;
};

uint ph_cache_frame() {
    return uint(optica_cache_frame);
}

uint ph_cache_parity() {
    return uint(optica_cache_frame) & 1u;
}

uint ph_cache_age(uint last_used) {
    uint now = ph_cache_frame();
    return now >= last_used ? now - last_used : now + ph_cache_frame_period - last_used;
}

uint ph_cache_hash(uint x) {
    x ^= x >> 16;
    x *= 0x7feb352du;
    x ^= x >> 15;
    x *= 0x846ca68bu;
    x ^= x >> 16;
    return x;
}

uint ph_cache_home(CacheKey key) {
    return ph_cache_hash(key.k0 ^ ph_cache_hash(key.k1)) & ph_cache_mask;
}

// ---------------------------------------------------------------------------------------------------
// Surface description and keys

// One surface point to be shaded, reduced to what identifies its cache samples.
struct CacheSurface {
    int axis;       // 0 = x, 1 = y, 2 = z
    int dir;        // axis * 2 + (positive ? 1 : 0)
    int level;      // lattice cells per block edge = 1 << level
    float lod;      // continuous level of detail (level = floor(lod)), for blending to the next level
    int plane;      // 1/16 block units, or whole blocks when coarse
    bool coarse;
    vec2 lattice;   // position on the plane in lattice units (in-plane axes b and c)
};

ivec2 ph_cache_plane_axes(int axis) {
    return ivec2((axis + 1) % 3, (axis + 2) % 3);
}

// Continuous level of detail (log2 of samples per block edge) for a surface seen at `distance`, where
// `facing` is the cosine between the surface normal and the view direction. Samples are spaced about
// ph_cache_pixels_per_sample pixels apart on screen (wider at grazing angles, where a pixel stretches
// over more of the surface). LOD Quality below 1.0 lowers the detail further.
float ph_cache_lod_for_distance(float distance, float facing) {
    // Size of one screen pixel in blocks at that distance (gbufferProjection[1][1] = 1 / tan(fov / 2)).
    float pixel_size = 2.0f * distance / max(gbufferProjection[1][1] * viewHeight, 1.0f);
    pixel_size /= max(facing, 0.3f);

    float samples_per_block = 1.0f / max(pixel_size * ph_cache_pixels_per_sample, 1e-4f);
    samples_per_block *= clamp(float(PH_LOD_SCALE), 0.25f, 1.0f);

    return clamp(log2(max(samples_per_block, 1.0f)), 0.0f, float(findMSB(PH_CACHE_DETAIL)));
}

// Pixels in the top part of a level's range blend towards the next finer level, so detail changes
// gradually with distance instead of in visible steps (which showed up as lines across ceilings and
// walls seen at an angle).
const float ph_cache_level_blend_start = 0.6f;

// The same surface one level finer, and how much of it to use (0 = none).
CacheSurface ph_cache_finer(CacheSurface surface, out float weight) {
    CacheSurface finer = surface;
    weight = 0.0f;

    if (surface.coarse || surface.level >= findMSB(PH_CACHE_DETAIL)) return finer;

    weight = smoothstep(ph_cache_level_blend_start, 1.0f, surface.lod - float(surface.level));
    finer.level = surface.level + 1;
    finer.lod = float(finer.level);
    finer.lattice = (surface.lattice + 0.5f) * 2.0f - 0.5f;
    return finer;
}

CacheSurface ph_cache_surface(vec3 world_pos, vec3 normal, float distance, bool force_coarse) {
    CacheSurface surface;

    vec3 a = abs(normal);
    surface.axis = a.x >= a.y && a.x >= a.z ? 0 : (a.y >= a.z ? 1 : 2);
    surface.dir = surface.axis * 2 + (normal[surface.axis] > 0.0f ? 1 : 0);
    surface.coarse = force_coarse || a[surface.axis] < 0.98f;

    ivec2 axes = ph_cache_plane_axes(surface.axis);
    vec2 in_plane = vec2(world_pos[axes.x], world_pos[axes.y]);

    if (surface.coarse) {
        surface.level = 0;
        surface.lod = 0.0f;
        surface.plane = int(floor(world_pos[surface.axis]));
    } else {
        vec3 view = world_pos - cameraPosition;
        float facing = abs(dot(normal, view)) / max(length(view), 1e-4f);
        surface.lod = ph_cache_lod_for_distance(distance, facing);
        surface.level = int(floor(surface.lod));
        surface.plane = int(round(world_pos[surface.axis] * 16.0f));
    }

    // Cell centres sit at (i + 0.5) / N, so the samples around a point start at floor(p * N - 0.5).
    surface.lattice = in_plane * float(1 << surface.level) - 0.5f;

    return surface;
}

CacheKey ph_cache_key(CacheSurface surface, ivec2 cell) {
    CacheKey key;
    key.k0 = (uint(cell.x) & 0xFFFFu) | ((uint(cell.y) & 0xFFFFu) << 16);
    key.k1 = (uint(surface.plane) & 0x3FFFFFu)
           | (uint(surface.dir) << 22)
           | (uint(surface.level) << 25)
           | (surface.coarse ? 1u << 27 : 0u)
           | (1u << 31);
    return key;
}

// The four lattice cells around the surface point, and their bilinear weights.
void ph_cache_corners(CacheSurface surface, out ivec2 cells[4], out vec4 weights) {
    ivec2 base = ivec2(floor(surface.lattice));
    vec2 f = surface.lattice - vec2(base);

    cells = ivec2[4](base, base + ivec2(1, 0), base + ivec2(0, 1), base + ivec2(1, 1));
    weights = vec4((1.0f - f.x) * (1.0f - f.y), f.x * (1.0f - f.y), (1.0f - f.x) * f.y, f.x * f.y);
}

// Undoes the wrapping of a key field of `bits` bits, relative to a reference value.
int ph_cache_unwrap(uint value, int reference, int bits) {
    int shift = 32 - bits;
    int delta = (int(value) - reference) << shift >> shift;
    return reference + delta;
}

// The world position (on the surface, not offset) and normal of a cache entry's sample.
void ph_cache_decode(CacheKey key, out vec3 world_pos, out vec3 normal) {
    int dir = int((key.k1 >> 22) & 7u);
    int axis = dir / 2;
    int level = int((key.k1 >> 25) & 3u);
    bool coarse = (key.k1 & (1u << 27)) != 0u;

    normal = vec3(0.0f);
    normal[axis] = (dir & 1) != 0 ? 1.0f : -1.0f;

    ivec2 axes = ph_cache_plane_axes(axis);
    float n = float(1 << level);

    int cam_i = int(floor(cameraPosition[axes.x] * n - 0.5f));
    int cam_j = int(floor(cameraPosition[axes.y] * n - 0.5f));
    int i = ph_cache_unwrap(key.k0 & 0xFFFFu, cam_i, 16);
    int j = ph_cache_unwrap(key.k0 >> 16, cam_j, 16);

    world_pos[axes.x] = (float(i) + 0.5f) / n;
    world_pos[axes.y] = (float(j) + 0.5f) / n;

    if (coarse) {
        int plane = ph_cache_unwrap(key.k1 & 0x3FFFFFu, int(floor(cameraPosition[axis])), 22);
        world_pos[axis] = float(plane) + 0.5f;
    } else {
        int plane = ph_cache_unwrap(key.k1 & 0x3FFFFFu, int(round(cameraPosition[axis] * 16.0f)), 22);
        world_pos[axis] = float(plane) / 16.0f;
    }
}

// ---------------------------------------------------------------------------------------------------
// Table access

uint ph_cache_base(uint slot) {
    return slot * ph_cache_entry_uints;
}

bool ph_cache_matches(uint slot, CacheKey key) {
    uint base = ph_cache_base(slot);
    return ph_cache[base + 1u] == key.k1 && ph_cache[base] == key.k0;
}

uint ph_cache_find(CacheKey key) {
    uint home = ph_cache_home(key);

    for (int p = 0; p < ph_cache_probe_length; p++) {
        uint slot = (home + uint(p)) & ph_cache_mask;
        if (ph_cache_matches(slot, key)) return slot;
    }

    return ph_cache_none;
}

// Finds the entry for a key, or creates it (queued for computation this frame). Returns none when it
// cannot be done this frame (the probe window is full of recently used entries, the queue is full, or
// another pixel is creating an entry nearby); the pixel then keeps its previous lighting and retries.
uint ph_cache_acquire(CacheKey key) {
    uint home = ph_cache_home(key);
    uint frame = ph_cache_frame();

    uint candidate = ph_cache_none;
    uint candidate_k1 = 0u;
    bool saw_lock = false;

    for (int p = 0; p < ph_cache_probe_length; p++) {
        uint slot = (home + uint(p)) & ph_cache_mask;
        uint base = ph_cache_base(slot);
        uint k1 = ph_cache[base + 1u];

        if (k1 == key.k1 && ph_cache[base] == key.k0) {
            // Many pixels share a sample: only refresh its last-used frame when it is a few frames old,
            // which saves most of these scattered writes (ages are only compared against long spans).
            if (ph_cache_age(ph_cache[base + 2u]) >= 4u) ph_cache[base + 2u] = frame;
            PH_PROFILE_ADD(PH_STAT_FOUND, 1);
            return slot;
        }

        if (k1 == ph_cache_locked) {
            saw_lock = true;
            continue;
        }

        if (candidate == ph_cache_none && (k1 == 0u || ph_cache_age(ph_cache[base + 2u]) > ph_cache_stale_frames)) {
            candidate = slot;
            candidate_k1 = k1;
        }
    }

    // Another pixel is writing an entry in this window, possibly for this very key: creating it here
    // too would leave a duplicate. Try again next frame.
    if (saw_lock) {
        PH_PROFILE_ADD(PH_STAT_FAIL_RACE, 1);
        return ph_cache_none;
    }

    if (candidate == ph_cache_none) {
        PH_PROFILE_ADD(PH_STAT_FAIL_PROBE, 1);
        return ph_cache_none;
    }

    // A new entry must make it into this frame's queue, or it would wait for the background refresh
    // (up to the refresh time) and fill in block by block. When the queue is full, create it next frame.
    uint parity = ph_cache_parity();
    if (ph_cache_state[parity] >= ph_cache_queue_max) {
        PH_PROFILE_ADD(PH_STAT_FAIL_QUEUE, 1);
        return ph_cache_none;
    }

    uint base = ph_cache_base(candidate);
    if (atomicCompSwap(ph_cache[base + 1u], candidate_k1, ph_cache_locked) != candidate_k1) {
        PH_PROFILE_ADD(PH_STAT_FAIL_RACE, 1);
        return ph_cache_none;
    }

    // Locked: reserve a queue place, write the entry, then publish the key.
    uint index = atomicAdd(ph_cache_state[parity], 1u);
    if (index >= ph_cache_queue_max) {
        atomicExchange(ph_cache[base + 1u], 0u);
        PH_PROFILE_ADD(PH_STAT_FAIL_OVERFLOW, 1);
        return ph_cache_none;
    }

    ph_cache[base] = key.k0;
    ph_cache[base + 2u] = frame;
    ph_cache[base + 3u] = 0u;
    ph_cache[base + 4u] = 0u;
    ph_cache[base + 5u] = 0u;
    ph_cache[base + 6u] = 0u;
    ph_cache[base + 7u] = 0u;
    ph_cache_state[ph_cache_state_header + index] = candidate;

    memoryBarrierBuffer();
    atomicExchange(ph_cache[base + 1u], key.k1);

    PH_PROFILE_ADD(PH_STAT_CREATED, 1);
    return candidate;
}

bool ph_cache_is_computed(uint slot) {
    return (ph_cache[ph_cache_base(slot) + 3u] & ph_cache_computed_bit) != 0u;
}

void ph_cache_load(uint slot, out vec3 direct, out vec3 indirect) {
    uint base = ph_cache_base(slot);
    vec2 d_rg = unpackHalf2x16(ph_cache[base + 4u]);
    vec2 d_b_i_r = unpackHalf2x16(ph_cache[base + 5u]);
    vec2 i_gb = unpackHalf2x16(ph_cache[base + 6u]);

    direct = vec3(d_rg, d_b_i_r.x);
    indirect = vec3(d_b_i_r.y, i_gb);
}

// True when a computed entry is older than twice the time the rotating update takes to visit the whole
// table (the refresh time, or longer for large tables, since it visits at most one update grid of
// entries per frame). Entries in view are refreshed by the rotating update anyway; this catches the
// ones that were out of view (skipped) and are seen again.
bool ph_cache_is_outdated(uint slot) {
    // Only on Changes: nothing goes out of date by time alone (changes are the dirty regions).
    if (PH_CACHE_REFRESH_SECONDS <= 0.0) return false;

    uint last = ph_cache[ph_cache_base(slot) + 7u];
    if ((last & ph_cache_computed_bit) == 0u) return false;

    float refresh_frames = float(PH_CACHE_REFRESH_SECONDS) / max(frameTime, 0.001f);
    float sweep_frames = float(ph_cache_capacity) / float(ph_cache_update_width * ph_cache_update_height);
    float max_frames = 2.0f * max(refresh_frames, sweep_frames);
    return float(ph_cache_age(last & ~ph_cache_computed_bit)) > max_frames;
}

// Age (in frames) of the newest world change around a block, or ph_cache_none if there is none.
const ivec3 ph_cache_dirty_grid = ivec3(64, 32, 64);

uint ph_cache_dirty_age(ivec3 block) {
    if (ph_cache_dirty[0] == 0) return ph_cache_none;

    ivec3 cell = (block >> 4) - ivec3(ph_cache_dirty[1], ph_cache_dirty[2], ph_cache_dirty[3]);
    if (any(lessThan(cell, ivec3(0))) || any(greaterThanEqual(cell, ph_cache_dirty_grid))) return ph_cache_none;

    int stamp = ph_cache_dirty[4 + cell.x + ph_cache_dirty_grid.x * (cell.y + ph_cache_dirty_grid.y * cell.z)];
    return stamp < 0 ? ph_cache_none : ph_cache_age(uint(stamp));
}

// True when a computed entry predates a world change `dirty_age` frames ago (and was not recomputed
// in the last few frames, which bounds the work while blocks keep changing nearby).
bool ph_cache_predates_change(uint slot, uint dirty_age) {
    if (dirty_age == ph_cache_none) return false;

    uint last = ph_cache[ph_cache_base(slot) + 7u];
    if ((last & ph_cache_computed_bit) == 0u) return false;

    uint age = ph_cache_age(last & ~ph_cache_computed_bit);
    return age > dirty_age && age >= ph_cache_dirty_min_frames;
}

// Queues an existing entry to be recomputed this frame (once, however many pixels ask). Refreshes may
// only use the first half of the queue, so new entries always find room. `reset_gi` restarts the
// entry's GI average (the surroundings changed).
void ph_cache_request_refresh(uint slot, bool reset_gi) {
    uint base = ph_cache_base(slot);
    uint parity = ph_cache_parity();
    if (ph_cache_state[parity] >= ph_cache_queue_max / 2u) {
        PH_PROFILE_ADD(PH_STAT_REFRESH_REJECTED, 1);
        return;
    }

    uint flags = atomicOr(ph_cache[base + 3u], ph_cache_queued_bit | (reset_gi ? ph_cache_reset_gi_bit : 0u));
    if ((flags & ph_cache_queued_bit) != 0u) return;

    uint index = atomicAdd(ph_cache_state[parity], 1u);
    if (index < ph_cache_queue_max) {
        ph_cache_state[ph_cache_state_header + index] = slot;
    } else {
        atomicAnd(ph_cache[base + 3u], ~(ph_cache_queued_bit | ph_cache_reset_gi_bit));
        PH_PROFILE_ADD(PH_STAT_REFRESH_REJECTED, 1);
    }
}

void ph_cache_store(uint slot, vec3 direct, vec3 indirect, uint gi_samples) {
    uint base = ph_cache_base(slot);
    ph_cache[base + 7u] = ph_cache_frame() | ph_cache_computed_bit;

    ph_cache[base + 4u] = packHalf2x16(direct.rg);
    ph_cache[base + 5u] = packHalf2x16(vec2(direct.b, indirect.r));
    ph_cache[base + 6u] = packHalf2x16(indirect.gb);
    ph_cache[base + 3u] = ph_cache_computed_bit | min(gi_samples, 0xFFFFu);
}

// An unsure result: keeps the entry's last-computed frame (so changes since still count as newer) and
// marks it for a retry. Its values are what the caller passes (the previous ones if it had any).
void ph_cache_store_unsure(uint slot, vec3 direct, vec3 indirect, uint gi_samples) {
    uint base = ph_cache_base(slot);
    ph_cache[base + 4u] = packHalf2x16(direct.rg);
    ph_cache[base + 5u] = packHalf2x16(vec2(direct.b, indirect.r));
    ph_cache[base + 6u] = packHalf2x16(indirect.gb);
    ph_cache[base + 3u] = ph_cache_computed_bit | ph_cache_retry_bit | min(gi_samples, 0xFFFFu);
}

// True for an unsure entry whose retry is due this frame (spread over the interval by slot).
bool ph_cache_retry_due(uint slot) {
    if ((ph_cache[ph_cache_base(slot) + 3u] & ph_cache_retry_bit) == 0u) return false;
    return ((ph_cache_frame() + slot) % ph_cache_retry_interval) == 0u;
}

#endif
