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
| Local dependency inputs | Pass | Five JARs are required; versions and API/JAR contracts are checked by default, fixed SHA checks are audit-only, and copied release inputs are manifested |
| Remote dependencies | Pass | Gradle resolution plus explicit Copycats/Embeddium API and bytecode contracts; fixed artifact SHA checks are audit-only |

## Automated validation — 2026-09-01

`gradlew clean build --no-daemon` completed with 18/18 tests in seven suites,
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
- the referenced Simulated `renderArrows` local-variable table identifies
  `clusters` as `Map<ResourceLocation, List<Cluster>>`, and the compiled Deep
  Seas handler captures that key type without a `ForceGroup` cast;
- both production Mixin configs bind `createdeepseas.refmap.json`, every listed
  Mixin class is present, and the rope target maps to SRG `m_82509_`;
- the production manifest registers both Mixin configs, required resources and
  three entrypoint classes exist, obsolete 1.21 data paths are absent, and
  entrypoint bytecode is Java 17.
- the water-occlusion pre-pass remains attached to the third
  `crumblingBufferSource()` call, shifted `AFTER`, which is the Forge 1.20.1
  boundary after block-entity/Flywheel rendering and before translucent terrain.
- the Deep Seas creative section's `create_submarine:banner` sprite has one
  block-atlas source, a readable 162-pixel-wide PNG and 18-pixel animation frames.

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

## Port.2 moving-part regression A/B — 2026-09-01

The first port.2 behavior candidate changed only the Deep Seas water-occlusion
stage and retained the preceding Sable production JAR. Its temporary
port.1-named production artifact had SHA-256
`3B34BC92CB4E19791F8114C5D99D165DFF3BABE9D9E380B8FFF07595A5865AE3`.
The PJ run from 00:34:59 through 00:37:10 entered the existing world, initialized
the client Sable sublevel container, saved all dimensions and sublevels, and
shut down normally. Its 1,510-line log has SHA-256
`237AE2A706E53E49A664E02E644ED5E476CC8F5EDDFF336355E79D50C525621B`
and contains no Mixin, fatal renderer, `shader is null`, or `GL_INVALID` failure.

The user confirmed that the affected scene looked normal in that run and the
previously invisible Create/Flywheel moving parts were visible again. This
single-variable result is the runtime evidence for moving the pre-pass from
`renderLevel` `HEAD` to Sable's renderer-owner boundary. Shader packs were
disabled. A later run with the final port.2 artifact remains required for exact
artifact correspondence; shader-pack-enabled rendering remains a separate
optional-path gate.

Three later remote-session attempts ended immediately after the local client
UDP channel became active. The held Java process returned exit code `0`; there
was no crash report, `hs_err`, Windows Error Reporting event, Mixin failure or
fatal renderer error. The same UDP log line is present in the accepted run, and
the user confirmed that their remote desktop sessions are frequently
disconnected. Those attempts are therefore excluded as externally contaminated
test runs: they are not evidence against either the final port.2 metadata or a
Sable build. Exact-artifact gameplay acceptance is intentionally delegated to
the user rather than converted into a speculative code workaround.

## Exact port.2 PJ runtime and banner finding — 2026-09-01

The final port.2 JAR, SHA-256
`814B35EF3AF2E763B3566A8582B5E214860DF542410BBCE3475170FBABF7DBDD`,
ran in the PJ instance from 12:56 through 13:15. The 1,643-line log has
SHA-256 `1CA0F73FB5C5FE7827DF2CFCADF67056C2049E3F15A777A0A271454A01FADEE1`.
Deep Seas selected the Embeddium water-occlusion hooks; Sable initialized the
server and client sublevel containers; repeated saves completed; Complementary
Reimagined plus Euphoria Patches loaded; and the client shut down normally.
There was no Mixin application failure, null-shader exception, GL fatal error or
new crash report. Flywheel defensively fell back from forced instancing to off
after the shader pack loaded.

The user then reported that the Deep Seas creative-section title rendered as
the black and magenta missing-texture checkerboard. Static audit matched that
observation: the section requested `create_submarine:banner` and its PNG was
present, but no block-atlas source registered that sprite. This is the runtime
finding closed by port.3; visual confirmation of its exact artifact remains the
acceptance gate.

