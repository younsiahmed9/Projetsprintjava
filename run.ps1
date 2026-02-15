# Script PowerShell pour lancer FinTrack avec JavaFX correctement configuré

Write-Host "======================================"
Write-Host "  Lancement de FinTrack"
Write-Host "======================================"

# Définir le répertoire du projet
$projectDir = "C:\Users\Mega-PC\IdeaProjects\FinTrack"
cd $projectDir

# Vérifier si JAVA_HOME est défini
if (-not $env:JAVA_HOME) {
    Write-Host "ERREUR: JAVA_HOME n'est pas défini!" -ForegroundColor Red
    Write-Host "Veuillez configurer JAVA_HOME pour pointer vers votre JDK 17"
    Read-Host "Appuyez sur Entrée pour quitter"
    exit 1
}

Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green

# Compiler le projet
Write-Host "`nCompilation du projet..." -ForegroundColor Yellow
mvn clean compile

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERREUR lors de la compilation!" -ForegroundColor Red
    Read-Host "Appuyez sur Entrée pour quitter"
    exit 1
}

Write-Host "Compilation réussie!" -ForegroundColor Green

# Lancer l'application avec les options JavaFX
Write-Host "`nLancement de l'application..." -ForegroundColor Yellow

$javaCmd = "$env:JAVA_HOME\bin\java.exe"
$modulePath = "$projectDir\lib\javafx-sdk-17\lib"

# Essayer d'abord via Maven javafx:run
Write-Host "Tentative via Maven javafx:run..."
mvn javafx:run

if ($LASTEXITCODE -ne 0) {
    Write-Host "Tentative via java direct..." -ForegroundColor Yellow

    # Vérifier si le répertoire target/classes existe
    if (-not (Test-Path "$projectDir\target\classes")) {
        Write-Host "ERREUR: target/classes n'existe pas. Compilation échouée?" -ForegroundColor Red
        Read-Host "Appuyez sur Entrée pour quitter"
        exit 1
    }

    # Lancer via java directement
    $javaArgs = @(
        "--module-path", "C:\javafx-sdk-17\lib",
        "--add-modules", "javafx.controls,javafx.fxml",
        "-cp", "$projectDir\target\classes;$projectDir\target\dependency\*",
        "Test.MainFx"
    )

    & $javaCmd $javaArgs
}

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nApplication terminée avec succès!" -ForegroundColor Green
} else {
    Write-Host "`nERREUR lors de l'exécution de l'application!" -ForegroundColor Red
}

Read-Host "Appuyez sur Entrée pour quitter"

