# Forge 1.20.1 port status

## Current checkpoint

- Branch: `port/forge-1.20.1`
- Source baseline: `37b876cea7e06e6a3ea56fce998aff9c4a0d3984`
- Target: Minecraft 1.20.1, Forge 47.4.0, Java 17
- Upstream version: Create Deep Seas 2.2.3
- Port version: not assigned
- Original matching JAR: not supplied
- State: baseline and dependency audit in progress; no ported build claimed

## System matrix

| System | State | Evidence | Next gate |
| --- | --- | --- | --- |
| Source baseline | Locked | Fork and upstream `main` both at `37b876c` | Preserve baseline ref and record later upstream changes |
| Forge workspace | Not started | Current build is NeoForge 1.21.1 / Java 21 | Establish Forge 47.4.0 / Java 17 build without changing behavior |
| Registries and entrypoints | Mapped | Three Mod IDs and all registry families identified | Compile and verify every original ID |
| Blocks/items/machines | Mapped | Ballast, pressure, oxygen, cable, mine, propeller and display systems present | Port full block/block-entity/capability chains |
| Sable physics | Mapped, not ported | 56 files import Sable; forces, sublevels and Rapier internals are used | Match formal 2.0.5-port.1 source/API behavior |
| Simulated ropes | Mapped, not ported | 61 imports across rope behavior, constraints and renderers | Match formal 1.3.1-port.1 behavior and persistence |
| Networking | Mapped, not ported | Seven 1.21 custom payloads | Direction-locked SimpleChannel implementation and tests |
| Client rendering | High risk, not ported | 27 client Mixins plus Veil/Sodium/Flywheel paths | Embeddium/Oculus/Veil compatibility matrix and visual validation |
| Abyss | Upstream WIP | Upstream constructor disables content in production | Preserve policy; compile and dev-test entities/worldgen/persistence |
| High Seas | Upstream placeholder | Empty entrypoint plus experimental water culling code | Preserve current behavior without inventing content |
| Packaging/recovery | Not started | Existing project has no port release script | Add after a stable compiled checkpoint |

## Missing evidence

- A matching original 2.2.3 release JAR has not been supplied.
- Exact 1.20.1 Lithostitched, Fusion, Copycats and Embeddium/Oculus artifacts
  have not yet been locked for this project.
- No Forge 1.20.1 compilation or runtime result exists yet.
- The license permits contribution proposals in the README, but compiled public
  redistribution still requires separate authorization.

## Immediate route

1. Lock exact formal dependency artifacts and hashes.
2. Replace the build/lifecycle layer and obtain the first complete compile-error
   inventory.
3. Restore registries, capabilities and networking as complete vertical chains.
4. Port Sable/Simulated physics and persistence against their actual formal
   1.20.1 sources.
5. Port client rendering last, then validate client, dedicated server, saves and
   the full PJ multi-mod stack.
