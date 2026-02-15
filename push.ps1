#!/usr/bin/env powershell

# Script PowerShell pour pousser FinTrack vers GitHub
# Utilisation: .\push.ps1

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  FinTrack - PUSH VERS GITHUB" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Aller au dossier du projet
Set-Location -Path "C:\Users\MSI\IdeaProjects\fintrack-gestion-documents"
Write-Host "Dossier: $(Get-Location)" -ForegroundColor Green

# Vérifier que Git est installé
try {
    $gitVersion = git --version 2>&1
    Write-Host "✓ Git trouvé: $gitVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ ERREUR: Git n'est pas installé!" -ForegroundColor Red
    Write-Host "Téléchargez-le depuis: https://git-scm.com/download/win" -ForegroundColor Yellow
    Read-Host "Appuyez sur Entrée pour continuer"
    exit 1
}

Write-Host ""

# Initialiser le dépôt si nécessaire
if (-not (Test-Path ".\.git")) {
    Write-Host "Initialisation du dépôt Git..." -ForegroundColor Cyan
    git init
    Write-Host "✓ Dépôt initialisé" -ForegroundColor Green
    Write-Host ""
}

# Configurer l'utilisateur
Write-Host "Configuration de Git..." -ForegroundColor Cyan
git config user.name "Ahmed Younsi"
git config user.email "younsi@example.com"
Write-Host "✓ Configuration terminée" -ForegroundColor Green
Write-Host ""

# Ajouter tous les fichiers
Write-Host "Ajout de tous les fichiers..." -ForegroundColor Cyan
git add .
Write-Host "✓ Fichiers ajoutés" -ForegroundColor Green
Write-Host ""

# Vérifier s'il y a des changements
$status = git status --porcelain
if ($status.Length -eq 0) {
    Write-Host "Aucun changement à commiter." -ForegroundColor Yellow
    Read-Host "Appuyez sur Entrée pour continuer"
    exit 0
}

# Créer un commit
Write-Host "Entrez le message de commit:" -ForegroundColor Yellow
$message = Read-Host "Message (défaut: Initial commit)"
if ([string]::IsNullOrWhiteSpace($message)) {
    $message = "Initial commit: FinTrack - Gestion Documents v1.0"
}

Write-Host "Création du commit: '$message'" -ForegroundColor Cyan
git commit -m "$message"
Write-Host "✓ Commit créé" -ForegroundColor Green
Write-Host ""

# Ajouter le remote origin
Write-Host "Configuration du remote GitHub..." -ForegroundColor Cyan
git remote remove origin 2>$null
git remote add origin "https://github.com/younsiahmed9/Projetsprintjava.git"
Write-Host "✓ Remote configuré" -ForegroundColor Green
Write-Host ""

# Renommer la branche en main
Write-Host "Préparation de la branche..." -ForegroundColor Cyan
git branch -M main
Write-Host "✓ Branche renommée en 'main'" -ForegroundColor Green
Write-Host ""

# Pousser vers GitHub
Write-Host "Envoi vers GitHub..." -ForegroundColor Cyan
Write-Host "(Cette opération peut demander votre authentification)" -ForegroundColor Yellow
Write-Host ""

git push -u origin main

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "================================================" -ForegroundColor Green
    Write-Host "✓ SUCCÈS! Projet poussé vers GitHub!" -ForegroundColor Green
    Write-Host "URL: https://github.com/younsiahmed9/Projetsprintjava" -ForegroundColor Cyan
    Write-Host "================================================" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "================================================" -ForegroundColor Red
    Write-Host "✗ ERREUR lors du push!" -ForegroundColor Red
    Write-Host "Vérifiez votre authentification GitHub" -ForegroundColor Yellow
    Write-Host "================================================" -ForegroundColor Red
}

Write-Host ""
Read-Host "Appuyez sur Entrée pour terminer"

