Optica: the Photonics voxel raytracing engine ported to Minecraft 26.1.2 (Fabric), with Euphoria Patches support.

This is the **full** build from `main`: the Photonics port with Optica's performance work. For the port without performance changes, use the `v0.1.0-port` release.

**Requirements:** Minecraft 26.1.2, Fabric Loader, Fabric API, Iris 1.11.3+ and Sodium 0.9.1+ (0.9.1 works with Voxy). Do not install Photonics alongside Optica.

**Included**
- Everything in the port build: Photonics engine on 26.1.2, the Photonics 0.3.x shader API (BASIC mode, legacy GI, legacy tracing API) and all bug fixes.
- Light merging: dense groups of the same light (lava lakes, glowstone ceilings) become one light per cell, with cells growing with distance, so large lava lakes no longer fill the light limit.
- Distance level of detail in BASIC and ReSTIR modes: far pixels use fewer lights and trace less often.
- BASIC direct light is reused across frames and refreshed on a rotating schedule, and shadow rays are cheaper.
- The block atlas is cached across pipelines, which makes dimension changes faster and lighter on memory.

Licensed LGPL-3.0-only. Based on Photonics by Redi2Go and Essentuan.
