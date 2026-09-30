# Optica — research notes

Research done on 2026-09-26, before any engine code was written. Optica's goal is to be a
Photonics-equivalent voxel raytracing engine for Minecraft **26.1.2** (Fabric), keeping
**Euphoria Patches** compatible.

---

## 1. The most important finding: Photonics is open source (LGPL-3.0)

- Repo: <https://github.com/Redi2Go/PhotonicEngine> (authors: **Redi2Go** and **Essentuan**).
  `master` is nearly empty (a README and CODEOWNERS), but the other branches hold the full source:
  - `multi-version`: last commit 2026-08-16, mod version **0.4.0-dev**, targets only 1.21.11.
    It is split into a version-independent `core` and a per-version `versions/1_21_11`.
    **This is the best base for a port.**
  - `voxelization-rewrite`: last commit 2026-08-27. An in-progress restructure into `engine`/`game`
    modules; still 1.21.11.
  - `docs`: the shader-developer documentation for the 0.3.1 API (`README.md`, `ph_lights.json`,
    and sample `shader_interface.glsl` and `write_indirect.glsl`).
- `LICENSE.md` is **LGPL-3.0**. `LICENSE-ADDITIONAL-TERMS.md` adds a GPLv3 §7(b) attribution term:
  every download page for Photonics or a modified version must show this notice near the top
  (within the first ~10 sentences, before any download link):
  > Photonics is free and open-source software, and can be downloaded from https://modrinth.com/mod/photonics (Modrinth), or https://github.com/Redi2Go/PhotonicEngine (GitHub). Anyone can modify and distribute it under the terms of the GNU Lesser General Public License, version 3.
- ⚠️ Conflict: the branch's `fabric.mod.json` still says `"license": "All-Rights-Reserved"`. The
  LICENSE file normally wins, but ask the authors (Photonics Discord or a GitHub issue) before
  publishing a fork.

### Decision (2026-09-26): port Photonics, relicense Optica to LGPL-3.0-only
The user chose option A, porting Photonics. The repo now has `LICENSE.md` (LGPL-3.0),
`LICENSE-GPL-3.0.txt`, `LICENSE-ADDITIONAL-TERMS.md` (Photonics' §7(b) notice, which carries over
to modified versions) and `NOTICE.md`. The README shows the required notice at the top.
`fabric.mod.json` says `LGPL-3.0-only`, and the jar bundles the license texts under
`META-INF/licenses/optica/`. Obligations:
- Every download page (Modrinth, CurseForge, GitHub releases) must show the Photonics notice
  before its first download link.
- Files copied from Photonics and then changed must say so (GPLv3 §5(a)), e.g. a one-line
  header: `Modified from Photonics (github.com/Redi2Go/PhotonicEngine) for Optica`.
- Publish the source for every released jar. This GitHub repo covers that.
- Do **not** copy code from Photon (custom license) or from Complementary/Euphoria
  (no redistribution). Only Photonics code may be reused.

---

## 2. What Photonics is

- An **Iris extension**, not a standalone renderer. It hooks into Iris's pipeline with mixins.
  Requires Fabric, Iris and Sodium. OpenGL 4.3+ features (SSBOs, `glTexStorage3D`, DSA from
  `GL45C`). No hardware RT; macOS is unsupported (it has no GL 4.3).
- Voxelizes the loaded world into a GPU sparse structure (world → chunk → block nodes). Block
  models are rasterized into **16×16×16 voxels per block**, with a palette and an atlas texture.
- Features: traced block lights with colored shadows through glass/tint, sky GI, handheld lights,
  "voxelized blocks" rendered through the `gbuffers_voxels`/`shadow_voxels` programs,
  world-space reflections (0.3.0), transparency modes, and three lighting modes: `OFF`, `BASIC`
  ("Sharp"), and `RESTIR` (ReSTIR DI + GI, with an SVGF-style denoiser).
- Shader-pack integration works two ways:
  1. **Native support.** The pack ships `/shaders/photonics/*.glsl` files and `photonics.*` keys
     in `shaders.properties`. Photon, Eclipse, Shrimple and **Euphoria Patches** work this way.
  2. **Runtime patches.** A `patch.json` plus `#file/#template/#replace/#create` directive files,
     loaded from `.minecraft/shader-patches`, from the jar, or from the dev resources. The bundled
     example is `modules/patches/BSL_v10.1`.

