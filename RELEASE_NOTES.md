> Photonics is free and open-source software, and can be downloaded from https://modrinth.com/mod/photonics (Modrinth), or https://github.com/Redi2Go/PhotonicEngine (GitHub). Anyone can modify and distribute it under the terms of the GNU Lesser General Public License, version 3.

Optica: Photonics ported to Minecraft 26.1.2 (Fabric), with Euphoria Patches support and optional performance settings.

**Requirements:** Minecraft 26.1.2, Fabric Loader, Fabric API, Iris 1.11.3+ and Sodium 0.9.1+ (0.9.1 works with Voxy). Optica replaces Photonics; do not install both.

**Lighting cache: smoother loading**
- When joining a world, switching servers or turning quickly, lighting no longer fills in block by block. Areas the cache has not computed yet are lit with regular Photonics-style lighting, and cached lighting fades in over it as it is computed.
- New cache samples are always computed in the frame they appear (they used to wait up to the refresh time when many appeared at once).

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
