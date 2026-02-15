# Configuration d'exécution pour JavaFX dans IntelliJ IDEA

## Pour lancer l'application :

### Méthode 1 : Via IntelliJ (Recommandé)
1. Ouvrez `src/main/java/Test/MainFx.java`
2. Cliquez droit sur le fichier → **Run 'MainFx'**
3. Si erreur JavaFX, allez à : **Run → Edit Configurations**
4. Pour la configuration "MainFx" :
   - Allez dans l'onglet **VM options**
   - Ajoutez cette ligne :
   ```
   --module-path C:\path\to\javafx-sdk\lib --add-modules javafx.controls,javafx.fxml
   ```
   - Remplacez `C:\path\to\javafx-sdk` par votre chemin JavaFX réel

### Méthode 2 : Via Maven (Command Line)
```bash
cd C:\Users\Mega-PC\IdeaProjects\FinTrack
mvn javafx:run
```

## Pour trouver votre chemin JavaFX :

Dans IntelliJ :
1. **File → Project Structure → SDKs**
2. Sélectionnez votre JDK 17
3. Le chemin JavaFX est généralement dans le dossier du JDK ou séparé

## Alternative : Télécharger JavaFX SDK

Si JavaFX n'est pas installé :
1. Allez sur https://gluonhq.com/products/javafx/
2. Téléchargez JavaFX SDK (version 17+)
3. Décompressez dans `C:\javafx-sdk-17`
4. Configurez dans IntelliJ → Project Structure → Global Libraries → Add...

