# Dependency and subsystem map

## Target runtime

| Dependency | Target | Role/source |
| --- | --- | --- |
| Minecraft | 1.20.1 | Target game |
| Forge | 47.4.0 | Loader and networking/capability lifecycle |
| Java | 17 | Compile and runtime toolchain |
| Create | 6.0.8 | Kinetics, stress, items and Ponder integration |
| Flywheel | 1.0.5 | Block-entity visuals |
| Ponder | 1.0.91 | Ponder scenes |
| Veil | 1.0.0.296 | Shader and water-occlusion integration |
| Sable | 2.0.5-port.1 | [Formal Forge port](https://github.com/NotAsher999/sable-1.20.1) |
| Simulated/Aeronautics | 1.3.1-port.1 | [Formal Forge port](https://github.com/NotAsher999/Simulated-Project-1.20.1) |

## Hash-locked local build inputs

These are named development artifacts. They belong in `localDeps` for building
the project and must not be installed in a normal game instance.

| File | SHA-256 |
| --- | --- |
| `sable-forge-1.20.1-2.0.5-port.1-dev.jar` | `0A23C66EF70AB04DAB2DF03BB5150AB6576561F446A7CED709FD819A30D3E216` |
| `simulated-forge-1.20.1-1.3.1-port.1-dev.jar` | `B9E75E12C3928D6A9F3BD16E5249A0AD74FAB239538F76BAD4DE4A58F9915DB8` |
| `aeronautics-forge-1.20.1-1.3.1-port.1-dev.jar` | `BD404E06850282D908A73E32030C251284363B182CA93579B3A2241A992245DA` |
| `Veil-forge-1.20.1-1.0.0.296-dev.jar` | `24717637D0B20B2FDE9A25B7456E05FD8F1035FF37000E7BD095742B7DA41E19` |
| `oculus-mc1.20.1-1.8.0.jar` | `0945DF0CBA0F62B3901DD80C3268E5311B770ECE78C78037A45DB12AC0425FEF` |

Oculus is a production-format optional runtime input retained in the minimal
workspace solely to reproduce the validated PJ renderer matrix. The normal
compile does not require it.

## Optional integration contracts

| Integration | Locked version/hash | Port behavior |
| --- | --- | --- |
| Copycats+ | 3.0.4 / `7D684E6A829FCACF5AB94D20044CAD0E7EB5376CBE02D77954FC41A2D264511F` | Conditional wrench priority and copied-material hull strength |
| Embeddium | 0.3.31 / `EED3D1325F2ACC2FD4E69BB495E5CCB91D962126AC5330F0582EBC2A3DAF47FB` | Explicit replacement for upstream Sodium renderer hooks |
| Oculus | 1.8.0 / hash above | Production PJ shader-stack validation |
| Lithostitched | optional, unconstrained upstream API | Worldgen modifiers and existing startup recommendation |
| Fusion | optional, resource-only | Connected-texture override assets |

Gradle dependency verification locks resolved Maven artifacts. The Copycats and
Embeddium tests additionally verify the exact public bytecode contracts used by
the conditional Mixins.

## Vertical subsystem closure

| Layer | Migrated chain |
| --- | --- |
| Entrypoints | Three `@Mod` classes, explicit Forge MOD/GAME buses, manifest Mixin discovery |
| Registries | Blocks, items, block entities, effects, menus, entities, creative tab, force groups, density function and condition codec |
| Data/resources | 29 recipes, loot/tags, models, languages, three-mod metadata, shaders and worldgen schemas |
| Physics | Ballast/floater aggregation, propellers, pulley, mines, pressure/sinking and liana force paths |
| Ropes | Cable item, strand state, winch/connector behavior, rendering, constraints and NBT |
| Capabilities | Forge fluid/energy providers with side exposure and `LazyOptional` invalidation |
| Networking | Seven direction-locked `SimpleChannel` messages and client/server handlers |
| Rendering | Vanilla/Embeddium routing, Veil shader modifiers, fog/cracks/ropes and water occlusion |
| Persistence | Machine state, hull config, client state, ownership and Sable sublevel save lifecycle |

Offroad and unrelated PJ addons are compatibility-matrix participants, not
direct Deep Seas compile dependencies, and are not made mandatory.
