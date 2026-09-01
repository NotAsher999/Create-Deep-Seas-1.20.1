# Create Deep Seas — Forge 1.20.1 port

This branch ports the upstream Create Deep Seas 2.2.3 source to Minecraft
1.20.1, Forge 47.4.0 and Java 17. It preserves the original single-artifact,
three-mod layout: `create_submarine`, `create_abyss` and `create_high_seas`.

The behavior baseline is upstream commit
`37b876cea7e06e6a3ea56fce998aff9c4a0d3984`. No matching upstream 2.2.3 JAR
was available, so source behavior and history—not binary identity—are the
authoritative baseline.

## Runtime requirements

- Minecraft 1.20.1
- Forge 47.4.0
- Java 17
- Create 6.0.8
- Flywheel 1.0.5
- Ponder 1.0.91
- Veil 1.0.0.296
- [Sable 2.0.5-port.1](https://github.com/NotAsher999/sable-1.20.1)
- [Simulated/Aeronautics 1.3.1-port.1](https://github.com/NotAsher999/Simulated-Project-1.20.1)

Copycats+ 3.0.4, Embeddium 0.3.31, Oculus 1.8.0, Lithostitched and Fusion are
optional integrations. The precise versions and SHA-256 build inputs are in
[`docs/DEPENDENCY_MAP.md`](docs/DEPENDENCY_MAP.md).

Install only production JARs in a game instance. Files ending in `-dev`,
`-sources`, or similar development classifiers are compile/IDE inputs and must
not be placed in `mods`.

## Port status

The Forge workspace builds from a clean checkout when its five hash-locked
local dependency JARs are present. The production JAR has been exercised in the
PJ multi-mod instance through world entry, Sable sublevel creation, F3+T resource
reload, pause/resume, save and clean shutdown. Base, Copycats, Embeddium and
Copycats+Embeddium client profiles, plus base/Copycats dedicated-server profiles,
have also passed their bootstrap gates.

See [`docs/PORT_STATUS.md`](docs/PORT_STATUS.md) and
[`docs/VALIDATION.md`](docs/VALIDATION.md) for evidence and remaining upstream
WIP/behavior boundaries.

## Build and recovery

For development:

```powershell
.\gradlew.bat clean build --no-daemon
```

After committing and creating annotated tag `v2.2.3-port.2`, run
`build-release.bat` to produce a verified local engineering checkpoint. The
minimal-workspace ZIP contains the ignored local dependency inputs and an exact
Git bundle; the ordinary source ZIP intentionally does not.

## Upstream project structure

- **Create Submarine** contains the technical blocks, physics, pressure,
  networking and water-culling systems.
- **Create Abyss** contains the dimension, fauna and plants. Upstream currently
  disables its production entrypoint; this port preserves that policy.
- **Create High Seas** is an upstream placeholder. This port does not invent
  unfinished gameplay.

## License boundary

The upstream project is All Rights Reserved. Its README invites contributions,
which is the basis for maintaining this contribution port. A public compiled
release, redistribution or marketplace upload still requires separate
authorization from the rights holder. Local builds and source checkpoints do
not grant that permission.
