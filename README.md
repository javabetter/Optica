# Photonics Unofficial Port

> Photonics is free and open-source software, and can be downloaded from https://modrinth.com/mod/photonics (Modrinth), or https://github.com/Redi2Go/PhotonicEngine (GitHub). Anyone can modify and distribute it under the terms of the GNU Lesser General Public License, version 3.

An unofficial port of the Photonics voxel raytracing engine (an Iris extension) to Minecraft 26.1.2
on Fabric, with Euphoria Patches compatibility. It is not affiliated with the Photonics developers.
See `NOTICE.md` for credits. This branch (`photonics-port`) is the port only; **Optica**, on `main`,
is the same port with performance work and a lighting cache.

## Requirements

Minecraft 26.1.2 with Fabric Loader, Fabric API, Iris 1.11.3+ and Sodium 0.9.1+ (Sodium 0.9.1 works
with Voxy). It replaces Photonics; do not install it alongside Photonics or Optica.

## Branches and releases

- `main` (**Optica**): the full mod. It includes Optica's performance work: merging of dense light groups (such
  as lava lakes), distance-based level of detail for lighting in both modes, temporal reuse of
  BASIC-mode direct light, cheaper shadow rays, and a block atlas cache for faster dimension changes.
- `photonics-port` (**Photonics Unofficial Port**, this branch): the Photonics port with compatibility work and bug fixes only, without those
  performance changes. Use it to compare against Photonics, or if you prefer the unmodified
  behaviour.

Each branch has its own release on the Releases page.

## Building

Build with JDK 25: `./gradlew build`. The jar lands in `build/libs/`.

## License

LGPL-3.0-only, with the Photonics attribution term. See `LICENSE.md`, `LICENSE-GPL-3.0.txt` and
`LICENSE-ADDITIONAL-TERMS.md`.
