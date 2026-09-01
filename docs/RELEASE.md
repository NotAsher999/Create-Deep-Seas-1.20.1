# Local release and recovery

## One-click verified checkpoint

Run `build-release.bat` from the repository root after the port has been
committed and annotated tag `v2.2.3-port.3` points to that commit. The script
refuses to package if the branch, tag, clean worktree, Java/Gradle version,
dependency hashes, automated-test totals or production-JAR contracts differ
from the recorded checkpoint.

This is a local engineering release only. The script performs no upload and the
ARR license still blocks public compiled redistribution without authorization.

## Outputs

- `dist/create-deep-seas-forge-1.20.1-2.2.3-port.3.jar` — normal production JAR.
- `release/2.2.3-port.3/` — production/sources JARs, exact tracked-source ZIP,
  minimal-workspace ZIP, build log, dependency/checkpoint notes and SHA-256 sums.

The tracked-source ZIP is a `git archive` and cannot build by itself because the
five local build/runtime inputs are deliberately ignored. The minimal workspace
contains those hash-locked inputs, a verified Git bundle with the port branch
and annotated tag, cross-platform ZIP paths, recovery instructions and a
manifest covering every file. Before publishing the ZIP, the script also clones
that bundle into an isolated probe, restores all five dependencies, rechecks
their hashes and requires the restored tagged worktree to remain clean.

Publication uses an isolated staging directory. A failed build or validation
does not replace the previous versioned release or `dist` JAR; final publication
has rollback coverage for both surfaces.
