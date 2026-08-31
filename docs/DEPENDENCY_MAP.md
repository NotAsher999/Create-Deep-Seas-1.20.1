# Dependency and subsystem map

## Target runtime

| Dependency | Target | Role |
| --- | --- | --- |
| Minecraft | 1.20.1 | Target game |
| Forge | 47.4.0 | Target loader |
| Java | 17 | Compile and runtime toolchain |
| Create | 6.0.8 | Kinetics, stress, tooltips and Ponder integration |
| Flywheel | 1.0.5 | Block-entity visuals |
| Veil | 1.0.0.296 | Shader and water-occlusion integration |
| Sable | 2.0.5-port.1 | Sublevels, physics, plots, forces and water occlusion |
| Simulated | 1.3.1-port.1 | Rope/winch/connector behavior and rendering |
| Aeronautics | 1.3.1-port.1 | Propeller blocks, renderer and visual base classes |
| Ponder | 1.0.91 | Ponder scenes |
| Registrate | MC1.20-1.3.3 | Create dependency chain |

The exact formal named-development JARs are taken from the existing Sable and
Simulated port workspaces. Production game instances must use the matching
production distributions, never named development JARs.

## Optional integrations

| Integration | Upstream use | Port state |
| --- | --- | --- |
| Copycats+ | Wrench priority Mixin and copied-material hull strength | Exact Forge 1.20.1 artifact/version audit pending |
| Lithostitched | Recommended/optional world-generation support and startup notice | Exact Forge 1.20.1 artifact/version audit pending |
| Fusion | Optional connected-texture assets | Exact Forge 1.20.1 artifact/version audit pending |
| Embeddium/Oculus | Required by the shared PJ renderer stack | Sodium 1.21 mixins require an API/behavior translation, not a package rename |

Offroad belongs to the wider Simulated family test matrix but is not a direct
Deep Seas compile dependency and will not be made mandatory.

## Vertical subsystem map

| Layer | Upstream components | Required migration closure |
| --- | --- | --- |
| Entrypoints | Three `@Mod` classes, common/client event buses | Forge constructors, explicit MOD/GAME bus semantics, production Mixin manifest |
| Registries | Blocks, items, block entities, fluids, effects, menus, entities, creative tab, force groups, density function and condition codec | Forge 47 registries and lifecycle ordering without dropping IDs |
| Data/resources | Recipes, tags, models, languages, shaders, dimension/worldgen data, Fusion overrides | 1.20.1 schemas, recipe paths and optional-resource behavior |
| Sable physics | Ballast/floater forces, propellers, pulley, mines, pressure/sinking, lianas, sublevel persistence | Match formal Sable 2.0.5-port.1 APIs and save lifecycle |
| Simulated ropes | Cable item, strand physics, winch/connector state and renderers | Match formal Simulated 1.3.1-port.1 fields, NBT and constraint semantics |
| Create/Aeronautics | Stress, display sources, visualizers, propeller inheritance | Match Create 6.0.8/Flywheel 1.0.5/Aeronautics 1.3.1-port.1 APIs |
| Capabilities | Fluid and energy storage for machines and rope endpoints | Replace NeoForge block capabilities with Forge `LazyOptional` lifecycle and side rules |
| Networking | Six submarine payloads and one shark struggle payload | Replace 1.21 payload API with direction-locked Forge `SimpleChannel`; preserve validation and bounds |
| Client rendering | Water occlusion, fog, cracks, ropes, propellers, overlays, Veil/Sodium fixes | Translate to Forge/Embeddium/Oculus without renderer-mod-specific bypasses |
| Abyss | Dimension effects, lianas, entities, PDA and shark interaction | Preserve upstream production-disable policy; still compile and validate dev behavior |
| Persistence | Hull config, client state, plant registry, Sable state, machines and ownership | Save/reload and malformed-data boundaries |

## Baseline size

- 174 Java source files
- 42 Mixin classes
- 266 main resource files
- 144 `createsubmarine` Java files
- 26 `AbyssDimension` Java files
- 4 `highseas` Java files
