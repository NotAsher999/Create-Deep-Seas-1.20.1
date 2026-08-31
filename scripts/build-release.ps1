[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
Add-Type -AssemblyName System.IO.Compression.FileSystem

$projectDir = Split-Path -Parent $PSScriptRoot
$gradlePropertiesPath = Join-Path $projectDir 'gradle.properties'
$gradleWrapperPath = Join-Path $projectDir 'gradlew.bat'
$expectedBranch = 'port/forge-1.20.1'
$expectedTests = 15
$expectedSuites = 6

$requiredLocalFiles = [ordered]@{
    'localDeps\sable-forge-1.20.1-2.0.5-port.1-dev.jar' =
        '0A23C66EF70AB04DAB2DF03BB5150AB6576561F446A7CED709FD819A30D3E216'
    'localDeps\simulated-forge-1.20.1-1.3.1-port.1-dev.jar' =
        'B9E75E12C3928D6A9F3BD16E5249A0AD74FAB239538F76BAD4DE4A58F9915DB8'
    'localDeps\aeronautics-forge-1.20.1-1.3.1-port.1-dev.jar' =
        'BD404E06850282D908A73E32030C251284363B182CA93579B3A2241A992245DA'
    'localDeps\Veil-forge-1.20.1-1.0.0.296-dev.jar' =
        '24717637D0B20B2FDE9A25B7456E05FD8F1035FF37000E7BD095742B7DA41E19'
    'localDeps\oculus-mc1.20.1-1.8.0.jar' =
        '0945DF0CBA0F62B3901DD80C3268E5311B770ECE78C78037A45DB12AC0425FEF'
}

function Read-GradleProperty {
    param([Parameter(Mandatory = $true)][string]$Name)

    $pattern = '^' + [regex]::Escape($Name) + '=(.+)$'
    $match = Select-String -LiteralPath $gradlePropertiesPath -Pattern $pattern |
        Select-Object -First 1
    if ($null -eq $match) { throw "Missing Gradle property: $Name" }
    return $match.Matches[0].Groups[1].Value.Trim()
}

function Assert-StrictChildPath {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$ParentPath
    )

    $fullPath = [System.IO.Path]::GetFullPath($Path)
    $fullParent = [System.IO.Path]::GetFullPath($ParentPath).TrimEnd(
        [System.IO.Path]::DirectorySeparatorChar,
        [System.IO.Path]::AltDirectorySeparatorChar)
    $prefix = $fullParent + [System.IO.Path]::DirectorySeparatorChar
    if (-not $fullPath.StartsWith($prefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Unsafe path outside expected parent: $fullPath"
    }
    return $fullPath
}

function Reset-StrictChildDirectory {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$ParentPath
    )

    $fullPath = Assert-StrictChildPath -Path $Path -ParentPath $ParentPath
    if (Test-Path -LiteralPath $fullPath) {
        Remove-Item -LiteralPath $fullPath -Recurse -Force
    }
    [System.IO.Directory]::CreateDirectory($fullPath) | Out-Null
    return $fullPath
}

function Assert-FileHash {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$ExpectedSha256,
        [Parameter(Mandatory = $true)][string]$Description
    )

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "Missing required file: $Description ($Path)"
    }
    $actual = (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash
    if ($actual -ne $ExpectedSha256) {
        throw "SHA-256 mismatch for $Description. Expected $ExpectedSha256, found $actual."
    }
}

function Assert-LocalInputs {
    foreach ($entry in $requiredLocalFiles.GetEnumerator()) {
        Assert-FileHash -Path (Join-Path $projectDir $entry.Key) `
            -ExpectedSha256 $entry.Value -Description $entry.Key
    }
}

function Invoke-GitText {
    param(
        [Parameter(Mandatory = $true)][string[]]$Arguments,
        [string]$WorkingDirectory = $projectDir
    )

    $oldPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $output = & git.exe -C $WorkingDirectory @Arguments 2>&1
        $exitCode = $LASTEXITCODE
    }
    finally { $ErrorActionPreference = $oldPreference }
    if ($exitCode -ne 0) {
        throw "Git command failed: git $($Arguments -join ' ')`n$($output | Out-String)"
    }
    return ($output | Out-String).Trim()
}

