# Script Final pour Lancer FinTrack avec Authentification Biometrique
# Ce script configure tout et lance l'application
# Usage: .\START-FINTRACK.ps1

Write-Host ""
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host "                                                             " -ForegroundColor Cyan
Write-Host "        FINTRACK - Authentification Biometrique              " -ForegroundColor Cyan
Write-Host "                                                             " -ForegroundColor Cyan
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$JAVA_HOME = "C:\Users\HP\Downloads\jdk-21_windows-x64_bin\jdk-21.0.10"
$PROJECT_DIR = "C:\Users\HP\IdeaProjects\FInTrack"

# Verifier Java
Write-Host "Verification de l'environnement..." -ForegroundColor Yellow
Write-Host ""

$javaExe = Join-Path $JAVA_HOME "bin\java.exe"
if (-not (Test-Path $javaExe)) {
    Write-Host "ERREUR: Java non trouve" -ForegroundColor Red
    Write-Host ""
    pause
    exit 1
}

Write-Host "Java trouve" -ForegroundColor Green
& $javaExe -version
Write-Host ""

# Aller dans le repertoire du projet
Set-Location $PROJECT_DIR

# Verifier MySQL
Write-Host "Verification de MySQL..." -ForegroundColor Yellow
$mysqlProcess = Get-Process mysqld -ErrorAction SilentlyContinue
if ($mysqlProcess) {
    Write-Host "MySQL en cours d'execution" -ForegroundColor Green
} else {
    Write-Host "MySQL ne semble pas demarre" -ForegroundColor Yellow
}
Write-Host ""

# Option IntelliJ IDEA
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host "  METHODE RECOMMANDEE : IntelliJ IDEA" -ForegroundColor Cyan
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pour de meilleurs resultats, utilisez IntelliJ IDEA :" -ForegroundColor White
Write-Host "  1. Ouvrez IntelliJ IDEA" -ForegroundColor Gray
Write-Host "  2. Ouvrez le projet: $PROJECT_DIR" -ForegroundColor Gray
Write-Host "  3. Naviguez vers: src/main/java/Controllers/FinTrackApp.java" -ForegroundColor Gray
Write-Host "  4. Clic droit -> Run FinTrackApp.main()" -ForegroundColor Gray
Write-Host ""

$response = Read-Host "Essayer de lancer depuis la ligne de commande? (o/n)"
if ($response -ne 'o') {
    Write-Host ""
    Write-Host "OK! Ouvrez IntelliJ IDEA." -ForegroundColor Green
    Write-Host ""
    pause
    exit 0
}

Write-Host ""
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host "  Tentative de lancement..." -ForegroundColor Cyan
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host ""

# Definir JAVA_HOME pour la session
$env:JAVA_HOME = $JAVA_HOME

# Lancer avec Maven
Write-Host "Lancement de l'application..." -ForegroundColor Cyan
Write-Host ""

