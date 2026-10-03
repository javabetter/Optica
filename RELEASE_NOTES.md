> Photonics is free and open-source software, and can be downloaded from https://modrinth.com/mod/photonics (Modrinth), or https://github.com/Redi2Go/PhotonicEngine (GitHub). Anyone can modify and distribute it under the terms of the GNU Lesser General Public License, version 3.

Optica: Photonics ported to Minecraft 26.1.2 (Fabric), with Euphoria Patches support and optional performance settings.

**Requirements:** Minecraft 26.1.2, Fabric Loader, Fabric API, Iris 1.11.3+ and Sodium 0.9.1+ (0.9.1 works with Voxy). Optica replaces Photonics; do not install both.

**New in 0.3.1: lighting cache fixes and performance**
- When joining a world, switching servers or turning quickly, lighting no longer fills in block by block. Areas the cache has not computed yet are lit with regular Photonics-style lighting, and cached lighting fades in over it as it is computed.
- New cache samples are always computed in the frame they appear (they used to wait up to the refresh time when many appeared at once).
- Fixed slow, missing or striped cached lighting: cache detail now follows how big blocks are on screen (about one sample per 3 pixels, up to Cache Detail), so distant surfaces no longer use full detail.
- Areas you look back at after a while are updated immediately, and the result is blended over a few frames, so refreshes and detail changes no longer pop.
- Placing or breaking blocks and lights updates the cached lighting around them within a few frames, instead of patch by patch over the refresh time.
- Lighting no longer drops out or shows lines when samples are briefly unavailable: those pixels keep their previous lighting until the new samples are in.
- Fixed lines across ceilings and walls with the lighting cache: detail now changes gradually with distance instead of in steps.
- Fixed cached lights that stopped loading or never finished: background refreshes can no longer crowd out new samples, and large cache memory sizes no longer cause constant re-refreshing.
- Light merging no longer re-groups lights at every step you take.
- Fixed blotchy lighting with the lighting cache: the cache now keeps direct light (the expensive part), and bounced light (GI) is computed on screen as in Sharp mode. The old cached GI is still available as a cheaper option (Cache GI: 1 / 2 / 4 Rays).
- Fixed lights going dark for several seconds (often after walking a few blocks): shadow rays gave up too early on long paths through detailed builds and counted as shadowed. The lighting cache now allows 256 steps per shadow ray, and Sharp mode is back to Photonics' 100.
- Fixed remaining lines with the lighting cache when looking or moving sideways: fully cached pixels no longer blend with a re-sampled previous frame.
- Fixed thin lines of light and shadow that stayed in the same place on screen when Photonics' Render Scale is below 1.0 (in every lighting mode, most visible with the lighting cache): positions were rebuilt from a neighbouring pixel's depth, putting them slightly above or below the surface.
- Cached lighting no longer refreshes on its own: **Refresh Time** has a new default, **Only on Changes**, which recomputes lighting only where blocks or lights change. The timed refresh is still available.
- Fixed cached lighting re-lighting large areas for no visible reason: chunks rebuilt without block changes (sky light updates, neighbour updates) no longer count as changes, and changes reported together (walking into new chunks) no longer merge into one region covering everything in between.
- Faster lighting cache: Screen Space GI traces new paths every other frame nearby and less often further away, even with LOD Quality at 1.0, and fully cached pixels skip re-reading the previous frame.
- Fixed Optica lighting switching off in some places and back on when moving half a block (most common in small islands, lobbies and other worlds surrounded by void): the voxel world could be built with its corner away from where the shaders expect it, so every shadow ray missed its light. This affected every lighting mode.
- Faster lighting cache: pixels no longer rewrite the "last used" mark of shared samples every frame, block and light changes are looked up in a grid instead of checked one by one, and the Screen Space GI filter reads a third of the data per sample.
- Fixed cached lighting briefly going dark and "refreshing" when moving (for example up and down half a block on servers): samples computed while a light's blocks had not reached the voxel world yet came out dark. Such samples now keep their previous lighting and are retried a few frames later.
- Much less background recomputing on busy servers: only changes to blocks the shadow rays actually see count as changes, and lighting is recomputed around the changed 4x4x4 block area rather than the whole chunk section.
- Faster Screen Space GI with the lighting cache: on a user's GPU it was most of the cache's frame time, so it now traces new paths half as often (it already averages 32).
- The profiler now logs the GPU time of the whole frame and of each Optica pass (`gpuMs`).
- New diagnostics on the Lighting Cache Settings page: a **Profiler** that logs what the cache does to `optica-profile.log`, and a **Debug View** that colours the lighting by cache state or detail level.

**New in 0.3.0**
- All performance options are now **off by default**, so out of the box Optica renders like Photonics. Turn on what you need in the shader pack's settings (Euphoria Patches: Configure Euphoria Patches > Modded Settings > Photonics):
  - **Lighting Cache** (baked lighting) and its settings sub-page.
  - **LOD Quality**: 1.0 (default) = no level of detail; 0.5 is a good balance.
  - **Light Merging**: Off (default); Medium is a good balance.
  - **Shadow Update Interval**: every frame (default); 6 frames is a good balance.
- Optica is now a single release. The separate port-only build is discontinued: Optica with every option off replaces it.

**Lighting cache**
- A very low-cost lighting mode. Lighting is computed per block face and reused, refreshed in the background (every second by default), so the per-frame cost barely depends on how many lights are nearby.
- Changes to lights and blocks appear after the refresh time, and entities do not cast raytraced shadows in this mode.

**Fixes since 0.1.0**
- Lopsided lighting and shadows around small groups of lights (e.g. the four torches of the End fountain) with light merging on.
- Light leaking through block edges as bright streaks with the lighting cache.

**Also included**
- Photonics engine ported to 26.1.2 (Iris 1.11, Sodium 0.9), with the Photonics 0.3.x shader API for packs such as Euphoria Patches (BASIC mode, legacy GI, legacy tracing API).
- Fixes for black lighting, lag with Photonics turned off, resizing, ghosting, memory not freed when toggling shaders, crashes on Hypixel and when switching dimensions, hand artifacts and aliasing at reduced render scale.
- A block atlas cache for faster dimension changes.

Licensed LGPL-3.0-only. Based on Photonics by Redi2Go and Essentuan.
