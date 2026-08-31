# Upstream baseline

## Locked source

- User fork: `https://github.com/NotAsher999/Create-Deep-Seas-1.20.1.git`
- Upstream: `https://github.com/MaxCreateMC/Create-Deep-Seas.git`
- Baseline commit: `37b876cea7e06e6a3ea56fce998aff9c4a0d3984`
- Baseline branch: `main`
- Baseline date: 2026-08-29
- Fork/upstream divergence at lock time: `0 / 0`
- Port branch: `port/forge-1.20.1`

The current source declares Minecraft 1.21.1, NeoForge 21.1.227, Java 21 and
Create Deep Seas 2.2.3. It contains three mod IDs in one artifact:
`create_submarine`, `create_abyss` and `create_high_seas`.

## Binary evidence boundary

No original release JAR was supplied for this port. The repository has no
GitHub release for the current `2.2.3` source and its newest named tag predates
the baseline. Therefore this project can lock source behavior and history, but
must not claim source/JAR byte-for-byte correspondence until a matching original
JAR is supplied and audited.

The four tracked 1.21.1 NeoForge dependency JARs are upstream development
inputs, not release baselines for this mod. Their SHA-256 values at baseline are:

| File | SHA-256 |
| --- | --- |
| `aeronautics-neoforge-1.21.1-1.1.3.jar` | `C2AEA9C86ECCB1D6CC56E1E143098D7A909DD26D3956AD253C69FD15F793FF26` |
| `aeronautics-neoforge-1.21.1-1.3.0.jar` | `482C90E0E6FE72F33FE7ABB079E5FDA581CD660EE53C38C44448A64283E1044C` |
| `simulated-neoforge-1.21.1-1.1.3.jar` | `60684F2B5AF067FE8B43B7E4F126B3C3516BF15E0269A0E3C840ACCE7801D583` |
| `simulated-neoforge-1.21.1-1.3.0.jar` | `9CBFDAF421B450727232D7A2DE5F9E2AB826AC2113A25FB1286BA431A1A8A403` |

## Upstream state at lock time

Upstream `main` and the user fork both point to the locked baseline. Upstream
also has `Experimental`, `Extremely-Experimental`, `cogfly-test` and
`sodium-veil-fix` branches. They are comparison inputs only and are not merged
into the stable port without an explicit behavior-level audit.

Tags `Alpha`, `Alpha2.1.4` and `Beta` point to older commits and do not identify
the current 2.2.3 source.

## License boundary

The repository license is All Rights Reserved. The README explicitly invites
contributors to propose changes, so local port work and a contribution branch
are maintained as such. A compiled public release or redistribution remains a
separate authorization gate and must not be inferred from successful builds.
