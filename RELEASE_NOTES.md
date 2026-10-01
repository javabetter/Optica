Photonics Unofficial Port: the Photonics voxel raytracing engine ported to Minecraft 26.1.2 (Fabric), with Euphoria Patches support. This is an unofficial port, not affiliated with the Photonics developers.

It contains only the port, compatibility work and bug fixes, without any performance changes. For the faster build with the lighting cache and performance settings, use **Optica** (the `main` branch releases).

**Requirements:** Minecraft 26.1.2, Fabric Loader, Fabric API, Iris 1.11.3+ and Sodium 0.9.1+ (0.9.1 works with Voxy). Do not install it alongside Photonics or Optica.

**Included**
- Photonics engine ported to 26.1.2 (Iris 1.11, Sodium 0.9).
- Photonics 0.3.x shader API for packs such as Euphoria Patches: BASIC lighting mode, legacy GI (`write_indirect`), the legacy tracing API and its globals.
- Fixes: black lighting from textures with no sampler state, lag with Photonics turned off, lighting stuck in a corner after resizing, one-frame lighting lag (ghosting), buffer memory not freed when toggling shaders, crashes on Hypixel and when switching dimensions, hand artifacts, and aliasing at reduced render scale (depth-aware upsampling).

**Changes in 0.1.1**
- Renamed from "Optica (port)" to Photonics Unofficial Port, with the Photonics icon. No code changes.

Licensed LGPL-3.0-only. Based on Photonics by Redi2Go and Essentuan.
