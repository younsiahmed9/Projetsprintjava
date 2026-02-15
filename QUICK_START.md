# 🚀 Guide de Lancement - FinTrack

## ⚡ Lancement Rapide (5 minutes)

### Étape 1 : Télécharger JavaFX SDK (si pas installé)

1. Allez sur : https://gluonhq.com/products/javafx/
2. Téléchargez **JavaFX SDK 17+** (Windows)
3. Décompressez dans : `C:\javafx-sdk-17\`

### Étape 2 : Compiler le projet via IntelliJ

1. Ouvrez **IntelliJ IDEA**
2. Ouvrez le dossier du projet : `C:\Users\Mega-PC\IdeaProjects\FinTrack`
3. Attendez que l'IDE finisse l'indexation
4. Cliquez sur **Build → Build Project** (ou Ctrl+F9)

### Étape 3 : Lancer l'application

#### Option A : Double-cliquez sur `run.bat` 
```
C:\Users\Mega-PC\IdeaProjects\FinTrack\run.bat
```
✅ **C'est la méthode la plus simple !**

#### Option B : Via IntelliJ
1. Ouvrez `src/main/java/Test/MainFx.java`
2. Cliquez sur le ▶️ vert à côté de `public class MainFx`
3. Sélectionnez **"Run 'MainFx'"**

#### Option C : Via PowerShell
```powershell
cd C:\Users\Mega-PC\IdeaProjects\FinTrack
.\run.ps1
```

---

## 📋 Vérifier l'installation

### ✅ Checklist avant de lancer :

- [ ] JDK 17+ installé (`java -version` dans cmd)
- [ ] JavaFX SDK 17 téléchargé dans `C:\javafx-sdk-17\`
- [ ] Projet compilé via IntelliJ (dossier `target/classes` existe)
- [ ] `JAVA_HOME` configuré (cherchez "Variables d'environnement" dans Windows)

### 🔍 Vérifier JAVA_HOME

```powershell
$env:JAVA_HOME
# Devrait afficher : C:\Program Files\...\jdk-17 (ou similaire)
```

Si rien n'apparaît :
1. **Panneau de configuration → Variables d'environnement**
2. Créez une nouvelle variable : `JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.x.x`

---

## 🎯 Interface CRUD

Après le lancement, vous verrez :

```
┌─────────────────────────────────────┐
│  Gestion des Dépenses               │
│                                     │
│  [➕ Ajouter] [🗑️ Supprimer] [⇄ Modifier]  │
│                                     │
│  ┌──────────────────────────────┐   │
│  │  Formulaire ou Recherche      │   │
│  │  ...                          │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 🎮 Fonctionnalités :

| Bouton | Action |
|--------|--------|
| **➕ Ajouter** | Ouvre un formulaire pour créer une nouvelle dépense |
| **🗑️ Supprimer** | Recherche par ID et supprime une dépense |
| **⇄ Modifier** | Recherche par ID et modifie une dépense |

---

## 🐛 Dépannage

### ❌ Erreur : "JAVA_HOME n'est pas défini"

**Solution :**
```powershell
# Définir temporairement
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.7"

# Ou définir de façon permanente dans les variables d'environnement Windows
```

### ❌ Erreur : "JavaFX SDK non trouvé"

**Solution :**
1. Téléchargez depuis https://gluonhq.com/products/javafx/
2. Décompressez dans `C:\javafx-sdk-17\`
3. Relancez `run.bat`

### ❌ Erreur : "target/classes n'existe pas"

**Solution :**
1. Ouvrez le projet dans IntelliJ
2. Cliquez sur **Build → Build Project**
3. Attendez la fin de la compilation
4. Relancez `run.bat`

### ❌ Erreur : "Cannot find symbol"

**Solution :**
1. Dans IntelliJ, allez sur **File → Invalidate Caches...**
2. Cochez "Invalidate and Restart"
3. Relancez la compilation

---

## 📞 Support

Si vous rencontrez un problème :

1. Vérifiez la section **Dépannage** ci-dessus
2. Essayez via IntelliJ plutôt que via `run.bat`
3. Vérifiez que tous les dossiers existent : `target/classes`, `C:\javafx-sdk-17\lib`

---

## ✨ Résumé

**3 étapes simples :**
1. ✅ Télécharger JavaFX SDK dans `C:\javafx-sdk-17\`
2. ✅ Compiler via IntelliJ (**Build → Build Project**)
3. ✅ Lancer via **double-clic sur `run.bat`** ou IntelliJ

C'est tout ! 🎉

