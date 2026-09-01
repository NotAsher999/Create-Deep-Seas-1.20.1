# Forge 1.20.1 port status

## Current checkpoint

- Branch: `port/forge-1.20.1`
- Upstream source baseline: `37b876cea7e06e6a3ea56fce998aff9c4a0d3984`
- Target: Minecraft 1.20.1, Forge 47.4.0, Java 17
- Upstream version: 2.2.3
- Port version: `2.2.3-port.5`
- Source/JAR correspondence: unavailable; no matching upstream 2.2.3 JAR was supplied
- State: port.5 restores the complete Forge global-configuration route and passes a clean production build; user-owned exact-artifact runtime acceptance pending; public binary distribution remains unauthorized

## System matrix

| System | State | Evidence | Boundary |
| --- | --- | --- | --- |
| Build/lifecycle | Complete | ModDevGradle LegacyForge 2.0.107, Gradle 8.14.3, Java 17; clean `build` passes | Five ignored local dependency JARs are required; versions/API contracts are checked and fixed-SHA auditing is opt-in |
| Entrypoints/registries | Complete | One artifact retains all three Mod IDs; Forge registries and lifecycle events initialize in client/server runs | Abyss production disable and High Seas placeholder remain upstream behavior |
| Blocks/items/machines | Ported | Original registration chains compile; recipes, loot, tags, models and item-stack schemas are migrated | Exhaustive manual interaction of every block is not represented as an automated test |
| Capabilities | Ported | Forge `LazyOptional` fluid/energy providers, side rules and invalidation restored | Representative machine transfer gameplay remains a manual acceptance surface |
| Networking | Complete | Seven direction-locked `SimpleChannel` messages; codec and registration tests pass | Tests cover framing/direction/bounds, not adverse real-network latency |
| Sable physics | Runtime pass | Updated formal Sable accepts addon force-group entries; PJ client/server sublevels initialize, reload and save | Full submarine assembly/pressure campaign remains gameplay-level validation |
| Simulated integration | Regression fix under validation | Rope target/refmap contracts pass; port.4 matched the formal port's `ResourceLocation`-keyed diagram force clusters and port.5 retains that correction | Reopen a populated structure diagram with the exact port.5 artifact; steel-cable gameplay remains a manual gate |
| Rendering | Regression fix under validation | Vanilla and Embeddium routes are selected explicitly; port.3 added the addon-owned banner atlas source and port.5 retains it | Confirm the exact port.5 banner visually and repeat the core submarine scene |
| Configuration UI | Regression fix under validation | Port.5 replaces the Forge mod-list placeholder with a scrollable screen bound to all 18 public `SubmarineConfig` values; edits are staged, range-validated and saved only on confirmation | Open the exact port.5 artifact, scroll through all five sections, save one value and confirm Cancel/Escape discard staged edits |
| Copycats | Compatibility pass | Exact 3.0.4 ABI test plus isolated Copycats and Copycats+Embeddium client, and Copycats server bootstrap | PJ production run did not have Copycats installed |
| Embeddium/Oculus | Runtime pass | Exact Embeddium 0.3.31 contracts; PJ world/save/reload with Embeddium+Oculus succeeds | Named-dev Oculus cannot run because Oculus's own SRG refmap targets production names |
| Persistence/shutdown | Runtime pass | PJ saved overworld, Nether, End and all Sable sublevels, returned to title, then shut down cleanly | Save upgrade from an original 1.21 world is not a supported cross-version path |
| Packaging/recovery | Complete | One-click script, dependency presence/contracts, source ZIP, minimal workspace, Git bundle and SHA-256 output manifests | Public compiled publication is blocked by ARR license |

## Upstream behavior intentionally preserved

- `create_abyss` returns early in production; the source and resources remain in
  the artifact but the port does not turn WIP content into a claimed release.
- `create_high_seas` remains a placeholder.
- Decompression chambers and boat support retain their upstream WIP boundaries.
- No feature was disabled, stubbed or replaced with a constant merely to make
  Forge compilation or startup succeed.

## Known risks and follow-up acceptance

- Run an extended gameplay session that assembles and drives a Deep Seas
  submarine, exercises ballast/pressure/sinking, steel cable, mine, propeller,
  electrolyzer and save/reload behavior together.
- Repeat the production visual pass with Copycats installed and with an Oculus
  shader pack enabled.
- Confirm that the exact port.5 artifact opens a populated structure diagram
  without the previous force-key cast and displays the animated Deep Seas
  section banner instead of the missing-texture checkerboard.
- Confirm that the exact port.5 artifact opens Global Settings without leaving
  the Deep Seas configuration flow and that save/cancel behavior matches the
  staged-value contract.
- If a matching upstream 2.2.3 JAR becomes available, perform the deferred
  source/binary correspondence audit without moving the locked baseline first.

These are explicit behavior-coverage boundaries, not hidden compile gaps. The
current checkpoint remains buildable and recoverable while further acceptance
evidence is collected.