### Source layout (`multi-version` branch)
```
modules/api                  44 files / 1.3k lines   abstraction interfaces (gpu buffers/textures, mc world/registry wrappers)
modules/core                191 files / 12.3k lines  engine: voxel tree, compiler, bakery, palette, lights, config (ph_lights.json),
                                                    Iris pipeline glue (PhotonicsPipeline/Sharp/Restir/Off), properties, patcher
modules/versions/1_21_11/common 140 files / 7.2k lines  MC/Iris/Sodium mixins + impls of the api interfaces
modules/versions/1_21_11/fabric   2 files               Fabric entrypoint (at.redi2go.photonics.client.PhotonicsClientFabric)
modules/shaders/photonics    ~85 GLSL files             tracing, lighting (sharp/restir/svgf/frag passes), interface stubs, modifiers
modules/patches/BSL_v10.1                               example runtime patch
```
Build: Gradle Kotlin DSL with version catalogs. Loom 1.17.3 (the `fabric-loom-remap` plugin for
1.21.11). Shadow-jars `glsl-transformer`, `antlr4-runtime`, `jcpp`, `fastutil-concurrent-wrapper`
and `semver4j`. Pulls Iris and Sodium from the **Modrinth maven** (`maven.modrinth:iris`,
`maven.modrinth:sodium`).

