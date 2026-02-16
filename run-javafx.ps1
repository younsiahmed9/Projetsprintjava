param(
  [string]$JavaHome = "",
  [switch]$UseIdeaListener
)

$ErrorActionPreference = 'Stop'

# Resolve project root
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

function Resolve-JavaHome {
  param([string]$Hint)

  if ($Hint -and (Test-Path (Join-Path $Hint 'bin\java.exe'))) {
    return $Hint
  }

  if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    return $env:JAVA_HOME
  }

  $candidates = @(
    'C:\Program Files\Java\jdk-21',
    'C:\Program Files\Eclipse Adoptium\jdk-21*',
    'C:\Program Files\Eclipse Adoptium\jdk-17*',
    "$env:USERPROFILE\Downloads\jdk-21*"
  )

  foreach ($c in $candidates) {
    foreach ($p in (Get-Item $c -ErrorAction SilentlyContinue)) {
      $j = $p.FullName
      if (Test-Path (Join-Path $j 'bin\java.exe')) {
        return $j
      }
    }
  }

  throw "Impossible de trouver un JDK valide. Définis JAVA_HOME vers le dossier du JDK (il doit contenir bin\\java.exe)."
}

$resolvedJavaHome = Resolve-JavaHome -Hint $JavaHome
$env:JAVA_HOME = $resolvedJavaHome
$env:Path = "$resolvedJavaHome\bin;$env:Path"

Write-Host "JAVA_HOME=$resolvedJavaHome"
& java -version

# En dehors d'IntelliJ, on n'a PAS besoin de maven-event-listener. Il ne fait qu'ajouter des soucis de quoting Windows.
$args = @('javafx:run', '-f', 'pom.xml')

if ($UseIdeaListener) {
  $ideaListener = "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.2\plugins\maven\lib\maven-event-listener.jar"
  if (Test-Path $ideaListener) {
    $args = @(
      "-Dmaven.ext.class.path=$ideaListener",
      "-Djansi.passthrough=true",
      "-Dstyle.color=always"
    ) + $args
  } else {
    Write-Warning "UseIdeaListener demandé, mais listener introuvable: $ideaListener"
  }
}

Write-Host "Running: .\\mvnw.cmd $($args -join ' ')"
& .\mvnw.cmd @args
