# 📚 Index de Documentation - FinTrack

Bienvenue dans la documentation de **FinTrack** ! Utilisez ce guide pour trouver ce dont vous avez besoin.

---

## 🚀 Je Veux Lancer l'Application MAINTENANT

### ⚡ 5 Minutes (Débutant)
👉 Lire : **[QUICK_START.md](./QUICK_START.md)**

**Résumé :**
1. Télécharger JavaFX SDK
2. Compiler dans IntelliJ
3. Double-cliquer sur `run.bat`

---

## 🔨 Je Veux Compiler le Projet

### Avec IntelliJ (Recommandé)
👉 Lire : **[COMPILATION_GUIDE.md](./COMPILATION_GUIDE.md)**

**Étapes :**
- Configurer le SDK Java
- Ajouter JavaFX
- Build → Build Project

### Avec Maven (Avancé)
👉 Voir la section "Commandes Maven" dans [COMPILATION_GUIDE.md](./COMPILATION_GUIDE.md)

---

## 🎮 J'ai une Erreur au Lancement

### Erreur JavaFX
👉 Lire : **[JAVAFX_SETUP.md](./JAVAFX_SETUP.md)**

### Erreur Générale
👉 Voir la section "Dépannage" dans **[QUICK_START.md](./QUICK_START.md)**

### Erreur Spécifique Maven
👉 Lire : **[LAUNCH_GUIDE.md](./LAUNCH_GUIDE.md)** → Section "Dépannage"

---

## 📖 Je Veux Comprendre l'Architecture

👉 Lire : **[PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md)**

**Contient :**
- Arborescence complète du projet
- Explication des fichiers clés
- Modèle de données
- Architecture MVC
- Technologies utilisées

---

## 📋 Je Veux Voir ce qui a été Réalisé

👉 Lire : **[SUMMARY.md](./SUMMARY.md)**

**Contient :**
- Résumé des livrables
- Fonctionnalités implémentées
- Statistiques du projet
- Conseils finaux

---

## 🎯 Je Veux Savoir Comment Utiliser l'Appli

👉 Lire : **[README_CRUD.md](./README_CRUD.md)**

**Contient :**
- Structure de l'interface
- Fonctionnement de chaque bouton
- Mode d'emploi
- Navigation

---

## 🔧 Je Veux Configurer Manuellement

👉 Lire : **[LAUNCH_GUIDE.md](./LAUNCH_GUIDE.md)**

**Contient :**
- Options de lancement détaillées
- Configuration IntelliJ avancée
- Commandes Maven/PowerShell

---

## 📊 Aide Rapide par Sujet

| Besoin | Fichier | Durée |
|--------|---------|-------|
| Lancer l'app | QUICK_START.md | 5 min |
| Compiler | COMPILATION_GUIDE.md | 10 min |
| Comprendre | PROJECT_STRUCTURE.md | 15 min |
| Dépanner | QUICK_START.md (FAQ) | 5 min |
| Utiliser | README_CRUD.md | 5 min |
| Vue d'ensemble | SUMMARY.md | 10 min |

---

## 🗂️ Plan de Lecture Recommandé

### Pour Débutant :
1. **QUICK_START.md** ← Commencez ici !
2. **README_CRUD.md** ← Comment utiliser l'appli
3. **SUMMARY.md** ← Voir ce qui a été fait

### Pour Développeur :
1. **QUICK_START.md** ← Démarrer l'appli
2. **PROJECT_STRUCTURE.md** ← Comprendre le code
3. **COMPILATION_GUIDE.md** ← Comment compiler
4. **LAUNCH_GUIDE.md** ← Options avancées

### Pour Admin Système :
1. **JAVAFX_SETUP.md** ← Configurer JavaFX
2. **COMPILATION_GUIDE.md** ← Configuration IntelliJ
3. **LAUNCH_GUIDE.md** ← Lancement en production

---

## 📞 Questions Fréquentes

### Q : Par où commencer ?
**R :** [QUICK_START.md](./QUICK_START.md)

### Q : Comment lancer l'appli ?
**R :** Double-cliquez sur `run.bat` (voir [QUICK_START.md](./QUICK_START.md))

### Q : Qu'est-ce qui a besoin de JavaFX SDK ?
**R :** L'interface graphique. À télécharger depuis https://gluonhq.com/products/javafx/

### Q : Est-ce que Maven est obligatoire ?
**R :** Non ! IntelliJ peut compiler sans Maven. Voir [COMPILATION_GUIDE.md](./COMPILATION_GUIDE.md)

### Q : Où est la base de données ?
**R :** MySQL, configurée dans `Services/ServiceDepense.java`

### Q : Puis-je modifier le code ?
**R :** Oui ! Tous les fichiers source sont dans `src/main/java/`

### Q : Comment ajouter une nouvelle fonctionnalité ?
**R :** Voir [PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md) → Architecture

---

## 🎁 Fichiers Fournis

```
📁 Documentation
├── 📄 INDEX.md                    ← Vous êtes ici !
├── 📄 QUICK_START.md              ← Démarrer en 5 min
├── 📄 COMPILATION_GUIDE.md        ← Comment compiler
├── 📄 JAVAFX_SETUP.md             ← Configuration JavaFX
├── 📄 LAUNCH_GUIDE.md             ← Options de lancement
├── 📄 PROJECT_STRUCTURE.md        ← Architecture du projet
├── 📄 README_CRUD.md              ← Comment utiliser
└── 📄 SUMMARY.md                  ← Résumé du projet

🎯 Scripts de Lancement
├── 🔧 run.bat                     ← Windows (RECOMMANDÉ)
├── 🔧 run.ps1                     ← PowerShell
└── 🔧 run.sh                      ← Linux/Mac

⚙️ Configuration
└── 📄 pom.xml                     ← Maven (optionnel)
```

---

## 🌟 Conseil Final

**Commencez par ceci :**

1. Lisez **[QUICK_START.md](./QUICK_START.md)** (5 min)
2. Lancez l'appli via **`run.bat`**
3. Testez les 3 boutons CRUD
4. Si erreur, lisez **[QUICK_START.md](./QUICK_START.md)** → Dépannage

---

## 📞 Support

Si vous êtes bloqué(e) :

1. **Cherchez dans le fichier approprié** (voir le tableau ci-dessus)
2. **Consultez la section Dépannage** de ce fichier
3. **Vérifiez que :**
   - JavaFX SDK est dans `C:\javafx-sdk-17\`
   - Le projet est compilé dans IntelliJ
   - JAVA_HOME est configuré

---

## ✅ Checklist Avant de Démarrer

- [ ] Java 17+ installé (`java -version`)
- [ ] JavaFX SDK 17 téléchargé
- [ ] IntelliJ IDEA ouvert avec le projet
- [ ] Projet compilé (`Build → Build Project`)
- [ ] `target/classes` existe
- [ ] Prêt à lancer !

🎉 **C'est bon ? Lancez l'appli !**

```bash
# Via run.bat
C:\Users\Mega-PC\IdeaProjects\FinTrack\run.bat

# Ou via IntelliJ
Shift+F10 (après sélection de MainFx.java)
```

---

Bonne chance ! 🚀

