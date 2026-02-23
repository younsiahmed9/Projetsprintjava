# Script PowerShell pour appliquer la migration de la base de données
# Ajoute la colonne fingerprint_template à la table users

Write-Host "=== Migration Base de Données - Empreinte Digitale ===" -ForegroundColor Cyan
Write-Host ""

# Configuration - Modifiez ces valeurs selon votre configuration MySQL
$mysqlHost = "localhost"
$mysqlPort = "3306"
$mysqlUser = "root"
$mysqlPassword = ""  # Mot de passe vide par défaut pour XAMPP
$database = "fintrack"

# Chemin vers mysql.exe (XAMPP par défaut)
$mysqlPath = "C:\xampp\mysql\bin\mysql.exe"

# Vérifier si mysql.exe existe
if (-not (Test-Path $mysqlPath)) {
    Write-Host "ERREUR: mysql.exe non trouvé à $mysqlPath" -ForegroundColor Red
    Write-Host "Veuillez modifier le chemin dans ce script ou installer XAMPP" -ForegroundColor Yellow
    exit 1
}

Write-Host "Configuration:" -ForegroundColor Green
Write-Host "  Host: $mysqlHost"
Write-Host "  Port: $mysqlPort"
Write-Host "  User: $mysqlUser"
Write-Host "  Database: $database"
Write-Host ""

# Requête SQL
$sqlQuery = @"
USE $database;

-- Ajouter la colonne fingerprint_template si elle n'existe pas
ALTER TABLE users
ADD COLUMN IF NOT EXISTS fingerprint_template MEDIUMBLOB NULL
AFTER profile_photo;

-- Vérifier le résultat
DESCRIBE users;
"@

Write-Host "Exécution de la migration..." -ForegroundColor Yellow

# Construire la commande mysql
$arguments = @(
    "-h", $mysqlHost,
    "-P", $mysqlPort,
    "-u", $mysqlUser
)

if ($mysqlPassword -ne "") {
    $arguments += "-p$mysqlPassword"
}

$arguments += "-e", $sqlQuery

try {
    # Exécuter la commande
    & $mysqlPath $arguments

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "=== Migration réussie! ===" -ForegroundColor Green
        Write-Host "La colonne fingerprint_template a été ajoutée à la table users." -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "ERREUR: La migration a échoué (code: $LASTEXITCODE)" -ForegroundColor Red
    }
} catch {
    Write-Host ""
    Write-Host "ERREUR: $_" -ForegroundColor Red
    Write-Host "Vérifiez que MySQL est démarré et que la base de données existe." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Appuyez sur une touche pour continuer..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
