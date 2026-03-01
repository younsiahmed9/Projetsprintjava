#!/usr/bin/env pwsh
# ============================================
# FINTRACK - PUSH AUTOMATISÉ VERS GITHUB
# ============================================
# Ce script effectue un push complet du projet restauré vers GitHub

$projectPath = "C:\Users\MSI\IdeaProjects\fintrack-gestion-documents"
$remoteUrl = "https://github.com/younsiahmed9/Projetsprintjava.git"
$branchName = "gestion_document"
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

Write-Host ""
Write-Host "╔════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   FinTrack - Push Automatisé vers GitHub  ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "🚀 Démarrage: $timestamp" -ForegroundColor Yellow
Write-Host "📍 Localisation: $projectPath" -ForegroundColor Yellow
Write-Host "🌐 Remote: $remoteUrl" -ForegroundColor Yellow
Write-Host "📌 Branche: $branchName" -ForegroundColor Yellow
Write-Host ""

cd $projectPath

# ============================================
# ÉTAPE 1: Vérifier l'état Git
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "📋 ÉTAPE 1: Vérification de l'état Git" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

$isGitRepo = Test-Path -Path ".git" -PathType Container
if ($isGitRepo) {
    Write-Host "✅ Dépôt Git trouvé" -ForegroundColor Green
    $status = git status --short
    if ($status) {
        Write-Host "   Fichiers en attente: $($status.Count)" -ForegroundColor Yellow
    } else {
        Write-Host "   Tous les fichiers sont à jour" -ForegroundColor Green
    }
} else {
    Write-Host "⚠️  Aucun dépôt Git, initialisation..." -ForegroundColor Yellow
    git init
    Write-Host "✅ Dépôt initialisé" -ForegroundColor Green
}
Write-Host ""

# ============================================
# ÉTAPE 2: Configuration utilisateur
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🔧 ÉTAPE 2: Configuration utilisateur Git" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

git config --global user.name "Ahmed Younsi" 2>$null
git config --global user.email "younsiahmed9@gmail.com" 2>$null
$userName = git config --global user.name
$userEmail = git config --global user.email
Write-Host "✅ Utilisateur: $userName <$userEmail>" -ForegroundColor Green
Write-Host ""

# ============================================
# ÉTAPE 3: Ajouter les fichiers
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "📦 ÉTAPE 3: Ajout des fichiers au staging" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

git add .
$stagedFiles = git diff --cached --name-only
$count = $stagedFiles.Count
Write-Host "✅ $count fichier(s) en attente de commit" -ForegroundColor Green
Write-Host ""

# Afficher un aperçu des fichiers
if ($stagedFiles) {
    Write-Host "📄 Fichiers à pousser:" -ForegroundColor Yellow
    $stagedFiles | ForEach-Object { Write-Host "   • $_" -ForegroundColor Gray }
    Write-Host ""
}

# ============================================
# ÉTAPE 4: Créer le commit
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "💾 ÉTAPE 4: Création du commit" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

$commitMessage = "Restauration complète - Services OCR, Doublons, Échéances, Traduction + Database"
git commit -m $commitMessage 2>&1 | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Commit créé avec succès" -ForegroundColor Green
    Write-Host "   Message: $commitMessage" -ForegroundColor Gray
} else {
    Write-Host "ℹ️  Aucune modification à committer" -ForegroundColor Yellow
}
Write-Host ""

# ============================================
# ÉTAPE 5: Configurer le remote
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🌐 ÉTAPE 5: Configuration du remote" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

$existingRemote = git remote -v | Select-String "origin"
if ($existingRemote) {
    Write-Host "ℹ️  Remote 'origin' existe, suppression..." -ForegroundColor Yellow
    git remote remove origin 2>$null
    Write-Host "✅ Remote supprimé" -ForegroundColor Green
}

git remote add origin $remoteUrl
Write-Host "✅ Remote ajouté: $remoteUrl" -ForegroundColor Green
Write-Host ""

# ============================================
# ÉTAPE 6: Renommer la branche
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "📌 ÉTAPE 6: Configuration de la branche" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

$currentBranch = git rev-parse --abbrev-ref HEAD
Write-Host "   Branche actuelle: $currentBranch" -ForegroundColor Gray

git branch -M $branchName 2>$null
Write-Host "✅ Branche renommée en: $branchName" -ForegroundColor Green
Write-Host ""

# ============================================
# ÉTAPE 7: Push vers GitHub
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🚀 ÉTAPE 7: Push vers GitHub" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚠️  AUTHENTIFICATION REQUISE!" -ForegroundColor Yellow
Write-Host "   Vous allez être invité à saisir vos credentials GitHub" -ForegroundColor Yellow
Write-Host ""

git push -u origin $branchName --force
if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Push terminé avec succès!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "❌ Erreur lors du push" -ForegroundColor Red
    Write-Host "   Vérifiez votre connexion Internet et vos credentials GitHub" -ForegroundColor Red
}
Write-Host ""

# ============================================
# RÉSUMÉ FINAL
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "✨ RÉSUMÉ FINAL" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""
Write-Host "🎉 CONFIGURATION FINALISÉE!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Informations de déploiement:" -ForegroundColor Yellow
Write-Host "   • Dépôt: $remoteUrl" -ForegroundColor Gray
Write-Host "   • Branche: $branchName" -ForegroundColor Gray
Write-Host "   • Localisation: $projectPath" -ForegroundColor Gray
Write-Host "   • Timestamp: $timestamp" -ForegroundColor Gray
Write-Host ""
Write-Host "📝 Prochaines étapes:" -ForegroundColor Yellow
Write-Host "   1. Consulter: https://github.com/younsiahmed9/Projetsprintjava" -ForegroundColor Gray
Write-Host "   2. Vérifier le push sur la branche: $branchName" -ForegroundColor Gray
Write-Host "   3. Lire la documentation:" -ForegroundColor Gray
Write-Host "      • RESTORATION_COMPLETE.md - État de restauration" -ForegroundColor Gray
Write-Host "      • README_SERVICES.md - Documentation technique" -ForegroundColor Gray
Write-Host "      • CHANGELOG.md - Détails des modifications" -ForegroundColor Gray
Write-Host ""
Write-Host "🚀 Pour lancer l'application:" -ForegroundColor Yellow
Write-Host "   mvn clean javafx:run" -ForegroundColor Gray
Write-Host ""

$endTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
Write-Host "⏱️  Fin: $endTime" -ForegroundColor Yellow
Write-Host ""
Write-Host "╔════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║         Push Automatisé Terminé!          ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Attendre la confirmation de l'utilisateur
Read-Host "Appuyez sur [Entrée] pour fermer ce script"

