param(
  [string]$JavaHome = ""
)

$ErrorActionPreference = 'Stop'
Set-Location (Split-Path -Parent $MyInvocation.MyCommand.Path)

function Resolve-JavaHome {
  param([string]$Hint)
  if ($Hint -and (Test-Path (Join-Path $Hint 'bin\java.exe'))) { return $Hint }
  if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) { return $env:JAVA_HOME }

  $candidates = @(
    'C:\Program Files\Java\jdk-21',
    'C:\Program Files\Eclipse Adoptium\jdk-21*',
    'C:\Program Files\Eclipse Adoptium\jdk-17*',
    "$env:USERPROFILE\Downloads\jdk-21*"
  )

  foreach ($c in $candidates) {
    foreach ($p in (Get-Item $c -ErrorAction SilentlyContinue)) {
      if (Test-Path (Join-Path $p.FullName 'bin\java.exe')) { return $p.FullName }
    }
  }
  throw "JAVA_HOME introuvable. Définis JAVA_HOME vers un JDK (dossier contenant bin\\java.exe)."
}

$resolved = Resolve-JavaHome -Hint $JavaHome
$env:JAVA_HOME = $resolved
$env:Path = "$resolved\bin;$env:Path"

Write-Host "JAVA_HOME=$resolved"

$wrapperJar = Join-Path $PWD '.mvn\wrapper\maven-wrapper.jar'
if (-not (Test-Path $wrapperJar)) {
  throw "maven-wrapper.jar introuvable: $wrapperJar"
}

# Rewrite mvnw.cmd as a clean ASCII batch file (no BOM).
$cmd = @'
@echo off
rem ------------------------------------------------------------------------------
rem Maven Wrapper startup batch script (Windows)
rem ------------------------------------------------------------------------------
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR%"=="" set "MAVEN_PROJECTBASEDIR=."

set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"

if not exist "%WRAPPER_JAR%" (
  echo Maven wrapper jar not found at "%WRAPPER_JAR%".
  exit /b 1
)

if not defined JAVA_HOME (
  echo JAVA_HOME is not set.
  exit /b 1
)

set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not exist "%JAVA_EXE%" (
  echo java.exe not found under JAVA_HOME: "%JAVA_EXE%"
  exit /b 1
)

"%JAVA_EXE%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
'@

[System.IO.File]::WriteAllText(".\\mvnw.cmd", $cmd, [System.Text.Encoding]::ASCII)
Write-Host "mvnw.cmd réécrit en ASCII (sans BOM)."

# Also create a PowerShell launcher that bypasses cmd.exe parsing pitfalls.
$ps1 = @'
param(
  [Parameter(ValueFromRemainingArguments=$true)]
  [string[]]$Args
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

if (-not $env:JAVA_HOME) {
  throw "JAVA_HOME n'est pas défini."
}
$java = Join-Path $env:JAVA_HOME 'bin\java.exe'
if (-not (Test-Path $java)) {
  throw "java.exe introuvable sous JAVA_HOME: $java"
}

$wrapperJar = Join-Path $projectRoot '.mvn\wrapper\maven-wrapper.jar'
if (-not (Test-Path $wrapperJar)) {
  throw "maven-wrapper.jar introuvable: $wrapperJar"
}

& $java "-Dmaven.multiModuleProjectDirectory=$projectRoot" -classpath $wrapperJar org.apache.maven.wrapper.MavenWrapperMain @Args
'@

[System.IO.File]::WriteAllText(".\\mvnw.ps1", $ps1, [System.Text.Encoding]::UTF8)
Write-Host "mvnw.ps1 créé (lanceur PowerShell fiable)."

# Quick smoke test
Write-Host "Smoke test: .\\mvnw.ps1 -v"
& .\mvnw.ps1 -v
