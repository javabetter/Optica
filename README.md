# Optica

> Photonics is free and open-source software, and can be downloaded from https://modrinth.com/mod/photonics (Modrinth), or https://github.com/Redi2Go/PhotonicEngine (GitHub). Anyone can modify and distribute it under the terms of the GNU Lesser General Public License, version 3.

Optica is a port of Photonics to Minecraft 26.1.2 on Fabric.
Optica also has various performance-improving options, including baked lighting. They are all off
by default, so out of the box Optica looks the same as Photonics.
See `NOTICE.md` for credits.

## Requirements

Minecraft 26.1.2 with Fabric Loader, Fabric API, Iris 1.11.3+ and Sodium 0.9.1+

__Optica replaces Photonics; do not install both.__

## Cached lighting mode

A toggleable lighting performance-boosting mode for lower-end GPUs. Lighting is computed per block face in the world and reused. It is then refreshed in the background, so the per-frame cost barely depends on how many lights are around.
Changes to lights and blocks show up with a delay of up to the refresh time, and entities do not cast
raytraced shadows. It works with Photonics-compatible shaders.

## Settings

Optica's settings are shader pack options. With Euphoria Patches they are at the bottom of the pack's
Photonics page. Packs without a Photonics page get an **Optica** page at the end of their main settings
screen instead. Every option below starts at its highest-quality setting (off).

- **Lighting Cache**: Off (default), or On for the cached lighting mode (replaces the pack's lighting mode).
- **Lighting Cache Settings** (sub-page):
  - **Refresh Time**: time to recompute all cached lighting once (0.25 - 10 s).
  - **Cache Detail**: lighting samples per block edge near the camera (1, 2, 4 or 8).
  - **Cache Memory**: GPU memory for the cache (32 - 512 MB).
  - **Cache GI**: Screen Space (default, smooth GI computed each frame as in Sharp mode), or 1 / 2 / 4 rays
    per cache update (GI stored in the cache: cheaper, but it can look blotchy).
  - **Profiler** and **Debug View**: diagnostics for bug reports, see `docs/PROFILER.md`.
- **LOD Quality** (0.1 - 1.0): how far lighting keeps full detail before it gets cheaper. 1.0, the
  default, is maximum quality (no level of detail); 0.5 is a good balance.
- **Light Merging** (Off, Low, Medium, High, Maximum): how aggressively dense groups of the same light
  (lava lakes, glowstone ceilings) are merged into fewer lights. Off by default; Medium is a good balance.
- **Shadow Update Interval** (every frame - 16 frames): Sharp mode, frames between shadow updates of
  nearby pixels. Every frame by default; 6 is a good balance.

## Building

Build with JDK 25: `./gradlew build`. The jar lands in `build/libs/`.

## License

LGPL-3.0-only, with the Photonics attribution term. See `LICENSE.md`, `LICENSE-GPL-3.0.txt` and
`LICENSE-ADDITIONAL-TERMS.md`.