## Formal packaging retry — 2026-09-01

The first tagged packaging attempt stopped before Gradle ran because the local
Windows PowerShell 5.1 process could not resolve the optional `Get-FileHash`
cmdlet. The script reported failure and did not replace the existing `release`
or `dist` outputs. Hashing was then moved to an in-script .NET SHA-256 helper so
the one-click checkpoint no longer depends on module auto-loading. A successful
formal run and its generated manifests are the release acceptance gate.

## Port.3 structure-diagram crash and port.4 correction — 2026-09-01

The user-owned `DeceasedCraft_CB` client crashed at 16:32:19 while rendering a
Simulated structure diagram. The first application frame was Deep Seas'
`DiagramScreenMixin`: it cast a `ResourceLocation` cluster key to Sable's
`ForceGroup`. Ponder and FancyMenu were downstream screen frames, not the source.
The integrated server completed its world and Sable sublevel saves during the
client shutdown, so the log did not indicate save corruption.

The formal Simulated port changed `DiagramScreen.renderArrows` in checkpoint
`c2884e1` from `Map<ForceGroup, List<Cluster>>` to stable
`Map<ResourceLocation, List<Cluster>>` keys. The inherited Deep Seas Mixin still
captured the older generic shape; type erasure allowed compilation and injection,
then the enhanced-for loop emitted the failing runtime cast. Port.4 now addresses
`create_submarine:ballast` directly in the identifier-keyed map. It does not
change Sable, suppress the exception, disable diagram arrows, or special-case
the affected save.

The first targeted test attempt was rejected by `compileTestJava` because the
new test omitted the static `assertFalse` import; production compilation had
already succeeded. After correcting that test-only error, the targeted contract
test and the full clean 18-test build passed. Exact-artifact diagram reopening
remains the user runtime gate.

## Runtime defects found and closed

### Creative section banner atlas

The exact port.2 PJ run rendered the Deep Seas section title as the black and
magenta missing-texture checkerboard. The section JSON and animated PNG matched
upstream, but neither upstream nor the port contributed the required
`minecraft:blocks` atlas source for `create_submarine:banner`. Port.3 adds one
addon-owned `single` source and locks the complete section-to-atlas-to-PNG
contract without modifying Simulated or copying another addon's resources.

### Simulated diagram cluster-key ABI

The formal 1.20.1 Simulated port uses registry identifiers as diagram force-map
keys so serialized/configured force-group IDs remain stable. Deep Seas now looks
up and replaces only `create_submarine:ballast` by that identifier. The build
checks both the dependency local-variable generic signature and the compiled
handler's casts, preventing a source-compatible generic mismatch from reaching
another production client.

### Sable force-group verifier

Sable previously assumed the registry contained exactly its seven built-ins,
which rejected legitimate addon entries. The formal Sable workspace now verifies
all built-ins by identity while allowing additional registered force groups.
Sable's clean build passed 51/51 tests in 24 suites; its production JAR hash is
`F139545B947E3E429BDD5BB3D08AD3C8B42A78266AF25DA13C19813AD6F9C042`.

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

## Packaging defect found and closed

An initial packaging rehearsal from checkpoint `39e1eed` was rejected during
independent recovery testing: a bundle clone correctly omitted the ignored
`localDeps` directory, while the generated instructions tried to copy the five
locked JARs into that directory before creating it. The release script now
creates the directory explicitly and, before publication, performs the same
bundle clone, dependency copy/hash verification, annotated-tag check and clean
worktree check described by `RESTORE.md`.

## Acceptance boundary

The recorded production run proves startup, world entry, Sable sublevel
lifecycle, resource reload, renderer coexistence, save and clean shutdown in the
full PJ environment. It does not by itself prove every submarine block's
gameplay behavior, a shader-pack-enabled render path, or Copycats behavior in
that exact PJ run. Port.4's structure-diagram correction is statically and
automatically verified but still awaits an exact-artifact reopen by the user.
Those are retained as explicit manual follow-up gates rather than being reported
as completed tests.
