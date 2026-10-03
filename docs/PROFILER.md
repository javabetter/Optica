# Lighting cache profiler

Two diagnostics for the lighting cache, both on the **Lighting Cache Settings** page (Euphoria Patches:
Configure Euphoria Patches > Modded Settings > Photonics > Lighting Cache Settings). Both are off by
default and only do anything while the Lighting Cache is on.

## Collecting a report

1. Turn on **Lighting Cache** and set **Profiler** to On. Click Apply.
2. Play normally and reproduce the problem (lines, lights that stop loading, etc.) for a minute or two.
   Note roughly when it happens (the log has a clock time on every line).
3. Optionally set **Debug View** to Cache Status and take screenshots of the problem, then Detail Level
   and take the same screenshots.
4. Send `optica-profile.log` from the Minecraft folder (next to `logs/` and `saves/`). Every run appends
   a new section starting with `# Optica lighting cache profile`; delete the file to start over.

The profiler costs some performance (it counts with GPU atomics), so turn it off afterwards.

## Debug View

- **Cache Status**: green = all of the pixel's samples are cached, yellow = some are, red = none are
  (keeping the previous lighting), magenta = none and no previous lighting (stand-in lighting),
  blue = the hand.
- **Detail Level**: samples per block edge; red = 1, yellow = 2, green = 4, cyan = 8, blue = one sample
  per block (surfaces that are not axis aligned, and the hand).

The colours replace the light, so the shader pack's own lighting (sun, sky) still shows around them.

## Log columns

Each line averages about one second. "Per frame" means divided by `gpuFrames`.

| Column | Meaning |
| --- | --- |
| `fps`, `maxFrameMs` | Frame rate and the longest frame in the window (CPU side). |
| `gpuFrames` | Frames the cache passes ran in the window. |
| `view` | Resolution the cache works at (screen size times render scale). |
| `lights` | Lights in the light list. |
| `pixels` | Pixels per frame that show the world. |
| `level0/1/2/3/coarse%` | Share of pixels using 1 / 2 / 4 / 8 samples per block edge, or one sample per block. |
| `blended%` | Share of pixels blending towards the next finer level (near a level change). |
| `samples` | Cache samples looked up per frame (up to 4 per pixel). |
| `found%` | Share of those already in the cache. |
| `created` | New samples created and queued per frame. |
| `failProbe` | Lookups that found no free place nearby in the table (table too full). |
| `failQueue` | New samples refused because this frame's queue (65536) was full. |
| `failRace` | Another pixel was creating a sample in the same place at the same time (retried next frame). |
| `failOverflow` | Created, then refused because the queue filled up at the same moment. |
| `refreshOutdated` | Requests (per pixel corner, not unique) to recompute samples seen again after a while out of view, or retried after an unsure computation (a shadow ray left the voxel world). |
| `refreshDirty` | Requests to recompute samples older than a nearby block or light change. |
| `refreshRejected` | Recompute requests refused because the queue's refresh share (half) was full. |
| `queueAsked`, `queueDone` | Queue entries asked for and computed per frame (asked > done means overflow). |
| `budget` | Background refresh slots per frame (0 with Refresh Time "Only on Changes"). |
| `cursorComputed` | Samples the background refresh recomputed per frame. |
| `tableUsed%` | Estimated share of the table in use (from the background refresh's visits; needs a Refresh Time). |
| `zeroDirect` | Samples computed with no direct light at all (normal far from lights; suspicious near them). |
| `binsMiss` | Light lookups outside the light grid (lights there are ignored). |
| `noLightList` | Samples computed while the light list was empty. |
| `noLightInRange` | Samples with no light in range at all. |
| `rays` | Shadow rays per frame (cache samples and stand-in lighting). |
| `rayReached%`, `rayBlocked%` | Shadow rays that reached their light / were blocked by a block (a real shadow). |
| `rayOutOfSteps%` | Shadow rays that gave up before reaching the light (counted as shadow; should be ~0). |
| `rayLeftWorld%`, `rayMissed%` | Shadow rays that left the voxel world / ended without hitting anything (should be ~0). |
| `covered%`, `partial%`, `uncovered%` | Pixels with all / some / none of their samples available. |
| `history%` | Pixels with missing samples that could reuse the previous frame's lighting. |
| `standIn%` | Pixels using the stand-in lighting (no samples and no history). |
| `slotNone` | Samples per frame that could not be created or found (see the `fail*` columns). |
| `slotMismatch` | Samples whose place was taken by another sample between passes. |
| `notComputed` | Samples that exist but are not computed yet. |
| `sectionsUploaded`, `lightsChanged`, `dirtyRegions` | Changed areas of world sections (the 4x4x4 block cubes whose voxel blocks changed, or that loaded) and lights that changed in the window, and the change regions created from them. |
| `gpuMs` | GPU milliseconds per frame: `frame` is the whole frame (everything Minecraft, the shader pack and Optica draw), then each Optica pass as `group/pass` (for example `cached_lighting/request`). Compare the passes with `frame` to see how much of it is Optica. |
