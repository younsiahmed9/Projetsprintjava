# 🔨 Comment Compiler FinTrack via IntelliJ

## ✅ Étape 1 : Ouvrir le Projet

1. Ouvrez **IntelliJ IDEA**
2. Cliquez sur **File → Open**
3. Sélectionnez le dossier : `C:\Users\Mega-PC\IdeaProjects\FinTrack`
4. Cliquez **OK**
5. Attendez que l'IDE finisse l'indexation (messages en bas)

---

## ✅ Étape 2 : Configurer le SDK Java

1. Allez dans **File → Project Structure** (ou Ctrl+Alt+Shift+S)
2. Allez dans l'onglet **Project**
3. **SDK** : Vérifiez que c'est configuré sur **JDK 17 ou supérieur**
4. Si "No SDK" apparaît :
   - Cliquez sur **"Edit"**
   - Sélectionnez votre JDK 17 (généralement dans `Program Files\Eclipse Adoptium\jdk-17...`)
   - Cliquez **OK**

---

## ✅ Étape 3 : Configurer JavaFX (Important!)

### Option A : Ajouter comme Global Library (Recommandé)

1. **File → Project Structure → Global Libraries** (en bas à gauche)
2. Cliquez sur le **"+"** vert
3. Sélectionnez **Java**
4. Allez dans : `C:\javafx-sdk-17\lib`
5. Donnez-lui un nom : **JavaFX-17**
6. Cliquez **OK**
7. Cliquez sur votre projet dans la liste de gauche
8. Allez dans l'onglet **Modules**
9. Sélectionnez votre module (`FinTrack`)
10. Dans l'onglet **Dependencies**, cliquez **"+"** → **Library**
11. Sélectionnez **JavaFX-17** 
12. Cliquez **OK** et **Apply**

### Option B : Via VM Options (Plus simple)

1. **Run → Edit Configurations**
2. Créez une nouvelle configuration **Application** :
   - Name: `FinTrack`
   - Main class: `Test.MainFx`
   - VM options: `--module-path C:\javafx-sdk-17\lib --add-modules javafx.controls,javafx.fxml`
   - Working directory: `C:\Users\Mega-PC\IdeaProjects\FinTrack`
3. Cliquez **OK**

---

## ✅ Étape 4 : Compiler le Projet

### Méthode 1 : Via Menu
1. Cliquez sur **Build → Build Project** (ou Ctrl+F9)
2. Attendez la fin (voir "Build completed successfully" en bas)

### Méthode 2 : Via Raccourci
1. Appuyez sur **Ctrl+Shift+F9** (compile tous les fichiers)

### Méthode 3 : Via Maven (si disponible)
1. Cliquez sur **View → Tool Windows → Maven**
2. Développez **FinTrack**
3. Double-cliquez sur **Lifecycle → compile**

---

## 🎯 Vérifier la Compilation

Après la compilation, vous devez voir :
- ✅ "Build completed successfully" (en bas)
- ✅ Dossier **`target/classes`** existe
- ✅ Pas d'erreurs rouges en rouge

Si vous voyez des erreurs :
1. Cliquez sur **View → Tool Windows → Problems** (ou Build)
2. Lisez les erreurs
3. Corrigez selon les suggestions

---

## 🚀 Lancer Après Compilation

### Option 1 : Via IntelliJ
1. Ouvrez `src/main/java/Test/MainFx.java`
2. Cliquez sur le triangle ▶️ **vert** à côté de `public class MainFx`
3. Sélectionnez **"Run 'MainFx'"**

### Option 2 : Via run.bat
1. Allez dans l'explorateur Windows
2. Double-cliquez sur `C:\Users\Mega-PC\IdeaProjects\FinTrack\run.bat`

### Option 3 : Via Raccourci Clavier
1. Sélectionnez `MainFx.java`
2. Appuyez sur **Shift+F10** (Run)

---

## 🔧 Configuration Complète du Run

Si vous avez des erreurs JavaFX au lancement :

1. **Run → Edit Configurations**
2. Créez une config **Application**
3. Configurez comme suit :

```
┌─────────────────────────────────────────┐
│ Run Configuration                       │
├─────────────────────────────────────────┤
│ Name:          FinTrack                 │
│ Main class:    Test.MainFx              │
│ VM options:    --module-path \          │
│                C:\javafx-sdk-17\lib \   │
│                --add-modules \          │
│                javafx.controls,javafx.fxml
│ Program args:  (vide)                   │
│ Working dir:   C:\Users\Mega-PC\...     │
│ Use classpath: FinTrack (module)        │
│ Use module:    ✓ (coché)                │
└─────────────────────────────────────────┘
```

4. Cliquez **Apply** et **OK**
5. Lancez avec le bouton ▶️ vert

---

## 🐛 Troubleshooting

### ❌ "Cannot resolve symbol"
- **Solution :** File → Invalidate Caches → Restart
- Attendez la réindexation, puis relancez la compilation

### ❌ "Module not found"
- **Solution :** Vérifiez la configuration JavaFX (Étape 3)
- Assurez-vous que `C:\javafx-sdk-17\lib` existe

### ❌ "Build failed"
- **Solution :** 
  1. Cliquez sur les erreurs pour voir les détails
  2. Allez au fichier posant problème
  3. Corrigez les erreurs
  4. Relancez la compilation

### ❌ "Compilation stuck"
- **Solution :** 
  1. Cliquez sur **Build → Stop**
  2. Attendez 5 secondes
  3. Relancez la compilation

---

## ✨ Après Compilation Réussie

✅ **Vous pouvez maintenant :**
1. Lancer l'application via le bouton ▶️
2. Cliquer sur les boutons CRUD
3. Tester l'ajout, la modification, la suppression

---

## 📝 Commandes Maven Équivalentes (si Maven est installé)

```bash
# Compiler
mvn clean compile

# Compiler + lancer
mvn javafx:run

# Compiler + créer JAR
mvn package

# Nettoyer
mvn clean
```

Mais avec IntelliJ, **vous n'avez pas besoin de Maven** ! 🎉

---

## 💾 Fichier à Vérifier

Après compilation, assurez-vous que ce fichier existe :
```
C:\Users\Mega-PC\IdeaProjects\FinTrack\target\classes\Test\MainFx.class
```

Si oui ✅, la compilation est réussie !


