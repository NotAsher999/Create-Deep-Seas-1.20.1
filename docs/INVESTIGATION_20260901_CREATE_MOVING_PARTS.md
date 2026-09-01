# Create/Flywheel moving-part rendering regression — 2026-09-01

## Report and runtime baseline

The user reported that animated Create parts had become invisible again in the
PJ production instance after the Create Deep Seas candidate was installed. The
captured run is `logs/latest.log` from 2026-08-31 23:41:25–23:49:29, SHA-256
`8C387969881A095AECEE3B9BB90E76DAF734F11FEA8985E76D8B6DD3472A9104`.
It entered the existing world, saved, and exited normally; there is no render
exception that can identify a failed draw call. Oculus reported shader packs
disabled and Flywheel was explicitly configured to use `flywheel:instancing`.

This is a visual regression, not a crash repair. A clean exit is not accepted
as proof that the affected pixels rendered.

## Previously repaired failure modes checked first

Three earlier defects can produce a similar visible result, so their deployed
contracts were checked before changing code:

1. The formal Simulated/Aeronautics bundle still contains the live
   `ChunkRenderTypeSet` interpretation and stable vanilla layer ordering. The
   current log's overwrite-conflict warning shows that Aeronautics, rather than
   Embeddium, still owns the iterator as required by the previously validated
   repair. The four relevant installed classes match the formal bundle.
2. Sable's Oculus `ExtendedShader` program-cache recovery remains present and
   `config/sable-client.toml` keeps `iris_program_cache_recovery = true`.
3. Voxy Compatibility Patch 1.0.1 remains installed. Its established boundary
   restores the block atlas on texture unit 0 after Voxy's opaque pass.

Sable's later removal of bounded Flywheel diagnostic classes was also audited.
Those deleted classes only recorded state; the embedding, shader ABI, visual
creation, matrix, lighting, and program-recovery behavior remains in the
production classes. Restoring the probes as behavior would therefore be an
incorrect fix.

No Simulated file was modified during this investigation.

## Directly matching migration-stage defect

The Deep Seas upstream wrapper invokes the Sable water-occlusion renderer at
`renderLevel` `HEAD`. Sable's own renderer integration, however, invokes that
same depth-mask pre-pass at a
specific point in `LevelRenderer.renderLevel`: after entity and block-entity
rendering (including Create/Flywheel visuals), and immediately before the
destruction overlay is flushed and translucent terrain begins. It identifies
that point as the third call (`ordinal = 2`) to
`RenderBuffers.crumblingBufferSource()`, shifted `AFTER`.

Forge 1.20.1 still has the same three-call structure and the same semantic
boundary. The initial Create Deep Seas port retained the addon's `HEAD` hook
while locally reconstructing Sable's excluded water renderer. That was not an
API-required 1.20.1 translation and left the restored renderer attached at a
different stage from its owning Sable integration. At `HEAD`, its framebuffer,
shader/program, culling, depth, and texture transitions run before opaque
terrain and Flywheel visuals; at Sable's boundary they run after those moving
parts have rendered.

The repair follows the Sable renderer-owner stage rather than disabling water
occlusion, Flywheel, Voxy, Veil, or custom render layers. It does not add a
Create block allow-list or a renderer-specific fallback. Because the captured
log contains no failed draw call, runtime visual reproduction remains the
final attribution gate.

## Candidate and verification

- Changed `LevelRendererMixin` from `renderLevel` `HEAD` to the existing Forge
  1.20.1 `crumblingBufferSource()` ordinal-2/AFTER boundary.
- Added `WaterOcclusionRenderStageContractTest` to lock the target, ordinal, and
  shift so a future API migration cannot silently move the pre-pass again.
- `gradlew clean build --no-daemon`: passed.
- Unit/contract result: 7 suites, 16 tests, 0 failures, 0 errors, 0 skipped.
- Candidate production JAR SHA-256:
  `3B34BC92CB4E19791F8114C5D99D165DFF3BABE9D9E380B8FFF07595A5865AE3`.
- Installed into PJ with byte-identical hash.
- Previous PJ JAR SHA-256:
  `849BC08264535D55BF3DFBA8703CCCE3FF2BD6DD413AE7D40D7498FE036CC889`.
- Recoverable backup:
  `artifacts/pj-deploy-backups/20260901-0016-water-occlusion-stage/`.

## Runtime acceptance gate

An isolated PJ run with the stage candidate and the preceding Sable production
JAR started at 00:34:59 and exited at 00:37:10. It entered the existing world,
created the client Sable sublevel container, saved all three dimensions and
their sublevels, and shut down normally. The 1,510-line log has SHA-256
`237AE2A706E53E49A664E02E644ED5E476CC8F5EDDFF336355E79D50C525621B` and
contains no `MixinApplyError`, `InvalidMixin`, `InjectionError`, `FATAL`,
`ReportedException`, `shader is null`, or `GL_INVALID` entry. Oculus shader
packs were disabled. The user then confirmed that everything in the affected
scene looked normal in this 00:37 run, including the previously missing moving
parts. Because this run changed the Deep Seas stage while retaining the
preceding Sable production JAR, it is the bounded A/B evidence that attributes
the visible regression to the misplaced water pre-pass rather than to the
separately discovered Sable selector defect. It proves the affected no-shader
scene plus startup/world/save/exit compatibility; it does not by itself prove
the resource-reload or shader-pack-enabled paths.

Later automated attempts made through a remote desktop session terminated with
Java exit code `0` after the local UDP channel became active. They produced no
Minecraft crash report, JVM fatal-error log, Mixin failure or renderer fatal.
Because the user confirmed that the remote session is frequently disconnected,
these runs are classified as externally contaminated and are not used to
attribute a defect to Deep Seas, Sable or the UDP line that happened to be
flushed last. No network or Sable workaround was added for this signal.

The direct moving-part regression is accepted for the no-shader PJ scene. The
candidate remains under broader runtime validation until the following
boundaries are also exercised:

- the user confirms that the exact final port.2 artifact reproduces the
  accepted moving-part result;
- the same scene still renders after pause/resume and `F3+T` resource reload;
- Deep Seas water occlusion and translucent water remain functional;
- the no-shader path remains clean, followed by a shader-enabled check if that
  configuration is used;
- save, exit, and reopen do not reintroduce the regression.

If the visual symptom remains, the next step is a bounded validation-only probe
at the water pre-pass return and Flywheel draw boundary (program, framebuffer,
active texture, unit-0 atlas, and draw count). The production fix must not grow
into blanket GL-state forcing without that evidence.
