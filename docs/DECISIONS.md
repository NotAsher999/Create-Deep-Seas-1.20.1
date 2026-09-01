# Port decisions

## D-001: source baseline

Use `37b876cea7e06e6a3ea56fce998aff9c4a0d3984` as the only initial behavior
baseline. The user fork and upstream `main` matched when work started. Do not
start from an older Alpha/Beta tag or merge experimental branches mechanically.

## D-002: missing original JAR

The source baseline is usable, but no matching original 2.2.3 JAR was supplied.
Do not describe resource/class/bytecode correspondence as verified. If a JAR is
later provided, audit it against the locked commit before changing the baseline.

## D-003: one artifact, three Mod IDs

Preserve `create_submarine`, `create_abyss` and `create_high_seas` in one
artifact. Do not split or delete modules merely to reduce compile failures.
Create Abyss remains disabled in production because that is explicit upstream
behavior; its source still compiles and its development paths remain testable.

## D-004: vertical dependency closure

Port each system through registration, data, server logic, networking,
persistence and client presentation. A compiling surface class does not make a
machine, entity, worldgen system or Mixin complete.

## D-005: loader semantics

Translate NeoForge 1.21 semantics into explicit Forge 47 behavior. This includes
event-bus selection, capabilities, networking, registry lifecycles, config
events, client extensions and production Mixin discovery. Package renaming by
itself is not accepted as a migration.

## D-006: formal local dependencies

Compile against the named development artifacts produced by the existing formal
Sable/Simulated/Aeronautics/Veil workspaces. Run production tests only with
their production distributions. Never modify the Sable or Simulated reference
workspaces as part of this port.

## D-007: renderer compatibility

The 1.21 Sodium Mixins must be compared to Forge 1.20.1 Embeddium/Oculus and Veil
implementations method by method. Do not add global null guards, disable water
occlusion or branch on the PJ mod list merely to silence a crash.

## D-008: license and distribution

Treat this as a contribution port under the README's contributor invitation.
The All Rights Reserved license means that a compiled public release,
redistribution or marketplace publication is a separate authorization gate.
Local builds and source checkpoints do not imply release permission.

## D-009: addon-aware Sable registry verification

Deep Seas adds two force-group entries to Sable's registry. Sable's previous
post-registration verifier required the registry size to equal the seven
built-ins and therefore rejected any legitimate addon. The formal Sable port now
checks that every built-in exists with the expected identity while allowing
additional entries. This preserves corruption detection without encoding a
closed-world assumption into a public addon registry.

## D-010: renderer selection belongs at the Mixin plugin boundary

Upstream Sodium hooks cannot be loaded alongside Forge 1.20.1 vanilla renderer
targets. A Mixin config plugin chooses exactly one path: vanilla hooks when
Embeddium is absent, translated Embeddium 0.3.31 hooks when present, and the
Copycats wrench Mixin only when Copycats is installed. This prevents optional
classloading failures without changing render behavior inside shared code.

## D-011: external targets and Minecraft targets remap independently

`RopeStrandHolderBehaviorMixin` targets a named Simulated development class with
`@Mixin(remap=false)`. Its nested `Vec3.closerThan` injection point is a
Minecraft method and must still set `@At(remap=true)` for a production Forge
JAR. Treating the outer setting as applicable to every nested target caused the
PJ-only redirect failure and is now guarded by dependency bytecode and refmap
tests.

## D-012: production refmap is an artifact contract

Both Mixin configs explicitly name `createdeepseas.refmap.json`. The build checks
the final reobfuscated JAR—not the named dev JAR—for the manifest, configs,
refmap, referenced Mixin classes and the critical rope SRG mapping. Development
launch arguments are not accepted as evidence that production discovery works.

## D-013: 1.20 renderer callback return types are authoritative

The formal Sable 1.20 renderer returns an integer section count, so its injected
callbacks use `CallbackInfoReturnable<Integer>`. Callback shapes are derived
from the actual target descriptor, not from a similarly named newer-source
method or a first crash-frame workaround.

## D-014: local engineering release only

The one-click script may produce a local production JAR and a recoverable
engineering checkpoint. It does not upload, publish or grant redistribution
rights. The ordinary source archive is intentionally incomplete without ignored
local inputs; the minimal-workspace archive carries exact JAR hashes and a Git
bundle so another maintainer can reproduce the checkpoint without chat history.
