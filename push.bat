@echo off
REM Script pour pousser FinTrack vers GitHub automatiquement
REM Utilisation: push.bat

echo ================================================
echo   FinTrack - PUSH VERS GITHUB
echo ================================================
echo.

REM Aller au dossier du projet
cd /d C:\Users\MSI\IdeaProjects\fintrack-gestion-documents

REM Vérifier que Git est installé
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERREUR: Git n'est pas installé!
    echo Téléchargez-le depuis: https://git-scm.com/download/win
    pause
    exit /b 1
)

echo ✓ Git trouvé
echo.

REM Initialiser le dépôt si nécessaire
if not exist ".git" (
    echo Initialisation du dépôt Git...
    git init
    echo ✓ Dépôt initialisé
    echo.
)

REM Configurer l'utilisateur
echo Configuration de Git...
git config user.name "Ahmed Younsi"
git config user.email "younsi@example.com"
echo ✓ Configuration terminée
echo.

REM Ajouter tous les fichiers
echo Ajout de tous les fichiers...
git add .
echo ✓ Fichiers ajoutés
echo.

REM Vérifier s'il y a des changements
git diff --cached --quiet
if %errorlevel% equ 0 (
    echo Aucun changement à commiter.
    pause
    exit /b 0
)

REM Créer un commit
set /p message="Entrez le message de commit (défaut: Initial commit): "
if "%message%"=="" (
    set message=Initial commit: FinTrack - Gestion Documents v1.0
)

echo Création du commit: "%message%"
git commit -m "%message%"
echo ✓ Commit créé
echo.

REM Ajouter le remote origin
git remote remove origin >nul 2>&1
echo Configuration du remote GitHub...
git remote add origin https://github.com/younsiahmed9/Projetsprintjava.git
echo ✓ Remote configuré
echo.

REM Renommer la branche en main
echo Préparation de la branche...
git branch -M main
echo ✓ Branche renommée en 'main'
echo.

REM Pousser vers GitHub
echo Envoi vers GitHub (cette opération peut demander votre authentification)...
git push -u origin main

if %errorlevel% equ 0 (
    echo.
    echo ================================================
    echo   ✓ SUCCÈS! Projet poussé vers GitHub!
    echo   URL: https://github.com/younsiahmed9/Projetsprintjava
    echo ================================================
) else (
    echo.
    echo ================================================
    echo   ✗ ERREUR lors du push!
    echo   Vérifiez votre authentification GitHub
    echo ================================================
)

echo.
pause