function Assert-GitCheckpoint {
    param(
        [Parameter(Mandatory = $true)][string]$ExpectedHead,
        [Parameter(Mandatory = $true)][string]$ExpectedTag,
        [Parameter(Mandatory = $true)][string]$ExpectedTagObject
    )

    $branch = Invoke-GitText -Arguments @('symbolic-ref', '--quiet', '--short', 'HEAD')
    $head = Invoke-GitText -Arguments @('rev-parse', 'HEAD')
    $tagObject = Invoke-GitText -Arguments @('rev-parse', '--verify', "refs/tags/$ExpectedTag")
    $tagCommit = Invoke-GitText -Arguments @('rev-parse', '--verify', "refs/tags/$ExpectedTag^{commit}")
    $status = Invoke-GitText -Arguments @('status', '--porcelain=v1', '--untracked-files=all')
    if ($branch -ne $expectedBranch -or $head -ne $ExpectedHead -or
        $tagObject -ne $ExpectedTagObject -or $tagCommit -ne $ExpectedHead) {
        throw 'Git checkpoint changed during packaging.'
    }
    if (-not [string]::IsNullOrWhiteSpace($status)) {
        throw "The Git worktree is not clean:`n$status"
    }
}

function Invoke-NativeBuild {
    param([Parameter(Mandatory = $true)][string]$LogPath)

    $oldPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        & $gradleWrapperPath clean build --no-daemon --stacktrace 2>&1 |
            Tee-Object -LiteralPath $LogPath
        $exitCode = $LASTEXITCODE
    }
    finally { $ErrorActionPreference = $oldPreference }
    if ($exitCode -ne 0) { throw "Gradle release build failed with exit code $exitCode." }
}

function Get-GradleVersionText {
    $oldPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $output = & $gradleWrapperPath --version --no-daemon 2>&1
        $exitCode = $LASTEXITCODE
    }
    finally { $ErrorActionPreference = $oldPreference }
    if ($exitCode -ne 0) { throw 'Gradle wrapper version check failed.' }
    return ($output | Out-String)
}

function Get-TestTotals {
    $resultDir = Join-Path $projectDir 'build\test-results\test'
    $files = @(Get-ChildItem -LiteralPath $resultDir -Filter 'TEST-*.xml' -File)
    $totals = [ordered]@{ suites = $files.Count; tests = 0; failures = 0; errors = 0; skipped = 0 }
    foreach ($file in $files) {
        [xml]$document = Get-Content -LiteralPath $file.FullName
        $suite = $document.testsuite
        $totals.tests += [int]$suite.tests
        $totals.failures += [int]$suite.failures
        $totals.errors += [int]$suite.errors
        $totals.skipped += [int]$suite.skipped
    }
    return $totals
}

function Read-ZipEntryText {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$EntryName
    )

    $archive = [System.IO.Compression.ZipFile]::OpenRead($Path)
    try {
        $entry = $archive.GetEntry($EntryName)
        if ($null -eq $entry) { throw "Archive $Path is missing $EntryName" }
        $reader = New-Object System.IO.StreamReader($entry.Open())
        try { return $reader.ReadToEnd() }
        finally { $reader.Dispose() }
    }
    finally { $archive.Dispose() }
}

