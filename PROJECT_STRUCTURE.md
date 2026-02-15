# 📁 Structure du Projet FinTrack

## 📂 Arborescence Complète

```
FinTrack/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── Controllers/
│   │   │   │   ├── DepenseController.java          (CRUD original - non utilisé)
│   │   │   │   ├── BudgetController.java
│   │   │   │   ├── MainPageController.java         ✨ NEW - Navigation
│   │   │   │   ├── AjouterDepenseController.java   ✨ NEW - Ajouter
│   │   │   │   ├── ModifierDepenseController.java  ✨ NEW - Modifier
│   │   │   │   └── SupprimerDepenseController.java ✨ NEW - Supprimer
│   │   │   ├── Models/
│   │   │   │   ├── Depense.java
│   │   │   │   └── Budget.java
│   │   │   ├── Services/
│   │   │   │   ├── ServiceDepense.java
│   │   │   │   ├── ServiceBudget.java
│   │   │   │   └── Iservice.java
│   │   │   ├── Test/
│   │   │   │   ├── Main.java
│   │   │   │   └── MainFx.java               (Point d'entrée mise à jour)
│   │   │   ├── utils/
│   │   │   │   └── MyDatabase.java
│   │   │   └── tn/esprit/
│   │   │       └── Main.java
│   │   └── resources/
│   │       ├── MainPage.fxml                 ✨ NEW - Page principale
│   │       ├── AjouterDepenseForm.fxml       ✨ NEW - Formulaire ajout
│   │       ├── ModifierDepenseForm.fxml      ✨ NEW - Formulaire modification
│   │       ├── SupprimerDepenseForm.fxml     ✨ NEW - Formulaire suppression
│   │       └── AjouterDepense.fxml           (Ancien formulaire)
│   └── test/
│       └── java/Services/
│           └── ServiceDepenseTest.java
├── target/
│   ├── classes/                              (Compilé - généré par Maven)
│   ├── generated-sources/
│   └── test-classes/
├── pom.xml                                   (Mise à jour - config Maven/JavaFX)
├── run.bat                                   ✨ Script de lancement Windows
├── run.ps1                                   ✨ Script PowerShell
├── run.sh                                    ✨ Script Linux/Mac
├── QUICK_START.md                            ✨ Guide rapide
├── LAUNCH_GUIDE.md                           ✨ Guide de lancement
├── JAVAFX_SETUP.md                           ✨ Configuration JavaFX
├── README_CRUD.md                            ✨ Documentation CRUD
└── README.md                                 (Optionnel - à créer)
```

---

## 🎯 Fichiers Clés par Fonctionnalité

### 🏠 Page Principale
- **FXML** : `src/main/resources/MainPage.fxml`
- **Controller** : `Controllers/MainPageController.java`
- **Rôle** : Menu d'accueil avec 3 boutons

### ➕ Ajouter une Dépense
- **FXML** : `src/main/resources/AjouterDepenseForm.fxml`
- **Controller** : `Controllers/AjouterDepenseController.java`
- **Service** : `Services/ServiceDepense.java` → `ajouter()`
- **Rôle** : Formulaire complet pour créer une dépense

### 🔍 Modifier une Dépense
- **FXML** : `src/main/resources/ModifierDepenseForm.fxml`
- **Controller** : `Controllers/ModifierDepenseController.java`
- **Service** : `Services/ServiceDepense.java` → `recuperer()`, `modifier()`
- **Rôle** : Recherche par ID + modification

### 🗑️ Supprimer une Dépense
- **FXML** : `src/main/resources/SupprimerDepenseForm.fxml`
- **Controller** : `Controllers/SupprimerDepenseController.java`
- **Service** : `Services/ServiceDepense.java` → `recuperer()`, `supprimer()`
- **Rôle** : Recherche par ID + suppression avec confirmation

---

## 📊 Modèle de Données

### Classe Depense
```java
public class Depense {
    private int idDepense;
    private int idUtilisateur;
    private int idBudget;
    private String categorie;
    private double montant;
    private Date dateDepense;
    private String description;
    private String modePaiement;  // "carte", "virement", "cash"
}
```

### Table MySQL
```sql
CREATE TABLE depense (
    id_depense INT PRIMARY KEY AUTO_INCREMENT,
    id_utilisateur INT NOT NULL,
    id_budget INT NOT NULL,
    categorie VARCHAR(100),
    montant DECIMAL(10, 2),
    date_depense DATE,
    description TEXT,
    mode_paiement VARCHAR(50)
);
```

---

## 🔄 Flow de Navigation

```
Démarrage
    ↓
[MainPage.fxml]
    ├─ Bouton "Ajouter" → [AjouterDepenseForm.fxml]
    │   └─ OnAction: Créer + Retour
    ├─ Bouton "Modifier" → [ModifierDepenseForm.fxml]
    │   ├─ Rechercher par ID
    │   └─ OnAction: Modifier + Retour
    └─ Bouton "Supprimer" → [SupprimerDepenseForm.fxml]
        ├─ Rechercher par ID
        └─ OnAction: Supprimer (avec confirmation) + Retour
```

---

## 🛠️ Technologies Utilisées

| Technologie | Version | Rôle |
|-------------|---------|------|
| **Java** | 17+ | Langage principal |
| **JavaFX** | 17 | Interface graphique |
| **Maven** | 3.9+ | Gestionnaire de dépendances |
| **MySQL** | 8.0+ | Base de données |
| **IntelliJ IDEA** | 2023+ | IDE de développement |

---

## 📦 Dépendances (dans pom.xml)

```xml
<dependencies>
    <!-- MySQL -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>17.0.7</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>17.0.7</version>
    </dependency>
    
    <!-- JUnit -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 🚀 Commandes Utiles

```powershell
# Compiler
mvn clean compile

# Lancer
mvn javafx:run

# Lancer via script
.\run.bat

# Nettoyer
mvn clean

# Générer JAR exécutable
mvn package
```

---

## 📝 Notes Importantes

1. **JavaFX est modulaire** : Nécessite `--module-path` et `--add-modules`
2. **FXML** : Fichier XML pour décrire l'interface (au lieu de code Java)
3. **Controller** : Classe Java qui gère les événements FXML
4. **Navigation** : Basée sur `FXMLLoader` pour charger les pages dynamiquement
5. **Base de données** : Directe via JDBC (pas de framework ORM)

---

## ✅ Statut du Projet

- ✅ Interface CRUD complète
- ✅ Navigation fluide entre pages
- ✅ Gestion d'erreurs
- ✅ Validation des données
- ✅ Connexion à MySQL
- ✅ Scripts de lancement
- ⏳ Tests unitaires (partiellement)
- ⏳ Authentification utilisateur
- ⏳ Rapports/Statistiques


