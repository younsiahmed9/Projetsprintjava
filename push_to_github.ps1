#!/usr/bin/env pwsh
# Script de configuration et push vers GitHub
# Ce script initialise le dépôt Git et pousse tous les fichiers vers GitHub

$projectPath = "C:\Users\MSI\IdeaProjects\fintrack-gestion-documents"
$remoteUrl = "https://github.com/younsiahmed9/Projetsprintjava.git"
$branchName = "gestion_document"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "FinTrack - Configuration Git et Push" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier que nous sommes dans le bon dossier
cd $projectPath
Write-Host "📍 Dossier actuel: $projectPath" -ForegroundColor Yellow
Write-Host ""

# Étape 1: Vérifier l'état de Git
Write-Host "📋 Vérification de l'état Git..." -ForegroundColor Cyan
$isGitRepo = Test-Path -Path ".git" -PathType Container
if ($isGitRepo) {
    Write-Host "✅ Dépôt Git détecté" -ForegroundColor Green
    git status
} else {
    Write-Host "❌ Aucun dépôt Git trouvé, initialisation..." -ForegroundColor Yellow
    git init
    Write-Host "✅ Dépôt Git initialisé" -ForegroundColor Green
}
Write-Host ""

# Étape 2: Configuration utilisateur Git
Write-Host "🔧 Configuration utilisateur Git..." -ForegroundColor Cyan
git config --global user.name "Ahmed Younsi" 2>$null
git config --global user.email "younsiahmed9@gmail.com" 2>$null
Write-Host "✅ Configuration utilisateur définie" -ForegroundColor Green
Write-Host ""

# Étape 3: Ajouter les fichiers
Write-Host "📦 Ajout des fichiers au staging..." -ForegroundColor Cyan
git add .
$stagedCount = (git diff --cached --name-only).Count
Write-Host "✅ $stagedCount fichier(s) en attente de commit" -ForegroundColor Green
Write-Host ""

# Étape 4: Créer le commit
Write-Host "💾 Création du commit..." -ForegroundColor Cyan
git commit -m "Restauration complète - Services OCR, Doublons, Echéances, Traduction + Database SQL"
Write-Host "✅ Commit créé avec succès" -ForegroundColor Green
Write-Host ""

# Étape 5: Vérifier le remote
Write-Host "🌐 Vérification du remote..." -ForegroundColor Cyan
$remoteExists = git remote -v | Select-String "origin"
if ($remoteExists) {
    Write-Host "✅ Remote 'origin' détecté, suppression..." -ForegroundColor Green
    git remote remove origin
} else {
    Write-Host "ℹ️  Aucun remote 'origin' détecté" -ForegroundColor Yellow
}
Write-Host ""

# Étape 6: Ajouter le remote
Write-Host "🔗 Ajout du remote GitHub..." -ForegroundColor Cyan
git remote add origin $remoteUrl
Write-Host "✅ Remote ajouté: $remoteUrl" -ForegroundColor Green
Write-Host ""

# Étape 7: Renommer la branche
Write-Host "📌 Renommage de la branche..." -ForegroundColor Cyan
git branch -M $branchName
Write-Host "✅ Branche renommée en: $branchName" -ForegroundColor Green
Write-Host ""

# Étape 8: Push
Write-Host "🚀 Push vers GitHub..." -ForegroundColor Cyan
Write-Host "⚠️  Cela nécessite une authentification GitHub..." -ForegroundColor Yellow
git push -u origin $branchName --force
Write-Host "✅ Push terminé!" -ForegroundColor Green
Write-Host ""

# Résumé
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "✨ Configuration Git terminée!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Résumé:" -ForegroundColor Yellow
Write-Host "  • Dépôt: $remoteUrl"
Write-Host "  • Branche: $branchName"
Write-Host "  • Dossier: $projectPath"
Write-Host ""
Write-Host "Prochaines étapes:" -ForegroundColor Yellow
Write-Host "  1. Vérifiez le push sur GitHub"
Write-Host "  2. Installez les dépendances: mvn clean install"
Write-Host "  3. Démarrez l'app: mvn javafx:run"
Write-Host ""

Read-Host "Appuyez sur Enter pour fermer ce script"

