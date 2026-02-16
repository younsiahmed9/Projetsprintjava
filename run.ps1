# FinTrack - Script de lancement
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " FinTrack - Gestion Documents" -ForegroundColor Cyan
Write-Host " Lancement de l'application..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier que Maven est installé
try {
    $mvnVersion = mvn --version 2>&1 | Select-Object -First 1
    Write-Host "✓ Maven détecté: $mvnVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ ERREUR: Maven n'est pas installé ou n'est pas dans le PATH!" -ForegroundColor Red
    pause
    exit 1
}

# Se placer dans le répertoire du script
Set-Location $PSScriptRoot

Write-Host ""
Write-Host "Compilation du projet..." -ForegroundColor Yellow
mvn clean compile

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "✗ ERREUR: La compilation a échoué!" -ForegroundColor Red
    pause
    exit 1
}

Write-Host ""
Write-Host "✓ Compilation réussie!" -ForegroundColor Green
Write-Host ""
Write-Host "Lancement de l'application JavaFX..." -ForegroundColor Yellow
Write-Host ""

mvn javafx:run

Write-Host ""
Write-Host "Application fermée." -ForegroundColor Cyan
pause

