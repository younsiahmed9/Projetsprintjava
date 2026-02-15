# FinTrack - Interface CRUD Dépenses

## Structure de l'Interface

La nouvelle interface CRUD offre une navigation intuitive avec 3 boutons principaux :

### 📄 Pages FXML créées :
1. **MainPage.fxml** - Page principale avec les 3 boutons de navigation
2. **AjouterDepenseForm.fxml** - Formulaire pour ajouter une nouvelle dépense
3. **ModifierDepenseForm.fxml** - Recherche par ID + modification
4. **SupprimerDepenseForm.fxml** - Recherche par ID + suppression

### 🎮 Contrôleurs créés :
1. **MainPageController.java** - Gère la navigation entre les pages
2. **AjouterDepenseController.java** - Ajouter une dépense
3. **ModifierDepenseController.java** - Rechercher et modifier une dépense
4. **SupprimerDepenseController.java** - Rechercher et supprimer une dépense

## Fonctionnalité

### Bouton "➕ Ajouter"
- Affiche un formulaire pour créer une nouvelle dépense
- Champs : ID Utilisateur, Budget, Catégorie, Montant, Date, Description, Mode Paiement
- Boutons : Ajouter (enregistre) / Annuler (retour)

### Bouton "🗑️ Supprimer"
- Champ de recherche par ID
- Affiche les détails de la dépense trouvée
- Confirmation avant suppression
- Boutons : Supprimer (rouge) / Annuler

### Bouton "⇄ Modifier"
- Champ de recherche par ID
- Formulaire pré-rempli avec les données trouvées
- Modification libre des champs
- Boutons : Modifier / Annuler

## Utilisation

1. Lancer l'application via `Test.MainFx`
2. Cliquer sur l'un des 3 boutons pour accéder à la fonctionnalité souhaitée
3. Remplir/modifier les données
4. Confirmer ou annuler l'action
5. Retour automatique à la page principale

## Notes

- Toutes les dépenses sont synchronisées avec la base de données MySQL
- Gestion d'erreurs avec messages d'alerte
- Navigation fluide entre les pages avec FXMLLoader

