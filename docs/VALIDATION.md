# Validation record

This record separates source audit, automated tests, isolated launch profiles
and the production PJ run. A passing bootstrap is not used as proof of every
gameplay branch.

## Locked baseline and inputs

| Check | Result | Evidence/boundary |
| --- | --- | --- |
| Fork/upstream baseline | Pass | Both started at `37b876c`; latest remote check still found no newer `main` commit |
| Original 2.2.3 JAR | Missing input | No matching release JAR exists in supplied material; binary correspondence is not claimed |
| Java/Forge target | Pass | Java 17 class major 61; Forge 47.4.0 metadata and Gradle toolchain |
| Local dependency inputs | Pass | Five JARs are SHA-256 locked in `gradle.properties`, dependency verification and the release script |
| Remote dependencies | Pass | Gradle verification metadata plus explicit Copycats/Embeddium contract hashes |

## Automated validation — 2026-08-31

`gradlew clean build --no-daemon` completed with 15/15 tests in six suites,
zero failures, errors or skips. The build also ran `verifyProductionJar` against
the final `build/libs` reobfuscated artifact.

Coverage includes:

- all bundled JSON parses, all 29 recipes use the Forge 1.20.1 layout, and the
  upstream color/tag semantics are retained;
- seven network messages keep exact IDs, directions, encoders/decoders and
  defensive bounds;
- Copycats 3.0.4 and Embeddium 0.3.31 exact artifacts expose the bytecode APIs
  used by their conditional Mixins;
- the locked Simulated rope method still invokes
  `Vec3.closerThan(Position,double)` and the external `remap=false` Mixin
  explicitly remaps that Minecraft call;
- the locked Sable chunked renderer still returns `int`, while both pocket-fog
  handlers use `CallbackInfoReturnable<Integer>`;
- both production Mixin configs bind `createdeepseas.refmap.json`, every listed
  Mixin class is present, and the rope target maps to SRG `m_82509_`;
- the production manifest registers both Mixin configs, required resources and
  three entrypoint classes exist, obsolete 1.21 data paths are absent, and
  entrypoint bytecode is Java 17.

## Isolated runtime matrix

| Profile | Result | What it establishes |
| --- | --- | --- |
| Base client | Pass | Main menu, vanilla renderer route, Veil/Deep Seas shader load and Ponder registration |
| Copycats client | Pass | Conditional wrench Mixin loads on Mixin 0.8.5 without interface-injector failure |
| Embeddium client | Pass | Embeddium renderer route and translated 0.3.31 Mixins load |
| Copycats + Embeddium client | Pass | Both optional paths coexist through main menu |
| Base dedicated server | Pass | Common registration/network/capability code is client-clean |
| Copycats dedicated server | Pass | Server reaches `Done`; Sable reports seven built-ins and nine total force groups |
| Oculus named-dev client | Not applicable | Oculus 1.8's production SRG refmap fails against a named development runtime before Deep Seas; production PJ is the valid gate |

## PJ production runtime — 2026-08-31

Installed Deep Seas candidate:

- file: `create-deep-seas-forge-1.20.1-2.2.3-port.1.jar`
- SHA-256: `849BC08264535D55BF3DFBA8703CCCE3FF2BD6DD413AE7D40D7498FE036CC889`
- size: 1,864,035 bytes

The final log is
`J:\YZ\PJ\.minecraft\versions\1.20.1-Forge\logs\latest.log`, 1,671 lines,
SHA-256 `804BFA9DD25BFAEA6910C181FCEBC8C61EE7760BE4C015DA4BF19B668047DDB4`.

Observed sequence:

1. Production Forge discovered the artifact and its manifest Mixin configs.
2. The plugin selected the Embeddium route; Sable verified seven built-in and
   nine total force groups; Veil/Pinwheel loaded 22 Deep Seas shaders.
3. The integrated server and client created their Sable sublevel containers;
   `DeepDarkVAN` entered `新的世界 (5)`.
4. F3+T completed a full resource reload. The same 22 shaders reloaded, Voxy
   rebuilt its render pipeline, and Sable plot chunks were accepted again.
5. The game paused/saved and resumed. Final exit saved overworld, Nether, End
   and all corresponding Sable sublevels; every dimension reported saved.
6. The client returned to the title screen and then shut down normally.

The run contains no `MixinApplyError`, `InvalidMixinException`, injection
failure, `ReportedException`, fatal Deep Seas error or `shader is null` crash
signature. Authentication, optional missing-content and other-mod model warnings
remain unrelated environmental messages and were not suppressed by this port.

## Runtime defects found and closed

### Sable force-group verifier

Sable previously assumed the registry contained exactly its seven built-ins,
which rejected legitimate addon entries. The formal Sable workspace now verifies
all built-ins by identity while allowing additional registered force groups.
Sable's clean build passed 51/51 tests in 24 suites; its production JAR hash is
`6AFA08BE8FCFFFEDDEF6A955CBD7214C245F4DA72F7DA3AA7C1619F78A573D45`.

### Production Mixin refmap

The first PJ production candidate did not name the generated refmap in either
Mixin config. Named development launches hid this because ModDev supplied the
mapping context. Both configs now bind the same refmap, and the production-JAR
gate validates that binding and every configured class.

### External `remap=false` target

The Simulated target class is external and deliberately unremapped, but the
nested Minecraft `Vec3.closerThan` invocation still requires SRG remapping on
Forge. `@At(remap=true)` restores that two-level contract. A bytecode test locks
the dependency method and the production refmap locks the exact SRG output.

### Sable integer callback

`VanillaChunkedSubLevelRenderData.renderChunkedSubLevel` returns `int` on the
formal 1.20.1 Sable port. Its HEAD/TAIL injections now use
`CallbackInfoReturnable<Integer>` instead of `CallbackInfo`; this matches Mixin
semantics and removes the downstream uninitialized-render-data failure.

## Acceptance boundary

The recorded production run proves startup, world entry, Sable sublevel
lifecycle, resource reload, renderer coexistence, save and clean shutdown in the
full PJ environment. It does not by itself prove every submarine block's
gameplay behavior, a shader-pack-enabled render path, or Copycats behavior in
that exact PJ run. Those are retained as explicit manual follow-up gates rather
than being reported as completed tests.
