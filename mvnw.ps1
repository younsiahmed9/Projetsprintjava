param(
  [Parameter(ValueFromRemainingArguments=$true)]
  [string[]]$Args
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

if (-not $env:JAVA_HOME) {
  throw "JAVA_HOME n'est pas dأ©fini."
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