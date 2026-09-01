# Validation record

This record distinguishes source inspection, compilation, automated behavior
checks and real runtime validation. None of the latter three has happened yet.

## Baseline audit — 2026-08-31

| Check | Result | Boundary |
| --- | --- | --- |
| Fork/upstream commit | Pass | Both `main` refs resolve to `37b876c`; divergence is 0/0 |
| Tags/releases | Audited | Three older tags exist; no current 2.2.3 release artifact is published in the repository |
| Original JAR correspondence | Missing input | No mod JAR was supplied; correspondence is not claimed |
| Source inventory | Pass | 174 Java files, 42 Mixins, 266 resources, three Mod IDs |
| Target dependency family | Identified | Forge/Create/Flywheel/Veil/Sable/Simulated/Aeronautics versions follow the established 1.20.1 port stack |
| Forge compilation | Not run | Build still targets NeoForge 1.21.1 and Java 21 |
| Runtime | Not run | No client, dedicated-server, world or visual result exists |

## Upstream behavior boundaries already identified

- `create_abyss` intentionally returns from its constructor in production.
- `create_high_seas` is a placeholder entrypoint; experimental support is not a
  reason to invent finished gameplay.
- Seven custom payloads carry hull config, machine interaction, cracks, bounds,
  camera shake and shark struggle state.
- Fluid/energy capability semantics, Sable physics and Simulated rope behavior
  are complete-chain migration gates.
- Sodium/Veil compatibility Mixins are high-risk and require target bytecode/API
  inspection before edits.

## Rejected completion shortcuts

- Removing Abyss, rope, pressure, worldgen or rendering code to make Gradle pass.
- Returning constants or empty implementations for missing 1.20.1 APIs.
- Treating a main-menu launch as proof of submarine physics, persistence or
  rendering behavior.
- Publishing a binary without resolving the license authorization gate.