function New-PortableZipFromDirectory {
    param(
        [Parameter(Mandatory = $true)][string]$SourceDirectory,
        [Parameter(Mandatory = $true)][string]$DestinationPath
    )

    $sourcePrefix = [System.IO.Path]::GetFullPath($SourceDirectory).TrimEnd(
        [System.IO.Path]::DirectorySeparatorChar,
        [System.IO.Path]::AltDirectorySeparatorChar) + [System.IO.Path]::DirectorySeparatorChar
    $destination = [System.IO.Path]::GetFullPath($DestinationPath)
    if ($destination.StartsWith($sourcePrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw 'ZIP destination cannot be inside its source directory.'
    }
    if (Test-Path -LiteralPath $destination) {
        throw "Refusing to overwrite staged ZIP: $destination"
    }

    $archive = [System.IO.Compression.ZipFile]::Open(
        $destination, [System.IO.Compression.ZipArchiveMode]::Create)
    try {
        Get-ChildItem -LiteralPath $SourceDirectory -Recurse -File |
            Sort-Object FullName |
            ForEach-Object {
                $fullPath = [System.IO.Path]::GetFullPath($_.FullName)
                if (-not $fullPath.StartsWith($sourcePrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
                    throw "ZIP source escaped staging directory: $fullPath"
                }
                $entryName = $fullPath.Substring($sourcePrefix.Length).Replace('\', '/')
                [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
                    $archive, $fullPath, $entryName,
                    [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null
            }
    }
    finally { $archive.Dispose() }
}

function Assert-ZipEntries {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string[]]$RequiredEntries
    )

    $archive = [System.IO.Compression.ZipFile]::OpenRead($Path)
    try {
        $names = @($archive.Entries | ForEach-Object { $_.FullName })
        $bad = $names | Where-Object { $_.Contains('\') } | Select-Object -First 1
        if ($null -ne $bad) { throw "Archive $Path contains non-portable entry $bad" }
        foreach ($required in $RequiredEntries) {
            if ($names -notcontains $required) {
                throw "Archive $Path is missing required entry $required"
            }
        }
    }
    finally { $archive.Dispose() }
}

function Write-DirectoryManifest {
    param(
        [Parameter(Mandatory = $true)][string]$Root,
        [Parameter(Mandatory = $true)][string]$ManifestPath
    )

    $rootPrefix = [System.IO.Path]::GetFullPath($Root).TrimEnd(
        [System.IO.Path]::DirectorySeparatorChar,
        [System.IO.Path]::AltDirectorySeparatorChar) + [System.IO.Path]::DirectorySeparatorChar
    $manifestFull = [System.IO.Path]::GetFullPath($ManifestPath)
    $lines = @()
    Get-ChildItem -LiteralPath $Root -Recurse -File | Sort-Object FullName | ForEach-Object {
        $full = [System.IO.Path]::GetFullPath($_.FullName)
        if ($full -ne $manifestFull) {
            $relative = $full.Substring($rootPrefix.Length).Replace('\', '/')
            $hash = (Get-FileHash -LiteralPath $full -Algorithm SHA256).Hash
            $lines += "$hash  $relative"
        }
    }
    $lines | Set-Content -LiteralPath $ManifestPath -Encoding UTF8
}

function Assert-ZipManifest {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$ManifestEntry
    )

    $archive = [System.IO.Compression.ZipFile]::OpenRead($Path)
    try {
        $entry = $archive.GetEntry($ManifestEntry)
        if ($null -eq $entry) { throw "Missing ZIP manifest $ManifestEntry" }
        $reader = New-Object System.IO.StreamReader($entry.Open())
        try { $lines = @($reader.ReadToEnd() -split "`r?`n" | Where-Object { $_ }) }
        finally { $reader.Dispose() }

        foreach ($line in $lines) {
            if ($line -notmatch '^([0-9A-Fa-f]{64})  (.+)$') {
                throw "Malformed workspace manifest line: $line"
            }
            $expected = $Matches[1].ToUpperInvariant()
            $name = $Matches[2]
            $fileEntry = $archive.GetEntry($name)
            if ($null -eq $fileEntry) { throw "Manifest entry is missing from ZIP: $name" }
            $stream = $fileEntry.Open()
            try {
                $sha = [System.Security.Cryptography.SHA256]::Create()
                try {
                    $actual = (($sha.ComputeHash($stream) | ForEach-Object { $_.ToString('x2') }) -join '').ToUpperInvariant()
                }
                finally { $sha.Dispose() }
            }
            finally { $stream.Dispose() }
            if ($actual -ne $expected) { throw "Workspace ZIP hash mismatch: $name" }
        }

        $fileCount = @($archive.Entries | Where-Object { -not $_.FullName.EndsWith('/') }).Count
        if ($fileCount -ne ($lines.Count + 1)) {
            throw 'Workspace ZIP manifest does not cover every file.'
        }
    }
    finally { $archive.Dispose() }
}

function Write-Sha256Sums {
    param(
        [Parameter(Mandatory = $true)][string]$Directory,
        [Parameter(Mandatory = $true)][string]$OutputPath
    )

    $outputFull = [System.IO.Path]::GetFullPath($OutputPath)
    $lines = @()
    Get-ChildItem -LiteralPath $Directory -File | Sort-Object Name | ForEach-Object {
        if ([System.IO.Path]::GetFullPath($_.FullName) -ne $outputFull) {
            $lines += "$((Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash)  $($_.Name)"
        }
    }
    $lines | Set-Content -LiteralPath $OutputPath -Encoding UTF8
}

function Assert-Sha256Sums {
    param(
        [Parameter(Mandatory = $true)][string]$Directory,
        [Parameter(Mandatory = $true)][string]$SumsPath
    )

    foreach ($line in Get-Content -LiteralPath $SumsPath) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        if ($line -notmatch '^([0-9A-Fa-f]{64})  (.+)$') {
            throw "Malformed SHA256SUMS line: $line"
        }
        Assert-FileHash -Path (Join-Path $Directory $Matches[2]) `
            -ExpectedSha256 $Matches[1].ToUpperInvariant() -Description $Matches[2]
    }
}

if (-not (Test-Path -LiteralPath $gradleWrapperPath -PathType Leaf)) {
    throw "Missing Gradle wrapper: $gradleWrapperPath"
}
if (-not (Test-Path -LiteralPath (Join-Path $projectDir 'gradle\verification-metadata.xml') -PathType Leaf)) {
    throw 'Missing Gradle dependency verification metadata.'
}

$modVersion = Read-GradleProperty -Name 'mod_version'
if ($modVersion -notmatch '^[0-9A-Za-z][0-9A-Za-z._-]*$') {
    throw "Unsafe mod version: $modVersion"
}
$tagName = "v$modVersion"
$mainJarName = "create-deep-seas-forge-1.20.1-$modVersion.jar"
$sourcesJarName = "create-deep-seas-forge-1.20.1-$modVersion-sources.jar"
$sourceZipName = "create-deep-seas-forge-1.20.1-$modVersion-source.zip"
$workspaceZipName = "create-deep-seas-forge-1.20.1-$modVersion-minimal-workspace.zip"
$bundleName = "create-deep-seas-forge-1.20.1-$modVersion-checkpoint.bundle"

$branch = Invoke-GitText -Arguments @('symbolic-ref', '--quiet', '--short', 'HEAD')
if ($branch -ne $expectedBranch) { throw "Release must run on $expectedBranch, found $branch." }
$headCommit = Invoke-GitText -Arguments @('rev-parse', 'HEAD')
$tagObject = Invoke-GitText -Arguments @('rev-parse', '--verify', "refs/tags/$tagName")
$tagCommit = Invoke-GitText -Arguments @('rev-parse', '--verify', "refs/tags/$tagName^{commit}")
$tagType = Invoke-GitText -Arguments @('cat-file', '-t', "refs/tags/$tagName")
if ($tagType -ne 'tag' -or $tagCommit -ne $headCommit) {
    throw "Annotated tag $tagName must point to HEAD $headCommit."
}
Assert-GitCheckpoint -ExpectedHead $headCommit -ExpectedTag $tagName -ExpectedTagObject $tagObject
Assert-LocalInputs

$gradleVersion = Get-GradleVersionText
if ($gradleVersion -notmatch '(?m)^Gradle 8\.14\.3\r?$' -or
    $gradleVersion -notmatch '(?m)^Launcher JVM:\s+17\.') {
    throw "Release requires Gradle 8.14.3 on a Java 17 launcher.`n$gradleVersion"
}

$releaseRoot = Join-Path $projectDir 'release'
$distRoot = Join-Path $projectDir 'dist'
[System.IO.Directory]::CreateDirectory($releaseRoot) | Out-Null
[System.IO.Directory]::CreateDirectory($distRoot) | Out-Null
$runId = [guid]::NewGuid().ToString('N')
$stagingRoot = Reset-StrictChildDirectory `
    -Path (Join-Path $releaseRoot ".staging-$modVersion-$runId") -ParentPath $releaseRoot
$workspaceStage = Join-Path $stagingRoot 'workspace'
[System.IO.Directory]::CreateDirectory($workspaceStage) | Out-Null
$buildLogPath = Join-Path $stagingRoot 'BUILD.log'

try {
    Write-Host "Building $modVersion from $headCommit..."
    Invoke-NativeBuild -LogPath $buildLogPath

    $testTotals = Get-TestTotals
    if ($testTotals.suites -ne $expectedSuites -or $testTotals.tests -ne $expectedTests -or
        $testTotals.failures -ne 0 -or $testTotals.errors -ne 0 -or $testTotals.skipped -ne 0) {
        throw "Unexpected tests: $($testTotals | ConvertTo-Json -Compress)"
    }

    $builtMainJar = Join-Path $projectDir "build\libs\$mainJarName"
    $builtSourcesJar = Join-Path $projectDir "build\libs\$sourcesJarName"
    if (-not (Test-Path -LiteralPath $builtMainJar -PathType Leaf) -or
        -not (Test-Path -LiteralPath $builtSourcesJar -PathType Leaf)) {
        throw 'Gradle did not produce the expected JARs.'
    }

    $modsToml = Read-ZipEntryText -Path $builtMainJar -EntryName 'META-INF/mods.toml'
    foreach ($requiredText in @(
        "version=`"$modVersion`"",
        'versionRange="[1.20.1]"',
        'versionRange="[47.4.0,48)"',
        'modId="create_submarine"',
        'modId="create_abyss"',
        'modId="create_high_seas"',
        'modId="sable"',
        'modId="simulated"',
        'modId="aeronautics"')) {
        if (-not $modsToml.Contains($requiredText)) {
            throw "Release JAR metadata is missing: $requiredText"
        }
    }
    $manifest = Read-ZipEntryText -Path $builtMainJar -EntryName 'META-INF/MANIFEST.MF'
    if (-not $manifest.Contains('MixinConfigs: create_submarine.mixins.json,create_abyss.mixins.json')) {
        throw 'Release JAR manifest does not register both Mixin configs.'
    }
    Assert-ZipEntries -Path $builtMainJar -RequiredEntries @(
        'createdeepseas.refmap.json',
        'create_submarine.mixins.json',
        'create_abyss.mixins.json',
        'com/maxenonyme/createsubmarine/submarine/mixin/CreateSubmarineMixinPlugin.class',
        'assets/create_submarine/pinwheel/shader_modifiers/minecraft/shaders/core/rendertype_translucent.fsh.txt',
        'data/create_submarine/recipes/submarine_propeller.json')
    $refmap = Read-ZipEntryText -Path $builtMainJar -EntryName 'createdeepseas.refmap.json'
    if (-not $refmap.Contains('Lnet/minecraft/world/phys/Vec3;m_82509_')) {
        throw 'Release JAR refmap lacks the production Simulated rope mapping.'
    }

    Assert-GitCheckpoint -ExpectedHead $headCommit -ExpectedTag $tagName -ExpectedTagObject $tagObject
    Assert-LocalInputs

    $sourceZipPath = Join-Path $stagingRoot $sourceZipName
    $oldPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        & git.exe -C $projectDir archive --format=zip --output=$sourceZipPath $headCommit 2>&1 | Out-Host
        $archiveExitCode = $LASTEXITCODE
    }
    finally { $ErrorActionPreference = $oldPreference }
    if ($archiveExitCode -ne 0) { throw 'git archive failed.' }

    Assert-ZipEntries -Path $sourceZipPath -RequiredEntries @(
        'LICENSE', 'README.md', 'README.zh-CN.md', 'build.gradle', 'gradle.properties',
        'gradlew.bat', 'build-release.bat', 'scripts/build-release.ps1',
        'docs/VALIDATION.md', 'docs/RELEASE.md', 'gradle/verification-metadata.xml')

    Expand-Archive -LiteralPath $sourceZipPath -DestinationPath $workspaceStage
    foreach ($entry in $requiredLocalFiles.GetEnumerator()) {
        $destination = Join-Path $workspaceStage $entry.Key
        [System.IO.Directory]::CreateDirectory((Split-Path -Parent $destination)) | Out-Null
        Copy-Item -LiteralPath (Join-Path $projectDir $entry.Key) -Destination $destination
        Assert-FileHash -Path $destination -ExpectedSha256 $entry.Value `
            -Description "staged $($entry.Key)"
    }

    $checkpointDir = Join-Path $workspaceStage '.checkpoint'
    [System.IO.Directory]::CreateDirectory($checkpointDir) | Out-Null
    $bundlePath = Join-Path $checkpointDir $bundleName
    Invoke-GitText -Arguments @(
        'bundle', 'create', $bundlePath,
        "refs/heads/$expectedBranch", "refs/tags/$tagName") | Out-Null
    Invoke-GitText -Arguments @('bundle', 'verify', $bundlePath) | Out-Null
    Assert-GitCheckpoint -ExpectedHead $headCommit -ExpectedTag $tagName -ExpectedTagObject $tagObject

    $bundleHeads = Invoke-GitText -Arguments @('bundle', 'list-heads', $bundlePath)
    if ($bundleHeads -notmatch ([regex]::Escape($headCommit) + '\s+refs/heads/' + [regex]::Escape($expectedBranch)) -or
        $bundleHeads -notmatch ([regex]::Escape($tagObject) + '\s+refs/tags/' + [regex]::Escape($tagName))) {
        throw 'Checkpoint bundle refs do not match the locked checkpoint.'
    }

    $inputLines = @(
        "Build input SHA-256 manifest for $modVersion",
        "Checkpoint: $headCommit",
        ''
    )
    foreach ($entry in $requiredLocalFiles.GetEnumerator()) {
        $inputLines += "$($entry.Value)  $($entry.Key.Replace('\', '/'))"
    }
    $inputLines += "$(Read-GradleProperty -Name 'copycats_sha256')  Maven Copycats contract artifact"
    $inputLines += "$(Read-GradleProperty -Name 'embeddium_sha256')  Maven Embeddium contract artifact"
    $inputLines | Set-Content -LiteralPath (Join-Path $workspaceStage 'BUILD_INPUTS_SHA256.txt') -Encoding UTF8

    @(
        "Version: $modVersion",
        "Branch: $expectedBranch",
        "Commit: $headCommit",
        "Annotated tag: $tagName",
        "Tag object: $tagObject",
        'Target: Minecraft 1.20.1 / Forge 47.4.0 / Java 17',
        "Tests: $expectedTests/$expectedTests in $expectedSuites suites",
        'Distribution: local engineering checkpoint; public binary redistribution is not authorized.',
        'Recovery: build directly from this extracted workspace, or follow RESTORE.md.'
    ) | Set-Content -LiteralPath (Join-Path $workspaceStage 'CHECKPOINT.txt') -Encoding UTF8

    $copyLines = @()
    foreach ($entry in $requiredLocalFiles.GetEnumerator()) {
        $source = $entry.Key
        $copyLines += "Copy-Item -LiteralPath '..\$source' -Destination '.\$source' -Force"
    }
    $restoreLines = @(
        '# Restore this checkpoint',
        '',
        'The extracted minimal workspace can build directly with Java 17:',
        '',
        '```powershell',
        '.\gradlew.bat clean build --no-daemon',
        '```',
        '',
        'To restore the exact Git branch and annotated tag into a clean clone:',
        '',
        '```powershell',
        "git clone -b $expectedBranch '.\.checkpoint\$bundleName' restored-repository",
        'Set-Location restored-repository',
        "New-Item -ItemType Directory -Path '.\localDeps' -Force | Out-Null"
    )
    $restoreLines += $copyLines
    $restoreLines += @(
        'git status --short',
        '.\gradlew.bat clean build --no-daemon',
        '```',
        '',
        'Only the five ignored local dependency JARs are copied into the clone, so `git status` remains clean.',
        'The build-input manifest stays in the parent minimal workspace for independent verification.'
    )
    $restoreLines | Set-Content -LiteralPath (Join-Path $workspaceStage 'RESTORE.md') -Encoding UTF8

    # Exercise the documented bundle recovery path before publishing the workspace ZIP.
    # A Git clone does not materialize the ignored localDeps directory, so this probe
    # also guards the directory-creation step in RESTORE.md.
    $restoreProbe = Assert-StrictChildPath `
        -Path (Join-Path $stagingRoot 'restore-probe') -ParentPath $stagingRoot
    if (Test-Path -LiteralPath $restoreProbe) {
        Remove-Item -LiteralPath $restoreProbe -Recurse -Force
    }
    try {
        Invoke-GitText -WorkingDirectory $stagingRoot -Arguments @(
            'clone', '-b', $expectedBranch, $bundlePath, $restoreProbe) | Out-Null
        [System.IO.Directory]::CreateDirectory((Join-Path $restoreProbe 'localDeps')) | Out-Null
        foreach ($entry in $requiredLocalFiles.GetEnumerator()) {
            $probeDependency = Join-Path $restoreProbe $entry.Key
            Copy-Item -LiteralPath (Join-Path $workspaceStage $entry.Key) `
                -Destination $probeDependency -Force
            Assert-FileHash -Path $probeDependency -ExpectedSha256 $entry.Value `
                -Description "restored $($entry.Key)"
        }
        $probeHead = Invoke-GitText -WorkingDirectory $restoreProbe -Arguments @('rev-parse', 'HEAD')
        $probeTagType = Invoke-GitText -WorkingDirectory $restoreProbe -Arguments @(
            'cat-file', '-t', "refs/tags/$tagName")
        $probeTagCommit = Invoke-GitText -WorkingDirectory $restoreProbe -Arguments @(
            'rev-parse', '--verify', "refs/tags/$tagName^{commit}")
        $probeStatus = Invoke-GitText -WorkingDirectory $restoreProbe -Arguments @(
            'status', '--porcelain=v1', '--untracked-files=all')
        if ($probeHead -ne $headCommit -or $probeTagType -ne 'tag' -or
            $probeTagCommit -ne $headCommit -or -not [string]::IsNullOrWhiteSpace($probeStatus)) {
            throw 'Restored bundle probe did not reproduce the clean tagged checkpoint.'
        }
    }
    finally {
        if (Test-Path -LiteralPath $restoreProbe) {
            Remove-Item -LiteralPath $restoreProbe -Recurse -Force
        }
    }

    $workspaceManifest = Join-Path $workspaceStage 'WORKSPACE_MANIFEST_SHA256.txt'
    Write-DirectoryManifest -Root $workspaceStage -ManifestPath $workspaceManifest
    $workspaceZipPath = Join-Path $stagingRoot $workspaceZipName
    New-PortableZipFromDirectory -SourceDirectory $workspaceStage -DestinationPath $workspaceZipPath

    $requiredWorkspaceEntries = @(
        'RESTORE.md', 'CHECKPOINT.txt', 'BUILD_INPUTS_SHA256.txt',
        'WORKSPACE_MANIFEST_SHA256.txt', ".checkpoint/$bundleName")
    $requiredWorkspaceEntries += @($requiredLocalFiles.Keys | ForEach-Object { $_.Replace('\', '/') })
    Assert-ZipEntries -Path $workspaceZipPath -RequiredEntries $requiredWorkspaceEntries
    Assert-ZipManifest -Path $workspaceZipPath -ManifestEntry 'WORKSPACE_MANIFEST_SHA256.txt'

    Copy-Item -LiteralPath $builtMainJar -Destination (Join-Path $stagingRoot $mainJarName)
    Copy-Item -LiteralPath $builtSourcesJar -Destination (Join-Path $stagingRoot $sourcesJarName)
    Assert-FileHash -Path (Join-Path $stagingRoot $mainJarName) `
        -ExpectedSha256 (Get-FileHash -LiteralPath $builtMainJar -Algorithm SHA256).Hash -Description $mainJarName
    Assert-FileHash -Path (Join-Path $stagingRoot $sourcesJarName) `
        -ExpectedSha256 (Get-FileHash -LiteralPath $builtSourcesJar -Algorithm SHA256).Hash -Description $sourcesJarName

    @(
        "Create Deep Seas $modVersion",
        "Commit: $headCommit",
        "Annotated tag: $tagName ($tagObject)",
        'Target: Minecraft 1.20.1 / Forge 47.4.0 / Java 17',
        "Automated tests: $expectedTests/$expectedTests in $expectedSuites suites",
        'Required runtime:',
        '- Create 6.0.8, Flywheel 1.0.5 and Ponder 1.0.91',
        '- Veil 1.0.0.296',
        '- Sable 2.0.5-port.1 production JAR',
        '- Simulated and Aeronautics 1.3.1-port.1 production JARs',
        'Do not install -dev or -sources artifacts in a normal game instance.',
        'See README.md and docs/VALIDATION.md for optional integrations and behavior boundaries.'
    ) | Set-Content -LiteralPath (Join-Path $stagingRoot 'DEPENDENCIES.txt') -Encoding UTF8

    @(
        "# Create Deep Seas $modVersion local engineering checkpoint",
        '',
        "- Source baseline: 37b876cea7e06e6a3ea56fce998aff9c4a0d3984",
        "- Port checkpoint: $headCommit",
        "- Automated tests: $expectedTests/$expectedTests",
        '- Clean Forge build and production-JAR verification: passed',
        '- PJ production world entry, F3+T, pause/resume, save and shutdown: passed on a source-equivalent candidate',
        '- Public compiled redistribution: not authorized by this checkpoint',
        '',
        'The minimal-workspace ZIP contains the exact Git bundle and hash-locked local inputs needed for recovery.'
    ) | Set-Content -LiteralPath (Join-Path $stagingRoot 'RELEASE_NOTES.md') -Encoding UTF8

    Copy-Item -LiteralPath (Join-Path $workspaceStage 'BUILD_INPUTS_SHA256.txt') `
        -Destination (Join-Path $stagingRoot 'BUILD_INPUTS_SHA256.txt')
    @(
        "Version: $modVersion",
        "Branch: $expectedBranch",
        "Commit: $headCommit",
        "Annotated tag: $tagName",
        "Tag object: $tagObject"
    ) | Set-Content -LiteralPath (Join-Path $stagingRoot 'CHECKPOINT.txt') -Encoding UTF8

    Remove-Item -LiteralPath $workspaceStage -Recurse -Force
    $sumsPath = Join-Path $stagingRoot 'SHA256SUMS.txt'
    Write-Sha256Sums -Directory $stagingRoot -OutputPath $sumsPath
    Assert-Sha256Sums -Directory $stagingRoot -SumsPath $sumsPath

    Assert-GitCheckpoint -ExpectedHead $headCommit -ExpectedTag $tagName -ExpectedTagObject $tagObject
    Assert-LocalInputs

    $finalReleaseDir = Assert-StrictChildPath `
        -Path (Join-Path $releaseRoot $modVersion) -ParentPath $releaseRoot
    $previousReleaseDir = Assert-StrictChildPath `
        -Path (Join-Path $releaseRoot ".$modVersion.previous") -ParentPath $releaseRoot
    if (-not (Test-Path -LiteralPath $finalReleaseDir) -and
        (Test-Path -LiteralPath $previousReleaseDir)) {
        Move-Item -LiteralPath $previousReleaseDir -Destination $finalReleaseDir
    }
    if ((Test-Path -LiteralPath $finalReleaseDir) -and
        (Test-Path -LiteralPath $previousReleaseDir)) {
        Remove-Item -LiteralPath $previousReleaseDir -Recurse -Force
    }

    $distMain = Join-Path $distRoot $mainJarName
    $distSums = Join-Path $distRoot 'SHA256SUMS.txt'
    $rollbackDir = Reset-StrictChildDirectory `
        -Path (Join-Path $releaseRoot ".rollback-$modVersion-$runId") -ParentPath $releaseRoot
    if (Test-Path -LiteralPath $distMain) {
        Copy-Item -LiteralPath $distMain -Destination (Join-Path $rollbackDir $mainJarName)
    }
    if (Test-Path -LiteralPath $distSums) {
        Copy-Item -LiteralPath $distSums -Destination (Join-Path $rollbackDir 'SHA256SUMS.txt')
    }

    try {
        if (Test-Path -LiteralPath $finalReleaseDir) {
            Move-Item -LiteralPath $finalReleaseDir -Destination $previousReleaseDir
        }
        Move-Item -LiteralPath $stagingRoot -Destination $finalReleaseDir
        Copy-Item -LiteralPath (Join-Path $finalReleaseDir $mainJarName) -Destination $distMain -Force
        $distHash = (Get-FileHash -LiteralPath $distMain -Algorithm SHA256).Hash
        "$distHash  $mainJarName" | Set-Content -LiteralPath $distSums -Encoding UTF8
        Assert-FileHash -Path $distMain -ExpectedSha256 $distHash -Description 'dist production JAR'
        if (Test-Path -LiteralPath $previousReleaseDir) {
            Remove-Item -LiteralPath $previousReleaseDir -Recurse -Force
        }
    }
    catch {
        if (Test-Path -LiteralPath $finalReleaseDir) {
            Remove-Item -LiteralPath $finalReleaseDir -Recurse -Force
        }
        if (Test-Path -LiteralPath $previousReleaseDir) {
            Move-Item -LiteralPath $previousReleaseDir -Destination $finalReleaseDir
        }
        if (Test-Path -LiteralPath (Join-Path $rollbackDir $mainJarName)) {
            Copy-Item -LiteralPath (Join-Path $rollbackDir $mainJarName) -Destination $distMain -Force
        } elseif (Test-Path -LiteralPath $distMain) {
            Remove-Item -LiteralPath $distMain -Force
        }
        if (Test-Path -LiteralPath (Join-Path $rollbackDir 'SHA256SUMS.txt')) {
            Copy-Item -LiteralPath (Join-Path $rollbackDir 'SHA256SUMS.txt') -Destination $distSums -Force
        } elseif (Test-Path -LiteralPath $distSums) {
            Remove-Item -LiteralPath $distSums -Force
        }
        throw
    }
    finally {
        if (Test-Path -LiteralPath $rollbackDir) {
            Remove-Item -LiteralPath $rollbackDir -Recurse -Force
        }
    }

    Write-Host "Verified local checkpoint: $finalReleaseDir"
    Write-Host "Production JAR: $distMain"
}
finally {
    if (Test-Path -LiteralPath $stagingRoot) {
        Remove-Item -LiteralPath $stagingRoot -Recurse -Force
    }
}
