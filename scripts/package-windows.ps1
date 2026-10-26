param(
    [string]$OutputDirectory = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot

if ($env:OS -ne "Windows_NT") {
    throw "Build the Windows app image on Windows 11. Cross-packaging from Linux is not supported."
}
if (-not $env:JAVA_HOME) {
    throw "Set JAVA_HOME to a Windows x64 JDK 21 installation."
}
$Java = Join-Path $env:JAVA_HOME "bin\java.exe"
$Jpackage = Join-Path $env:JAVA_HOME "bin\jpackage.exe"
$Jlink = Join-Path $env:JAVA_HOME "bin\jlink.exe"
foreach ($Tool in @($Java, $Jpackage, $Jlink)) {
    if (-not (Test-Path -LiteralPath $Tool -PathType Leaf)) {
        throw "Required JDK tool not found: $Tool"
    }
}
$JavaVersion = & $Jpackage --version
if ($LASTEXITCODE -ne 0 -or $JavaVersion -notmatch '^21(?:\.|$)') {
    throw "Packaging requires JDK 21."
}
# Windows PowerShell treats native stderr as an error under ErrorActionPreference=Stop.
$SavedPreference = $ErrorActionPreference
try {
    $ErrorActionPreference = "Continue"
    $JavaProperties = (& $Java -XshowSettings:properties -version 2>&1 | Out-String)
    $JavaExitCode = $LASTEXITCODE
} finally {
    $ErrorActionPreference = $SavedPreference
}
if ($JavaExitCode -ne 0 -or $JavaProperties -notmatch 'os\.arch\s*=\s*amd64') {
    throw "Use a Windows x64 JDK 21; other architectures are not prepared by this script."
}

if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $OutputDirectory = Join-Path $ProjectRoot "target\windows-dist"
} else {
    $OutputDirectory = [System.IO.Path]::GetFullPath($OutputDirectory)
}
$AppImage = Join-Path $OutputDirectory "CurrencyConverter"
$Archive = Join-Path $OutputDirectory "CurrencyConverter-windows-x64.zip"
if ((Test-Path -LiteralPath $AppImage) -or (Test-Path -LiteralPath $Archive)) {
    throw "The output already exists. Choose a fresh -OutputDirectory or preserve/remove the old output first."
}

Push-Location $ProjectRoot
try {
    & (Join-Path $ProjectRoot "mvnw.cmd") --batch-mode --no-transfer-progress clean verify -Papp-image
    if ($LASTEXITCODE -ne 0) {
        throw "Maven verification failed; no app image was created."
    }
    if (-not (Test-Path -LiteralPath $OutputDirectory -PathType Container)) {
        New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
    }
    $InputDirectory = Join-Path $ProjectRoot "target\package-input"
    $MainJar = "java-currency-converter-0.1.0-SNAPSHOT.jar"
    Copy-Item -LiteralPath (Join-Path $ProjectRoot "target\$MainJar") -Destination $InputDirectory
    $RequiredJavaFxJars = @(
        "javafx-base-21.0.13-win.jar",
        "javafx-controls-21.0.13-win.jar",
        "javafx-graphics-21.0.13-win.jar"
    )
    $MissingJavaFxJars = @(
        $RequiredJavaFxJars | Where-Object {
            -not (Test-Path -LiteralPath (Join-Path $InputDirectory $_) -PathType Leaf)
        }
    )
    if ($MissingJavaFxJars.Count -gt 0) {
        throw "Required Windows JavaFX libraries are missing: $($MissingJavaFxJars -join ', '). Check JDK architecture and Maven platform detection."
    }
    if (Get-ChildItem -LiteralPath $InputDirectory -Filter "*linux*.jar") {
        throw "Linux JavaFX libraries must not be included in the Windows image."
    }
    $Arguments = @(
        "--type", "app-image",
        "--name", "CurrencyConverter",
        "--app-version", "0.1.0",
        "--vendor", "University Team",
        "--description", "Desktop currency converter",
        "--input", $InputDirectory,
        "--main-jar", $MainJar,
        "--main-class", "com.github.panteliszara.currencyconverter.Launcher",
        "--add-modules", "java.desktop,java.net.http,java.logging,jdk.crypto.ec,jdk.unsupported",
        "--java-options", '--module-path=$APPDIR',
        "--java-options", "--add-modules=javafx.controls",
        "--dest", $OutputDirectory
    )
    & $Jpackage @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "jpackage failed. Inspect its output before trying again."
    }
    Compress-Archive -LiteralPath $AppImage -DestinationPath $Archive
    $Hash = (Get-FileHash -LiteralPath $Archive -Algorithm SHA256).Hash.ToLowerInvariant()
    Set-Content -LiteralPath "$Archive.sha256" -Encoding Ascii -Value "$Hash  CurrencyConverter-windows-x64.zip"
    Write-Host "Portable image: $AppImage"
    Write-Host "Archive: $Archive"
    Write-Host "JDK: $JavaVersion"
    Write-Host "Windows 11 clean-machine verification is required before distribution."
} finally {
    Pop-Location
}
