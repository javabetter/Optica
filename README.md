# Optica

> Photonics is free and open-source software, and can be downloaded from https://modrinth.com/mod/photonics (Modrinth), or https://github.com/Redi2Go/PhotonicEngine (GitHub). Anyone can modify and distribute it under the terms of the GNU Lesser General Public License, version 3.

Optica is a port of the Photonics voxel raytracing engine (an Iris extension) to Minecraft 26.1.2
on Fabric, with Euphoria Patches compatibility. See `NOTICE.md` for credits.

## Requirements

Minecraft 26.1.2 with Fabric Loader, Fabric API, Iris 1.11.3+ and Sodium 0.9.1+ (Sodium 0.9.1 works
with Voxy). Optica replaces Photonics; do not install both.

## Cached lighting mode (main branch)

A third lighting mode for low-end GPUs. Lighting is computed per block face in the world and reused,
refreshed in the background, so the per-frame cost barely depends on how many lights are around.
Changes to lights and blocks show up with a delay of up to the refresh time, and entities do not cast
raytraced shadows.

Enable it in the shader pack's settings menu: Optica adds an **Optica** page at the end of the
pack's main settings screen (Iris: Shader Packs > Shader Pack Settings), with these options:

- **Lighting Mode**: Shader Pack (the pack's own BASIC or ReSTIR mode) or Cached.
- **Cache Refresh Time**: time to recompute all cached lighting once (0.25 - 10 s).
- **Cache Detail**: lighting samples per block edge near the camera (1, 2, 4 or 8).
- **Cache Memory**: GPU memory for the cache (32 - 512 MB).
- **Cache GI Rays**: sky/GI rays per cache update (Off disables GI in this mode).

It works with Euphoria Patches set to either Photonics mode (BASIC is recommended).

## Branches and releases

- `main`: the full mod. It includes Optica's performance work: merging of dense light groups (such
  as lava lakes), distance-based level of detail for lighting in both modes, temporal reuse of
  BASIC-mode direct light, cheaper shadow rays, and a block atlas cache for faster dimension changes.
- `photonics-port`: the Photonics port with compatibility work and bug fixes only, without those
  performance changes. Use it to compare against Photonics, or if you prefer the unmodified
  behaviour.

Each branch has its own release on the Releases page.

## Building

Build with JDK 25: `./gradlew build`. The jar lands in `build/libs/`.

## License

LGPL-3.0-only, with the Photonics attribution term. See `LICENSE.md`, `LICENSE-GPL-3.0.txt` and
`LICENSE-ADDITIONAL-TERMS.md`.
