# 📤 GUIDE - PUSH DU PROJET VERS GITHUB

## ⚠️ Prérequis

Avant de commencer, assurez-vous d'avoir :
- ✅ **Git** installé ([https://git-scm.com/download](https://git-scm.com/download))
- ✅ **GitHub CLI** ou accès terminal (PowerShell/CMD)
- ✅ **Authentification GitHub** configurée (token ou SSH)

---

## 🚀 Étapes pour Pousser le Projet

### Étape 1️⃣ : Installer Git (si nécessaire)

**Windows:**
```bash
# Télécharger et installer depuis https://git-scm.com/download/win
# Ou avec Chocolatey:
choco install git
```

**Vérifier l'installation:**
```bash
git --version
```

---

### Étape 2️⃣ : Configurer Git

```bash
# Configurer votre identité globalement
git config --global user.name "Ahmed Younsi"
git config --global user.email "younsi@example.com"

# Vérifier la configuration
git config --list
```

---

### Étape 3️⃣ : Initialiser le Dépôt Local

```bash
# Aller au dossier du projet
cd C:\Users\MSI\IdeaProjects\fintrack-gestion-documents

# Initialiser le dépôt Git
git init

# Ajouter tous les fichiers
git add .

# Créer le premier commit
git commit -m "Initial commit: FinTrack - Gestion Documents v1.0"
```

---

### Étape 4️⃣ : Ajouter le Dépôt Distant

```bash
# Ajouter votre dépôt GitHub distant
git remote add origin https://github.com/younsiahmed9/Projetsprintjava.git

# Vérifier que le remote est ajouté
git remote -v
```

---

### Étape 5️⃣ : Pousser vers GitHub

**Option A: HTTPS (avec authentification token)**
```bash
# Pousser la branche main
git push -u origin main

# Si la branche n'existe pas, créer et pousser:
git branch -M main
git push -u origin main
```

**Option B: SSH (plus sécurisé)**
```bash
# Configuration SSH préalablement faite
git push -u origin main
```

---

## 📋 Commandes Complètes - Copier/Coller

```bash
# 1. Aller au dossier
cd C:\Users\MSI\IdeaProjects\fintrack-gestion-documents

# 2. Initialiser Git
git init

# 3. Configurer Git (une seule fois)
git config --global user.name "Ahmed Younsi"
git config --global user.email "younsi@example.com"

# 4. Ajouter tous les fichiers
git add .

# 5. Premier commit
git commit -m "Initial commit: FinTrack - Gestion Documents avec Suppression Professionnelle et Budget"

# 6. Ajouter le remote
git remote add origin https://github.com/younsiahmed9/Projetsprintjava.git

# 7. Pousser vers GitHub
git branch -M main
git push -u origin main
```

---

## 🔐 Authentification GitHub

### Par HTTPS (Token):
1. Aller sur **GitHub Settings → Developer settings → Personal access tokens**
2. Créer un nouveau token avec la permission `repo`
3. Copier le token
4. Quand Git demande le mot de passe, coller le token

### Par SSH:
1. Générer une clé SSH :
```bash
ssh-keygen -t ed25519 -C "younsi@example.com"
```
2. Ajouter la clé publique à GitHub Settings → SSH Keys
3. Utiliser `git@github.com:younsiahmed9/Projetsprintjava.git` comme URL

---

## ✅ Vérifier que le Push a Réussi

```bash
# Voir l'historique des commits
git log

# Voir les remotes configurés
git remote -v

# Vérifier le statut
git status
```

---

## 📊 Contenu qui Sera Poussé

### Code Source
```
src/main/java/
├── Controllers/
│   ├── AlertUtils.java
│   ├── CategorieController.java
│   ├── CategorieListCell.java
│   ├── CategoryManagerDialog.java
│   ├── ContextMenuBuilder.java
│   ├── DocumentCardListCell.java
│   ├── DocumentController.java
│   ├── DocumentFolderListCell.java
│   ├── DocumentListCell.java
│   ├── DocumentViewController.java
│   ├── DossierController.java
│   ├── DossierListCell.java
│   ├── MainController.java
│   └── Dialogs/
│       └── CrudDialogManager.java
│       └── DeleteConfirmationDialog.java
├── Models/
│   ├── Categorie.java
│   ├── Document.java
│   └── Dossier.java
├── Services/
│   ├── Iservice.java
│   ├── ServiceCategorie.java
│   ├── ServiceDocument.java
│   └── ServiceDossier.java
└── utils/
    ├── MyDatabase.java
    └── ValidationUtils.java

src/main/resources/
├── db.properties
├── assets/
├── css/
├── fxml/
└── images/
```

### Configuration
```
pom.xml (Maven)
add_budget_column.sql (Script SQL)
fintrack.sql (Base de données)
```

### Documentation
```
*.md files (guides, tutoriels, documentation)
```

---

## 🎯 Fichier .gitignore (Optionnel)

Créer un fichier `.gitignore` à la racine :

```
# IDE
.idea/
*.iml
.vscode/
*.code-workspace

# Maven
target/
*.jar
*.war
*.ear

# Database
*.db
*.sqlite

# OS
.DS_Store
Thumbs.db

# Logs
*.log

# Build
.gradle/
build/
```

---

## 📝 Messages de Commit Utiles

```bash
# Premier commit
git commit -m "Initial commit: FinTrack v1.0 - Gestion Documents"

# Après modifications
git commit -m "feat: Ajouter suppression professionnelle"
git commit -m "feat: Ajouter budget financier aux documents"
git commit -m "fix: Corriger CategoryManagerDialog"
git commit -m "docs: Ajouter documentation complète"
```

---

## ❌ Problèmes Courants

### "Git not found"
**Solution:** Installer Git depuis https://git-scm.com/download/win

### "Authentification échouée"
**Solution:** 
- Vérifier votre token GitHub
- Ou utiliser SSH au lieu de HTTPS

### "Remote already exists"
**Solution:**
```bash
git remote remove origin
git remote add origin https://github.com/younsiahmed9/Projetsprintjava.git
```

### "Branch master vs main"
**Solution:**
```bash
git branch -M main
git push -u origin main
```

---

## ✨ Après le Premier Push

### Futurs Commits (c'est plus rapide):
```bash
# Ajouter les modifications
git add .

# Créer un commit
git commit -m "votre message"

# Pousser
git push
```

---

## 🎉 Résultat Final

Une fois le push réussi, vous verrez votre projet sur GitHub à :
```
https://github.com/younsiahmed9/Projetsprintjava
```

Tous les fichiers, commits et l'historique seront disponibles ! 🚀

---

**Date**: 15/02/2026  
**Projet**: FinTrack - Gestion Documents v1.0  
**Status**: ✅ Prêt pour GitHub

