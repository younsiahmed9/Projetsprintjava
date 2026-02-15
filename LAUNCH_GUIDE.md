# Configuration pour IntelliJ IDEA - Lancer FinTrack facilement

## 🚀 Méthode 1 : Lancer depuis IntelliJ (RECOMMANDÉ)

### Option A : Via le bouton Run (Plus rapide)
1. Ouvrez le fichier `src/main/java/Test/MainFx.java`
2. Cliquez sur le triangle ▶️ vert à côté du nom de la classe ou de `main()`
3. Sélectionnez "Run 'MainFx'"

### Option B : Via Edit Configurations (Si erreur JavaFX)
1. **Run → Edit Configurations**
2. Cliquez sur **"+"** → **Application**
3. Configurez comme suit :
   - **Name** : FinTrack
   - **Main class** : `Test.MainFx`
   - **VM options** : 
     ```
     --module-path C:\javafx-sdk-17\lib --add-modules javafx.controls,javafx.fxml
     ```
   - **Working directory** : `C:\Users\Mega-PC\IdeaProjects\FinTrack`
   - **Use classpath of module** : `FinTrack`
4. Cliquez **OK**
5. Cliquez le bouton **Run** (ou Shift+F10)

---

## 🎯 Méthode 2 : Lancer via PowerShell

### Exécuter le script :
```powershell
cd C:\Users\Mega-PC\IdeaProjects\FinTrack
.\run.ps1
```

### Ou manuellement :
```powershell
cd C:\Users\Mega-PC\IdeaProjects\FinTrack
mvn clean compile
mvn javafx:run
```

---

## 🔧 Méthode 3 : Lancer via Maven (Command Line)

```bash
cd C:\Users\Mega-PC\IdeaProjects\FinTrack
mvn javafx:run
```

---

## ⚙️ Configuration du SDK JavaFX dans IntelliJ

Si vous avez une erreur "JavaFX SDK not found" :

1. **File → Project Structure → Project**
2. Vérifiez que votre **SDK** est configuré sur JDK 17 ou supérieur
3. **File → Project Structure → Global Libraries**
4. Cliquez **"+"** → **Java**
5. Pointez vers le dossier `lib` de votre JavaFX SDK (exemple: `C:\javafx-sdk-17\lib`)
6. Donnez-lui un nom : "JavaFX-17"
7. Appliquez les changements

---

## 📍 Emplacements courants de JavaFX SDK

- **Avec Temurin JDK** : `C:\Program Files\Eclipse Adoptium\jdk-17.x.x\`
- **Téléchargé séparément** : `C:\javafx-sdk-17\`
- **Via Homebrew/Chocolatey** : Vérifiez le PATH avec `where javafx`

---

## 🐛 Dépannage

### Erreur : "JavaFX components are missing"
→ Ajoutez les VM options mentionnées dans **Option B** ci-dessus

### Erreur : "Cannot find module javafx.controls"
→ Vérifiez que `--module-path` pointe vers le bon dossier JavaFX

### Erreur : "Process exited with code 1"
→ Essayez via la **Méthode 2** (PowerShell) ou relancez depuis IntelliJ avec Invalidate Cache

---

## ✅ Test rapide

Après lancement, vous devriez voir :
- Une fenêtre avec le titre "FinTrack - Gestion des Dépenses"
- 3 boutons : **➕ Ajouter**, **🗑️ Supprimer**, **⇄ Modifier**
- Un formulaire s'affiche quand vous cliquez sur un bouton

