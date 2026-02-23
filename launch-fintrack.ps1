# Script simplifié pour lancer FinTrack avec authentification biométrique
# Usage: .\launch-fintrack.ps1

Write-Host "=== Lancement de FinTrack ===" -ForegroundColor Cyan
Write-Host ""

# Définir JAVA_HOME
$env:JAVA_HOME = "C:\Users\HP\Downloads\jdk-21_windows-x64_bin\jdk-21.0.10"
$javaExe = Join-Path $env:JAVA_HOME "bin\java.exe"

if (-not (Test-Path $javaExe)) {
    Write-Host "ERREUR: Java non trouvé à $javaExe" -ForegroundColor Red
    Write-Host "Veuillez ajuster JAVA_HOME dans ce script" -ForegroundColor Yellow
    exit 1
}

Write-Host "Java trouvé: $javaExe" -ForegroundColor Green

# Aller dans le répertoire du projet
cd C:\Users\HP\IdeaProjects\FInTrack

# Compiler le projet si nécessaire
Write-Host "Compilation du projet..." -ForegroundColor Yellow
& $javaExe -version

$mavenWrapper = ".\mvnw.cmd"
if (Test-Path $mavenWrapper) {
    Write-Host "Utilisation du wrapper Maven..." -ForegroundColor Yellow

    # Compiler
    cmd /c "$mavenWrapper clean compile -DskipTests"

    if ($LASTEXITCODE -eq 0) {
        Write-Host "Compilation réussie!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Lancement de l'application JavaFX..." -ForegroundColor Cyan
        Write-Host ""

        # Lancer l'application
        cmd /c "$mavenWrapper javafx:run"
    } else {
        Write-Host "Erreur lors de la compilation" -ForegroundColor Red
    }
} else {
    Write-Host "ERREUR: mvnw.cmd non trouvé" -ForegroundColor Red
    Write-Host "Essayez de lancer directement avec Maven si installé: mvn javafx:run" -ForegroundColor Yellow
}