$mvnw = Join-Path $PROJECT_DIR "mvnw.cmd"
if (Test-Path $mvnw) {
    cmd /c "$mvnw javafx:run"
} else {
    Write-Host "Impossible de lancer" -ForegroundColor Red
    Write-Host ""
    Write-Host "Utilisez IntelliJ IDEA" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host ""
pause

Write-Host ""
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host "                                                             " -ForegroundColor Cyan
Write-Host "        FINTRACK - Authentification Biometrique              " -ForegroundColor Cyan
Write-Host "                                                             " -ForegroundColor Cyan
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$JAVA_HOME = "C:\Users\HP\Downloads\jdk-21_windows-x64_bin\jdk-21.0.10"
$PROJECT_DIR = "C:\Users\HP\IdeaProjects\FInTrack"

# Vérifier Java
Write-Host "📋 Vérification de l'environnement..." -ForegroundColor Yellow
Write-Host ""

$javaExe = Join-Path $JAVA_HOME "bin\java.exe"
if (-not (Test-Path $javaExe)) {
    Write-Host "❌ ERREUR: Java non trouvé à: $javaExe" -ForegroundColor Red
    Write-Host ""
    Write-Host "Veuillez ajuster la variable JAVA_HOME dans ce script" -ForegroundColor Yellow
    Write-Host ""
    pause
    exit 1
}

Write-Host "✅ Java trouvé: $javaExe" -ForegroundColor Green
& $javaExe -version
Write-Host ""

# Aller dans le répertoire du projet
Set-Location $PROJECT_DIR

# Vérifier MySQL
Write-Host "📋 Vérification de MySQL..." -ForegroundColor Yellow
$mysqlProcess = Get-Process mysqld -ErrorAction SilentlyContinue
if ($mysqlProcess) {
    Write-Host "✅ MySQL est en cours d'exécution" -ForegroundColor Green
} else {
    Write-Host "⚠️  MySQL ne semble pas démarré" -ForegroundColor Yellow
    Write-Host "   Veuillez démarrer XAMPP et MySQL avant de continuer" -ForegroundColor Yellow
    Write-Host ""
    $response = Read-Host "Continuer quand même? (o/n)"
    if ($response -ne 'o') {
        exit 0
    }
}
Write-Host ""

# Option 1 : Essayer IntelliJ IDEA
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host "  METHODE RECOMMANDEE : IntelliJ IDEA" -ForegroundColor Cyan
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pour de meilleurs resultats, lancez l'application depuis IntelliJ IDEA :" -ForegroundColor White
Write-Host "  1. Ouvrez IntelliJ IDEA" -ForegroundColor Gray
Write-Host "  2. Ouvrez le projet: $PROJECT_DIR" -ForegroundColor Gray
Write-Host "  3. Naviguez vers: src/main/java/Controllers/FinTrackApp.java" -ForegroundColor Gray
Write-Host "  4. Clic droit -> Run FinTrackApp.main()" -ForegroundColor Gray
Write-Host ""

$response = Read-Host "Voulez-vous essayer de lancer depuis la ligne de commande? (o/n)"
if ($response -ne 'o') {
    Write-Host ""
    Write-Host "OK! Ouvrez IntelliJ IDEA et suivez les étapes ci-dessus." -ForegroundColor Green
    Write-Host ""
    pause
    exit 0
}

Write-Host ""
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host "  Tentative de lancement depuis la ligne de commande..." -ForegroundColor Cyan
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host ""

# Définir JAVA_HOME pour la session
$env:JAVA_HOME = $JAVA_HOME

# Vérifier si les classes sont compilées
$mainClass = Join-Path $PROJECT_DIR "target\classes\Controllers\FinTrackApp.class"
if (-not (Test-Path $mainClass)) {
    Write-Host "⚠️  Classes non compilées, compilation en cours..." -ForegroundColor Yellow
    Write-Host ""

    $mvnw = Join-Path $PROJECT_DIR "mvnw.cmd"
    if (Test-Path $mvnw) {
        cmd /c "$mvnw clean compile -DskipTests"
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Erreur lors de la compilation" -ForegroundColor Red
            Write-Host ""
            pause
            exit 1
        }
    } else {
        Write-Host "❌ mvnw.cmd non trouvé" -ForegroundColor Red
        Write-Host "Veuillez compiler le projet dans IntelliJ IDEA d'abord" -ForegroundColor Yellow
        Write-Host ""
        pause
        exit 1
    }
}

Write-Host "✅ Classes compilées" -ForegroundColor Green
Write-Host ""

# Essayer de lancer avec Maven
Write-Host "🚀 Lancement de l'application..." -ForegroundColor Cyan
Write-Host ""

$mvnw = Join-Path $PROJECT_DIR "mvnw.cmd"
if (Test-Path $mvnw) {
    # Tentative avec mvnw
    Write-Host "Utilisation du wrapper Maven..." -ForegroundColor Gray
    cmd /c "$mvnw javafx:run"
} else {
    Write-Host "❌ Impossible de lancer avec Maven" -ForegroundColor Red
    Write-Host ""
    Write-Host "Veuillez utiliser IntelliJ IDEA pour lancer l'application:" -ForegroundColor Yellow
    Write-Host "  -> Ouvrez le projet dans IntelliJ IDEA" -ForegroundColor Gray
    Write-Host "  -> Lancez Controllers/FinTrackApp.java" -ForegroundColor Gray
}

Write-Host ""
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host ""
pause
