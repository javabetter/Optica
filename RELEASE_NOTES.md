Optica: the Photonics voxel raytracing engine ported to Minecraft 26.1.2 (Fabric), with Euphoria Patches support.

This is the **full** build from `main`: the Photonics port with Optica's performance work. For the port without performance changes, use the `v0.1.0-port` release.

**Requirements:** Minecraft 26.1.2, Fabric Loader, Fabric API, Iris 1.11.3+ and Sodium 0.9.1+ (0.9.1 works with Voxy). Do not install Photonics alongside Optica.

**New in 0.2.0: cached lighting mode**
- A very low-cost lighting mode. Lighting is computed per block face and reused, refreshed in the background (every second by default), so the per-frame cost barely depends on how many lights are nearby.
- Enable it with `lightingMode=cached` in `config/optica.properties`, then press R. Refresh time, detail, cache memory and GI rays are configurable there.
- Changes to lights and blocks appear after the refresh time, and entities do not cast raytraced shadows in this mode.

**Included from 0.1.0**
- Everything in the port build: Photonics engine on 26.1.2, the Photonics 0.3.x shader API (BASIC mode, legacy GI, legacy tracing API) and all bug fixes.
- Light merging for dense light groups (lava lakes), distance level of detail in BASIC and ReSTIR, reuse of BASIC direct light across frames, cheaper shadow rays, and a block atlas cache for faster dimension changes.

Licensed LGPL-3.0-only. Based on Photonics by Redi2Go and Essentuan.
