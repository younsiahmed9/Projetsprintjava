@echo off
REM Script de configuration Git et push

cd /d C:\Users\MSI\IdeaProjects\fintrack-gestion-documents

REM Initialiser le dépôt Git
echo Initialisation du dépôt Git...
git init

REM Ajouter la configuration utilisateur si nécessaire
git config --global user.name "Ahmed Younsi"
git config --global user.email "younsiahmed9@gmail.com"

REM Ajouter tous les fichiers
echo Ajout des fichiers...
git add .

REM Créer le commit initial
echo Création du commit...
git commit -m "Restauration complète - Services OCR, Doublons, Echéances, Traduction"

REM Ajouter le remote
echo Ajout du dépôt distant...
git remote add origin https://github.com/younsiahmed9/Projetsprintjava.git

REM Renommer la branche master en main
git branch -M main

REM Push vers GitHub
echo Push vers GitHub...
git push -u origin main --force

echo Terminé!
pause