### Mixins the version module applies (the 26.1.2 port surface)
- **Iris** (all class names still exist in Iris's `26.1` branch):
  `IrisRenderingPipeline`, `PipelineManager`, `CompositeRenderer` (+`$Pass`),
  `ShadowCompositeRenderer`, `FinalPassRenderer`, `ShaderPack`, `ShaderProperties`,
  `IncludeGraph`, `JcppProcessor`, `ShaderPackSourceNames`, `AbsolutePackPath`, `IrisSamplers`,
  `SamplerHolder`, `UniformHolder`, `DynamicUniformHolder`, `UniformUpdateFrequency`,
  `CommonUniforms`.
- **Sodium**: `gl.shader.GlProgram` and `render.chunk.RenderSectionManager` (both exist on the
  `26.1.2/stable` branch).
- **Blaze3D**: `GlDevice`, `GlCommandEncoder`, `GlBuffer` (+`GlMappedView`),
  `BufferStorage$Immutable`, `DirectStateAccess`, `GlDebugLabel$Core/Ext/Empty`, `GlSampler`,
  `GpuSampler`, `GpuBufferSlice`, `AddressMode`, `FilterMode`, `InternalTextureFormat`.
- **Minecraft**: `BlockRenderDispatcher`, `FeatureRenderDispatcher`, `RenderType`, `RenderSetup`
  (+`$TextureBinding`), `OuterWrappedRenderType`, `DynamicTexture`, `ReloadableTexture`,
  `LevelRenderer`, `LevelChunkSection`, `ChunkAccess`, `Level`, `LevelReader`, `BlockGetter`,
  `LevelHeightAccessor`, `Block`, `BlockState`, `StateDefinition`, `Property`, `BlockEntity`,
  `BlockStateParser$BlockResult/$TagResult`, `Holder`, `HolderSet`, `HolderLookup`,
  `RegistryAccess`, `Registries`, `Identifier`, `BlockPos`, `CompoundTag`, `Inventory`,
  `Minecraft`.

---

## 3. 1.21.11 → 26.1 changes that affect the port

Source: NeoForged's 26.1 primer (`primers/26.1/index.md` in <https://github.com/neoforged/.github>).
- Java **25**. Minecraft ships **unobfuscated** (Mojang names), so Loom needs no mappings and the
  1.21.11 `fabric-loom-remap` / mixin-remapping build logic can be dropped. Our skeleton already
  uses the plain `net.fabricmc.fabric-loom` 1.17.17 with no mappings.
- **`BlockRenderDispatcher` and `ItemRenderer` are removed.** Block models now go through
  `BlockModel`/`BlockModelResolver`/`BlockModelRenderState` feature submission. Photonics' meshing
  (`MinecraftBlockMesher`, `McBlockRenderer`, `BlockRenderDispatcherMixin`, which reads
  `ModelBlockRenderer`) must be rewritten. This is the biggest single port item.
- `VertexConsumer#putBulkData` → `putBlockBakedQuad`/`putBakedQuad` taking a `QuadInstance`; new
  `BlockQuadOutput`. This affects `VertexBuilderMixin` and the bakery's vertex capture.
- Render layers are now computed per quad from texture transparency (`ItemBlockRenderTypes` is
  gone; `BakedQuad$SpriteInfo` has the layer). `ChunkSectionLayer#TRIPWIRE` is removed.
- Sections are uploaded into an `UberGpuBuffer` with `TlsfAllocator`;
  `SectionRenderDispatcher` takes a `SectionCompiler`.
- Blaze3D split into wrapper + `*Backend` interfaces. `GlDevice` implements `GpuDeviceBackend` and
  `GlCommandEncoder` implements `CommandEncoderBackend`, and **both are now package-private**, so
  mixins must use `targets = "..."` strings and accessors. `RenderPipeline` depth/blend moved
  to `DepthStencilState`/`ColorTargetState`, and `DepthTestFunction` became `CompareOp`.
- `CameraRenderState`: the camera is extracted per frame. Features render in separate solid and
  translucent passes. `ChunkPos` is a record.
- **26.1 is the last OpenGL-only release.** 26.2 adds a Vulkan backend (with parallel
  `com.mojang.blaze3d.vulkan` classes), and OpenGL removal is planned later. Photonics' raw-GL
  code (`GlBufferHeap`, `GlTexture2D/3D`, `SingleFramebuffer`, `GlBufferHolder`) is fine for
  26.1.2 but is a long-term risk.

Dependency versions for 26.1.2 (from Iris's `26.1` branch, 2026-09-14):
Minecraft `26.1.2`, Fabric Loader `0.19.3`, Fabric API `0.154.2+26.1.2` (our skeleton has
`0.155.2`), **Sodium `0.9.2+mc26.1.2`**, **Iris `1.11.4`** (Modrinth has `1.11.1`/`1.11.2` for
26.1.2 and 26.2).

---

## 4. Shader-facing API Optica must replicate (for Euphoria Patches and other packs)

Every Photonics-aware pack talks to it only through this contract. **To be compatible with
Euphoria Patches, Optica must match it exactly**: same property keys, macros, file paths,
function names and struct layouts. The full reference is the `docs` branch README (0.3.1). The
0.4 changes are in `DOCUMENTATION.md` on `multi-version`; 0.4 keeps the 0.3 names through
`@Key(legacy=...)` and `photonics/deprecated/shader_interface.glsl`.

**`shaders.properties` keys**: `photonics.enabled`, `photonics.supported` (patches only),
`photonics.renderScale`, `photonics.maxLights`, `photonics.alphaMode` (`none|block|voxel`),
`photonics.enableGi`, `photonics.enableBlockLight`, `photonics.enableHandheldLight`,
`photonics.enableLightBinning`, `photonics.voxelizeLava`, `photonics.lightingMode`
(`OFF|BASIC|RESTIR`), `photonics.maxSamples`, `photonics.restirInitialSamples`,
`photonics.restirSpatialReuseSamples`, `photonics.restirSpatialReuseRadius`,
`photonics.restirAccumulationFrames`, `photonics.restirDenoiserPasses`,
`photonics.restirSoftShadows`, and `photonics.restirCombinedGi`. Photon also uses
`photonics.enchantmentGlintStrength` and `photonics.useSeparateHandheldRays`. 0.4 adds
`photonics.maxGiBounces` and `photonics.enableBlockLightGi`.

**Macros** (in shaders and properties files): `PHOTONICS` (always defined when installed; packs
use it for feature detection), `PHOTONICS_VERSION` (encoded as major·10000 + minor·100 + patch,
e.g. 0.3.4 → 304; EP needs ≥ 0.3.4), `PH_RENDER_SCALE`, `PH_MAX_LIGHTS`, `PH_MAX_SAMPLES`,
`PH_ENABLE_GI`, `PH_ENABLE_BLOCKLIGHT`, `PH_ENABLE_HANDHELD_LIGHT`, plus
`OVERWORLD`/`NETHER`/`END` inside `/photonics`. Packs may set `PH_USE_CUSTOM_ALPHA`,
`PH_ALPHA_FUNC`, `PH_USE_CUSTOM_AIR_ID` and `PH_AIR_ID`.

**Uniforms**: `world_offset`, `world_camera_position`, `rt_camera_position`, `handheld_color`,
`light_reload`.

**Files the mod injects** (packs ship empty stubs so they still load without the mod):
`/photonics/photonics.glsl` (includes `ph_core` and `ph_raytracing`), `/photonics/ph_samplers.glsl`,
and `ph_sampling`, which provides `sample_photonics_direct(uv)` and
`sample_photonics_handheld(uv)`.
Tracing API: `struct RayJob {origin, direction, result_position, result_normal, result_color, result_hit}`,
`trace_ray(inout RayJob[, bool transparency])`, `ray_constraint`, `result_tint_color`,
`result_block_id`, `get_result_sky_light(normal)`, `get_block_id(rt_pos)`, and
`struct Light` with `load_light(i)`.

**Files the pack provides**: `/photonics/shader_interface.glsl` (`load_world_position()`,
`load_fragment_variables(albedo, world_pos, world_normal, world_normal_mapped)`, `sun_direction`,
`indirect_light_color`, `get_taa_jitter()`, `is_in_world()`) and `/photonics/write_indirect.glsl`
(`write_indirect(vec3)`). It may also provide the optional `/photonics/modifiers/*.glsl` (light,
restir_gi, is_hand, attenuation, voxel_color, and so on). Pack files override Photonics'
defaults, except `photonics.glsl`.

**New Iris programs**: `gbuffers_voxels` and `shadow_voxels`, which draw a block-sized cage mesh
and trace inside it. The mod must register these program names with Iris.

**`/shaders/ph_lights.json`** (preprocessed): per-block light color, intensity, radius, falloff and
`is_traced`, keyed by block-state predicates or `*<blocks.properties id>`. Priority order: user
config, then mods, then the shader. The default table is `resources/ph_lights.json` on the `docs`
branch.

A worked example of all of this is Photon's `shaders/photonics/` and `world*/{gbuffers,shadow}_voxels.*`
(<https://github.com/sixthsurge/photon>, written by Essentuan), plus
`Essentuan/photon@photonics-example`.

---

## 5. Euphoria Patches specifics

- EP is an add-on for Complementary Reimagined/Unbound by SpacEagle17. It is distributed as the
  **EuphoriaPatcher** mod (MPL-2.0, <https://github.com/EuphoriaPatches/EuphoriaPatcher>),
  currently at `1.10.5-r5.9.3`. The patcher applies a binary `.patch` that ships only inside the
  release jar, so **EP's shader source is not in any public repo**.
- EP 1.9.0+ supports Photonics natively and requires Photonics ≥ 0.3.4.
- How EuphoriaPatcher detects Photonics, from its source:
  - `ModChecker.PHOTONICS` checks that one of these classes exists:
    `at.redi2go.photonics.client.Photonics` or `at.redi2go.photonic.client.Photonic`.
    If it does, the patcher injects the shader define **`EUPHORIA_PATCHES_IS_PHOTONICS_INSTALLED`**.
  - `PhotonicsRaytracerMixin` (`@Pseudo`) targets
    `at.redi2go.photonic.client.Raytracer#readShaderFile(path, boolean)` and turns a `null`
    return into `""` (a workaround for missing files).
  - Optica's classes won't match that check, but that's harmless (see below).
### Findings from the real EP 1.10.5 pack (supplied by the user, kept out of git)
- EP ships native Photonics files: `shaders/photonics/{photonics,ph_samplers}.glsl` (empty
  stubs), `shader_interface.glsl`, `write_indirect.glsl`, and modifiers `light_modifier`,
  `restir_gi_modifier`, `restir_denoiser_depth_fetch_modifier`, `handheld_light_pulse_modifier`,
  `voxel_color_modifier` and `is_hand_modifier`. It also ships `shaders/ph_lights.json` and
  `gbuffers_voxels`/`shadow_voxels` in each `world*/`.
- Gate in `shaders.properties`: `defined PHOTONICS && PHOTONICS_VERSION >= 301 && !defined MC_OS_MAC && defined IS_IRIS`.
  `PHOTONICS_VERSION >= 302` is needed for `photonics.enableHandheldLight`. **Optica must define
  `PHOTONICS` and a `PHOTONICS_VERSION` ≥ 302**, and should report the Photonics API version it
  implements (e.g. 400 for the 0.4.0 base), not Optica's own version.
- Keys EP sets: `photonics.supported/enabled/alphaMode/maxLights/maxSamples/lightingMode (BASIC|RESTIR)/renderScale/enableGi/enableHandheldLight/enableBlockLight/restirCombinedGi/useSeparateHandheldRays/enchantmentGlintStrength/restirInitialSamples/restirSpatialReuseSamples/restirSpatialReuseRadius/restirAccumulationFrames/restirDenoiserPasses/restirSoftShadows`.
  EP also resizes `colortex9` by `PHOTONICS_RENDER_SCALE`.
- It relies on the **undocumented uniform `phFirstBuildTime`** (set once by `WorldCompiler` after
  the first world build) to fade Photonics lighting in, and it treats |x| or |z| > 30000 as out of
  range. Keep that uniform.
- `modify_light` reads `Light.blockId`, `Light.index`, `Light.color` and `Light.intensity`. These
  are 0.4-era `Light` struct fields that exist in the `multi-version` source, which confirms that
  branch is the right base.
- Other API it uses: `RayJob`, `trace_ray`, `ray_constraint`, `result_*`, `get_block_id`,
  `world_offset`, `rt_camera_position`, `sample_photonics_direct/handheld`,
  `PH_USE_CUSTOM_ALPHA` and `PH_ALPHA_FUNC`.
- `EUPHORIA_PATCHES_IS_PHOTONICS_INSTALLED` is used **only** to show an "update Photonics"
  warning when `PHOTONICS_VERSION < 301`. Optica does **not** need the EuphoriaPatcher
  detection shim.
- To inspect it again, unzip `Euphoria_Patches_1.10.5.zip` into a local, git-ignored folder.
  Never commit it.

---

## 6. Environment blockers in this cloud container

Found while researching. They must be fixed before code can be built or tested here.
- JDK 25: installed on 2026-09-26 with `apt-get update && apt-get install -y openjdk-25-jdk-headless`
  (it is now the default `java`). The container is ephemeral, so a fresh container needs the same
  command again, or a SessionStart hook.
- The network policy **blocks**: `maven.fabricmc.net` (Loom, Fabric Loader and Fabric API),
  `api.modrinth.com`/`modrinth.com` (the Modrinth maven for Iris and Sodium),
  `piston-meta.mojang.com`, `piston-data.mojang.com`, `libraries.minecraft.net` and
  `resources.download.minecraft.net` (Minecraft itself), plus `curseforge.com`, `fabricmc.net`
  and `docs.neoforged.net`. These work: `github.com` over git, `services.gradle.org`,
  `plugins.gradle.org` and `repo.maven.apache.org`.
  → Add these to the environment's allowed domains: `maven.fabricmc.net`, `api.modrinth.com`,
  `piston-meta.mojang.com`, `piston-data.mojang.com`, `libraries.minecraft.net`,
  `resources.download.minecraft.net`. Alternatively, build Iris and Sodium from source over git
  (both repos can be cloned), but Loom and Minecraft still need the Fabric and Mojang hosts.
- A GPU/GL 4.3 context isn't available here, so runtime testing has to happen on the user's
  machine. CI can only compile.

---

## 7. Suggested plan for the implementation phase

1. ~~Settle the license question~~ Done: LGPL-3.0-only. Optionally tell the Photonics authors.
2. Fix the environment (~~JDK 25~~ done; allowed domains still needed) and get the empty skeleton to build.
3. Import Photonics `multi-version` (`api`, `core`, `shaders`, `resources`, `patches`) under
   `com.optica`, keeping its license headers and adding NOTICE and attribution. Replace the
   1.21.11 remap build logic with plain Loom 1.17.x for unobfuscated 26.1.2. Add Iris `1.11.x` and
   Sodium `0.9.2+mc26.1.2` as `modImplementation`/`compileOnly` dependencies.
4. Create `versions/26_1_2` from `1_21_11` and fix the mixins against the real 26.1.2, Iris and
   Sodium classes. Order: Blaze3D GL wrappers, then Iris pipeline hooks, then world/chunk
   access, then the **block meshing rewrite** (replacing `BlockRenderDispatcher`).
5. Keep the shader API byte-identical: keep the `PHOTONICS`/`PHOTONICS_VERSION` macro names
   (report ≥ 302 for Euphoria; 400 for the 0.4.0 base) and keep the `phFirstBuildTime` uniform.
6. Test with Photon (open source, in-repo example), then BSL with the bundled patch, then Euphoria
   Patches on real hardware.

## Sources
- Photonics: <https://modrinth.com/mod/photonics>, <https://github.com/Redi2Go/PhotonicEngine> (branches `docs`, `multi-version`, `voxelization-rewrite`)
- Photon shader (reference integration): <https://github.com/sixthsurge/photon>
- EuphoriaPatcher: <https://github.com/EuphoriaPatches/EuphoriaPatcher>, <https://www.euphoriapatches.com/>
- Iris `26.1` branch: <https://github.com/IrisShaders/Iris>; Sodium `26.1.2/stable`: <https://github.com/CaffeineMC/sodium>
- 26.1 primer: <https://github.com/neoforged/.github/blob/main/primers/26.1/index.md>; Fabric 26.1 post: <https://fabricmc.net/2026/03/14/261.html>
- Vulkan transition: <https://www.minecraft.net/en-us/article/another-step-towards-vibrant-visuals-for-java-edition>

---

## 9. Runtime findings (first in-game tests, 2026-09-26)

Optica builds, launches and loads worlds on 26.1.2 with Iris 1.11.4 and Sodium 0.9.2, and also with
Iris 1.11.3 and Sodium 0.9.1 (the Sodium build Voxy 0.2.18 pins; both Iris versions accept any 0.9.x).
The build compiles against the oldest supported pair (Iris 1.11.3, Sodium 0.9.1). All mixins
apply. The whole engine runs (world compiler, chunk compilers, light list, world workers), and
Euphoria Patches 1.10.5 loads with Optica: its `ph_lights.json` is parsed, and the
`photonicsMasterTimer`/`phFirstBuildTime` fade works.

**Critical discovery: the public Photonics source is an unfinished 0.4 rewrite.**
- `multi-version` began on 2026-04-01 as a re-architecture ("Port config to new project"). The
  private 0.3.x Java code was carried over in pieces. Only the 0.3.5 *shaders* were imported
  (commit `f7c6931`); they were deleted on 2026-05-12 (`1ec8761`). No commit contains the complete,
  released 0.3.x engine.
- The 0.4-dev branch Optica is based on (`54e049b`) is **not compatible with the 0.3.5 shader API
  that Euphoria Patches, Photon, BSL and others use**:
  - `photonics.glsl` is empty: there is no `RayJob`, `trace_ray`, `ray_constraint`,
    `result_block_id`, `result_tint_color`, `get_result_sky_light`, `get_block_id` or `load_light`.
    EP's `voxel_color_modifier` uses `result_block_id`, so ReSTIR mode fails to compile
    (``h0_handheld.fsh: `result_block_id' undeclared``).
  - BASIC ("Sharp") lighting is a stub: `rendering/sharp/samplers.glsl` returns `vec3(0)`. EP
    defaults to BASIC, so it shows no traced block light. This was verified with a debug overlay:
    `photonicsMasterTimer` = 1 and the fade = 1, but `sample_photonics_direct` = 0.
  - Reference for the 0.3.5 API behaviour: `git show f7c6931:modules/shaders/ph_raytracing.glsl`
    (and `ph_core.glsl`, `basic/lighting.fsh`, `restir/*`) in the PhotonicEngine repo.
- **Plan:** keep the 0.4 engine (the most complete, and ReSTIR is implemented) and add an Optica
  **0.3.5 compatibility layer**:
  1. A legacy `photonics.glsl`/`ph_core`/`ph_raytracing` API built on the 0.4 `RayIterator`/
     `RayResult`: `RayJob`, `trace_ray`, `ray_constraint`, the `result_*` globals, `load_light`,
     `get_block_id` and `get_result_sky_light`.
  2. Implement BASIC mode, either by porting 0.3.5 `basic/lighting.fsh` onto the 0.4 light list
     and tracing, or as a stopgap by mapping BASIC to ReSTIR.
  3. Report `PHOTONICS_VERSION` 305 (0.3.5) once the compat layer exists; it currently reports 400.

**Compatibility layer status (implemented, verified in the dev client with EP 1.10.5)**
- Integer voxel textures had no sampler state, so they were incomplete and read as 0, and all
  traced light was black. Fixed in `AbstractGlTexture` (NEAREST filtering, clamp).
- Legacy globals (`result_block_id`, `result_tint_color`, `ph_result_sky_brightness`,
  `ray_constraint`) are in every program that includes the palette. `result_block_id` is set
  before `voxel_color_modifier` runs, so EP's ReSTIR mode compiles and runs.
- The legacy `photonics.glsl` implements the 0.3.x API on the 0.4 `RayIterator`:
  - `RayJob`, `trace_ray` (with `ray_constraint`, transparency tinting through
    `PH_USE_CUSTOM_ALPHA`/`PH_ALPHA_FUNC`, and `PH_FULL_TRANSPARENCY`), `RAY_ITERATION_COUNT`.
  - `get_result_sky_light`, `get_block_id` (a constrained probe down the block's centre column),
    `load_light`, `load_main_hand_light`, `load_off_hand_light`.
  - Verified with a temporary overlay: legacy traces hit the expected voxels, and `get_block_id`
    resolves them.
- BASIC mode (`SharpPipeline`):
  - Light bins (`LightBins`, an 8-block cell grid of 64³ cells around the camera) feed a
    direct-light pass. The pass keeps the `PH_MAX_SAMPLES` brightest reachable lights per
    fragment and traces a shadow ray to each.
- Direct light is cached over time, as in 0.3.x: a pixel with valid reprojected history reuses it and
  re-traces its lights only every `PH_SHARP_REFRESH_INTERVAL` (4) frames, interleaved 2x2. Newly
  revealed pixels and the hand are traced every frame. This cuts BASIC mode's shadow rays roughly 4x.
- Legacy GI: when the pack's combined GI is off (BASIC, or ReSTIR with combined GI disabled), a
  1-spp temporally accumulated sky GI pass plus an edge-aware blur feeds the pack's
  `write_indirect()` (EP: `colortex9`).
- `PHOTONICS_VERSION` stays 400. Every EP gate is a lower bound (`>= 301`/`>= 302`), and 400 also
  tells packs the 0.4 API exists. (The patch loader's "unsupported version" warning was inverted;
  that is fixed.)
- Not yet supported: `gbuffers_voxels`/`shadow_voxels` programs ("voxelized blocks"). The legacy
  tracing API they use is ready, but the programs are not yet rendered.

**Robustness and quality fixes (from real-hardware testing)**
- Iris pipelines snapshot Photonics' renderers at creation. Other mods (e.g. Voxy) construct
  `ShaderProperties` too, which used to tear down the live Photonics pipeline under a cached Iris
  pipeline ("unexpected active renderers size" crash on Hypixel). Identical properties are now a
  no-op. Stale Iris pipelines are detected by a generation counter and trigger a pipeline rebuild
  (see "Pipeline lifecycle (round 3)"). A mismatch disables Photonics for that pipeline instead of throwing.
- Light LOD: dense groups of identical lights are merged per cell, with cells growing with distance
  (2/4/8/16 blocks). Brightness gain is `count^1` for ReSTIR (it sums all lights) and `count^0.5`
  for BASIC (it sums only the brightest `PH_MAX_SAMPLES`). The attenuation constant is softened by the
  members' spread. Test world: 3,588 → about 840 lights.
- Lighting rendered at `PH_RENDER_SCALE < 1` is upsampled depth-aware (`rendering/upsample.glsl`).
  0.4's `is_hand_at()` and the default depth fetch ignored the render scale; they now go through the
  scaled 0.3.x hooks. The legacy GI and BASIC history never mix hand and world pixels.
- A stall watchdog logs the render thread's stack when the game has not ticked for 5 s.

**Performance round 2**
- Light merging is coarser: cells of 2/4/8/16/32 blocks by distance, lava one level coarser, and
  groups of 2+ merge in cells of 8 or more. If the result still exceeds the light budget, the cells
  are doubled (up to 3 passes).
- Distance LOD in BASIC: the direct-light refresh interval is 4/8/16 frames and the light count is
  100%/50%/25% of `PH_MAX_SAMPLES` (thresholds 16/24/40/64 blocks). Legacy GI traces every
  2/4/8 frames; its blur is 3x3 beyond 32 blocks.
- `trace_light_vis` ignored `max_iterations` (always 100). BASIC now uses 64, which was visually
  identical in testing; ReSTIR passes 100, which keeps its old behavior.
- Downloaded block atlases (and PBR maps) are cached across pipelines through soft references, so a
  dimension change no longer reads them back from the GPU. The cache is cleared on resource reload
  and keyed by shader pack.

**Pipeline lifecycle (round 3)**
- Parsing shader properties never destroys the running Photonics pipeline; real option changes go
  through an Iris reload, which destroys it anyway.
- The Photonics pipeline remembers its level. If the level changes while Iris pipelines survive (some
  mod prevented `destroyPipeline` on Hypixel), `preparePipeline` tears down all Iris pipelines and
  Photonics at its start. Out-of-sync Iris pipelines request the same rebuild (at most once per 10 s)
  instead of calling `Iris.reload()`, which ran synchronously inside `preparePipeline` through
  `Minecraft.execute` and crashed ("Tried to use a destroyed GlResource").
- Watchdog finding: first-visit dimension freezes are Iris' CPU-side shader transformation
  (glsl-transformer), not driver compilation, so the NVIDIA shader cache size does not matter.
- ReSTIR distance LOD: initial candidates 100/50/25% (32/64 blocks), the di0 visibility pre-check is
  skipped beyond 48 blocks, and GI paths are traced every 1/2/4 frames (48/96 blocks). BASIC's LOD
  boundaries were pushed out (samples 100/75/50% at 32/64 blocks), and its base refresh interval is 6.

**Cached lighting mode**
- Selected on the Optica page of the pack's Iris settings menu. `/photonics/optica_settings.glsl`
  is a virtual pack file whose `#define ... // [...]` lines Iris discovers as pack options. It is
  only added for packs Optica runs with. `ShaderPropertiesMixin` adds `screen.OPTICA`, links it
  from the main `screen`, and registers sliders. Iris reads the layout from the *original*
  (un-preprocessed) properties, so that copy is the one patched. `LanguageMapMixin` merges in the
  labels. On load, `OpticaSettings` reads the option values, overrides the pack's
  `photonics.lightingMode` with `CACHED`, and passes the settings on as `optica.*` keys
  (`CachedProperties` turns them into `PH_CACHE_*` defines).
- World-space surface cache (`rendering/cached`). Samples sit on a lattice over axis-aligned face
  planes (planes in 1/16 steps; N x N cells per block, N = `cacheDetail` near the camera, halving
  at 16/40/96 blocks). Non-axis-aligned surfaces and the hand use one coarse sample per block. Each
  pixel interpolates four samples. The GPU hash table has 8-slot linear probing and 32-byte entries
  (key, last-used frame, flags, half-float direct and GI). Entries unused for 900 frames get
  replaced.
- Passes: c0 finds or creates the pixel's samples (new ones go into a queue); c1 (fixed 512x192)
  computes queued samples, then a rotating slice of the table sized so everything recently visible
  is refreshed once per `cacheRefreshSeconds`; c2 interpolates into `sharp_direct`; c3 feeds
  `write_indirect()`. The direct light is BASIC's top-`PH_MAX_SAMPLES` light selection with shadow
  rays. GI is `sample_indirect`, averaged over up to 8 refreshes.
- Verified in the dev client with EP: same average brightness as BASIC in a test scene (30.4 vs
  29.9), and a newly placed light appears after its refresh.

**Other runtime notes**
- Memory: the voxel world uses about 64 KB per non-empty section in a fixed 512 MB heap
  (`BufferWorldAllocator(1 << 29)`, unchanged from Photonics). Render distance 6 levels off at
  about 100 MB. Render distance 16 exhausts the heap (`OutOfMemoryError: Could not allocate ...`
  in world workers). TODO: size the heap from the render distance, or cap the voxelized radius.
- The allocator only reuses exact-size free regions and never defragments (also inherited).
- 26.1 graphics presets (`graphicsPreset`) re-apply the render distance at startup; use
  `graphicsPreset:"custom"` to control it.
