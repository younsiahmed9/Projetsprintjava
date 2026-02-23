# Script PowerShell pour réinitialiser les empreintes dans la base de données
# Exécuter ce script pour supprimer toutes les empreintes enregistrées

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RÉINITIALISATION DES EMPREINTES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Ce script va supprimer TOUTES les empreintes digitales" -ForegroundColor Yellow
Write-Host "enregistrées dans la base de données." -ForegroundColor Yellow
Write-Host ""
Write-Host "Vous devrez réenregistrer vos empreintes après cette opération." -ForegroundColor Yellow
Write-Host ""

$confirm = Read-Host "Voulez-vous continuer? (O/N)"

if ($confirm -ne "O" -and $confirm -ne "o") {
    Write-Host "Opération annulée." -ForegroundColor Red
    exit
}

Write-Host ""
Write-Host "Connexion à MySQL..." -ForegroundColor Green

# Configuration de la base de données (à adapter selon votre configuration)
$mysqlPath = "C:\xampp\mysql\bin\mysql.exe"
$host = "localhost"
$username = "root"
$password = ""  # Mot de passe vide par défaut pour XAMPP
$database = "fintrack"

# Vérifier si MySQL est accessible
if (-not (Test-Path $mysqlPath)) {
    Write-Host "Erreur: MySQL n'est pas trouvé à $mysqlPath" -ForegroundColor Red
    Write-Host ""
    Write-Host "Solutions possibles:" -ForegroundColor Yellow
    Write-Host "1. Vérifiez que XAMPP est installé" -ForegroundColor Yellow
    Write-Host "2. Modifiez le chemin dans ce script" -ForegroundColor Yellow
    Write-Host "3. Exécutez le fichier reset-fingerprints.sql manuellement dans phpMyAdmin" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Pour phpMyAdmin:" -ForegroundColor Cyan
    Write-Host "1. Ouvrez http://localhost/phpmyadmin" -ForegroundColor White
    Write-Host "2. Sélectionnez la base 'fintrack'" -ForegroundColor White
    Write-Host "3. Cliquez sur 'SQL'" -ForegroundColor White
    Write-Host "4. Copiez-collez : UPDATE users SET fingerprint_template = NULL;" -ForegroundColor White
    Write-Host "5. Cliquez sur 'Exécuter'" -ForegroundColor White
    exit
}

# Commande SQL pour réinitialiser les empreintes
$sqlCommand = "UPDATE users SET fingerprint_template = NULL;"

try {
    # Exécuter la commande SQL
    $arguments = "-h$host -u$username"
    if ($password) {
        $arguments += " -p$password"
    }
    $arguments += " $database -e `"$sqlCommand`""

    Write-Host "Exécution de la commande SQL..." -ForegroundColor Green

    $process = Start-Process -FilePath $mysqlPath -ArgumentList $arguments -Wait -PassThru -NoNewWindow

    if ($process.ExitCode -eq 0) {
        Write-Host ""
        Write-Host "✅ SUCCÈS!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Toutes les empreintes ont été supprimées de la base de données." -ForegroundColor Green
        Write-Host ""
        Write-Host "PROCHAINES ÉTAPES:" -ForegroundColor Cyan
        Write-Host "1. Lancez l'application : .\mvnw.ps1 javafx:run" -ForegroundColor White
        Write-Host "2. Créez un nouveau compte OU connectez-vous avec un compte existant" -ForegroundColor White
        Write-Host "3. Allez dans 'Mon Profil' ou 'Inscription'" -ForegroundColor White
        Write-Host "4. Cliquez sur 'Enregistrer empreinte'" -ForegroundColor White
        Write-Host "5. Suivez le compte à rebours : 3... 2... 1..." -ForegroundColor White
        Write-Host "6. Posez votre doigt sur le lecteur HP ProBook" -ForegroundColor White
        Write-Host "7. Testez la connexion par empreinte!" -ForegroundColor White
        Write-Host ""
    } else {
        Write-Host "❌ ERREUR lors de l'exécution de la commande SQL" -ForegroundColor Red
        Write-Host ""
        Write-Host "Utilisez plutôt phpMyAdmin:" -ForegroundColor Yellow
        Write-Host "http://localhost/phpmyadmin" -ForegroundColor Cyan
    }
} catch {
    Write-Host "❌ ERREUR: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "SOLUTION MANUELLE - Utilisez phpMyAdmin:" -ForegroundColor Yellow
    Write-Host "1. Ouvrez http://localhost/phpmyadmin" -ForegroundColor White
    Write-Host "2. Sélectionnez la base 'fintrack'" -ForegroundColor White
    Write-Host "3. Cliquez sur l'onglet 'SQL'" -ForegroundColor White
    Write-Host "4. Exécutez cette commande:" -ForegroundColor White
    Write-Host "   UPDATE users SET fingerprint_template = NULL;" -ForegroundColor Cyan
    Write-Host ""
}

Write-Host ""
Write-Host "Appuyez sur une touche pour quitter..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
