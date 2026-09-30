#ifndef PH_SURFACE_CACHE_INCLUDE
#define PH_SURFACE_CACHE_INCLUDE

// Optica: the surface cache of the cached lighting mode (see CachedPipeline.java).
//
// Lighting is stored for points on a lattice laid over block faces in world space. A face is identified
// by its axis-aligned direction and its plane (in 1/16 block steps, so slabs and other partial blocks
// get their own plane); on the plane, samples sit at cell centres of an N x N grid per block, where N
// is PH_CACHE_DETAIL near the camera and halves with distance. A pixel interpolates the four samples
// around it, so neighbouring faces on the same plane blend smoothly.
//
// Surfaces that are not axis aligned (and the hand) use one coarse sample per block.
//
// The table is open addressing with a short linear probe. Entries not used for a while are replaced.

//ph_required: uniform vec3 cameraPosition;
//ph_required: uniform int frameCounter;

#ifndef PH_CACHE_CAPACITY_LOG2
#define PH_CACHE_CAPACITY_LOG2 21
#endif

#ifndef PH_CACHE_DETAIL
#define PH_CACHE_DETAIL 4
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

// Iris wraps frameCounter at 720720.
const uint ph_cache_frame_period = 720720u;
// Entries not seen for this many frames may be replaced by new ones.
const uint ph_cache_stale_frames = 900u;

// Entry layout (uints):
//   0 key0, 1 key1 (0 = empty slot), 2 frame last used, 3 flags (computed bit | GI sample count),
//   4 direct.rg (half), 5 direct.b | indirect.r (half), 6 indirect.gb (half), 7 unused
layout (std430) restrict buffer ph_surface_cache {
    uint ph_cache[];
};

// State: [0..1] number of new entries queued this frame (by frame parity), [2..3] refresh cursor (by
// frame parity), then the queue of new entry slots.
layout (std430) restrict buffer ph_surface_cache_state {
    uint ph_cache_state[];
};

struct CacheKey {
    uint k0;
    uint k1;
};

uint ph_cache_frame() {
    return uint(frameCounter);
}

uint ph_cache_parity() {
    return uint(frameCounter) & 1u;
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
    int plane;      // 1/16 block units, or whole blocks when coarse
    bool coarse;
    vec2 lattice;   // position on the plane in lattice units (in-plane axes b and c)
};

ivec2 ph_cache_plane_axes(int axis) {
    return ivec2((axis + 1) % 3, (axis + 2) % 3);
}

int ph_cache_level_for_distance(float distance) {
    int n = PH_CACHE_DETAIL;
    if (distance > 16.0f) n /= 2;
    if (distance > 40.0f) n /= 2;
    if (distance > 96.0f) n /= 2;
    n = max(n, 1);

    return findMSB(n);
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
        surface.plane = int(floor(world_pos[surface.axis]));
    } else {
        surface.level = ph_cache_level_for_distance(distance);
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

// Finds the entry for a key, or creates it (queued for computation this frame). Returns none when the
// probe window is full of recently used entries; the pixel then simply has no sample this frame.
uint ph_cache_acquire(CacheKey key) {
    uint home = ph_cache_home(key);
    uint frame = ph_cache_frame();

    uint candidate = ph_cache_none;
    uint candidate_k1 = 0u;

    for (int p = 0; p < ph_cache_probe_length; p++) {
        uint slot = (home + uint(p)) & ph_cache_mask;
        uint base = ph_cache_base(slot);
        uint k1 = ph_cache[base + 1u];

        if (k1 == key.k1 && ph_cache[base] == key.k0) {
            ph_cache[base + 2u] = frame;
            return slot;
        }

        if (candidate == ph_cache_none && (k1 == 0u || ph_cache_age(ph_cache[base + 2u]) > ph_cache_stale_frames)) {
            candidate = slot;
            candidate_k1 = k1;
        }
    }

    if (candidate == ph_cache_none) return ph_cache_none;

    uint base = ph_cache_base(candidate);
    uint previous = atomicCompSwap(ph_cache[base + 1u], candidate_k1, key.k1);

    if (previous == candidate_k1) {
        // Claimed: reset the entry and queue it for computation.
        ph_cache[base] = key.k0;
        ph_cache[base + 2u] = frame;
        ph_cache[base + 3u] = 0u;
        ph_cache[base + 4u] = 0u;
        ph_cache[base + 5u] = 0u;
        ph_cache[base + 6u] = 0u;

        uint index = atomicAdd(ph_cache_state[ph_cache_parity()], 1u);
        if (index < ph_cache_queue_max)
            ph_cache_state[ph_cache_state_header + index] = candidate;

        return candidate;
    }

    // Another pixel claimed the slot at the same time, most likely for the same key.
    return previous == key.k1 ? candidate : ph_cache_none;
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

void ph_cache_store(uint slot, vec3 direct, vec3 indirect, uint gi_samples) {
    uint base = ph_cache_base(slot);
    ph_cache[base + 4u] = packHalf2x16(direct.rg);
    ph_cache[base + 5u] = packHalf2x16(vec2(direct.b, indirect.r));
    ph_cache[base + 6u] = packHalf2x16(indirect.gb);
    ph_cache[base + 3u] = ph_cache_computed_bit | min(gi_samples, 0xFFFFu);
}

#endif
