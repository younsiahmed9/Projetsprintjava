package Controllers;

import Models.Categorie;
import Services.ServiceCategorie;
import utils.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

public class CategorieController {
    @FXML private ListView<Categorie> listCategories;
    @FXML private TextField tfNom;
    @FXML private TextArea taDesc;

    private final ServiceCategorie categorieService = new ServiceCategorie();
    private Categorie selected;

    @FXML
    public void initialize() {
        listCategories.setCellFactory(param -> new CategorieListCell());
        listCategories.setOnMouseClicked(event -> onListClick());

        // Validation en temps réel
        tfNom.textProperty().addListener((obs, oldVal, newVal) -> validateNomField());

        refresh();
    }

    /**
     * Valide le champ Nom en temps réel
     */
    private void validateNomField() {
        String nom = tfNom.getText();
        if (nom.isEmpty()) {
            tfNom.setStyle("-fx-border-color: transparent;");
        } else if (!ValidationUtils.isValidTitre(nom)) {
            tfNom.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
        } else {
            tfNom.setStyle("-fx-border-color: #22c55e; -fx-border-width: 2;");
        }
    }

    private void onListClick() {
        selected = listCategories.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        tfNom.setText(selected.getNom());
        taDesc.setText(selected.getDescription());
    }

    @FXML
    private void onAdd() {
        if (!validateForm()) return;

        try {
            // Vérifier que le nom n'existe pas déjà
            if (categorieService.existsByNom(tfNom.getText())) {
                AlertUtils.showError("Erreur", "Une catégorie avec ce nom existe déjà !");
                tfNom.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                return;
            }

            Categorie cat = new Categorie(ValidationUtils.sanitize(tfNom.getText()),
                    ValidationUtils.sanitize(taDesc.getText()));
            categorieService.add(cat);
            AlertUtils.showSuccess("Succès", "Catégorie ajoutée avec succès !");
            refresh();
            onClear();
        } catch (Exception e) {
            AlertUtils.showError("Erreur lors de l'ajout", e.getMessage());
        }
    }

    @FXML
    private void onUpdate() {
        if (selected == null) {
            AlertUtils.showWarning("Attention", "Veuillez sélectionner une catégorie à modifier.");
            return;
        }

        if (!validateForm()) return;

        try {
            selected.setNom(ValidationUtils.sanitize(tfNom.getText()));
            selected.setDescription(ValidationUtils.sanitize(taDesc.getText()));
            categorieService.update(selected);
            AlertUtils.showSuccess("Succès", "Catégorie modifiée avec succès !");
            refresh();
            onClear();
        } catch (Exception e) {
            AlertUtils.showError("Erreur lors de la modification", e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        if (selected == null) {
            AlertUtils.showWarning("Attention", "Veuillez sélectionner une catégorie à supprimer.");
            return;
        }

        if (!AlertUtils.showConfirmation("Confirmation",
                "Êtes-vous sûr de vouloir supprimer la catégorie \"" + selected.getNom() + "\" ?\n\n" +
                "Les documents associés conserveront une catégorie vide.")) {
            return;
        }

        try {
            categorieService.delete(selected.getId());
            AlertUtils.showSuccess("Succès", "Catégorie supprimée avec succès !");
            refresh();
            onClear();
        } catch (Exception e) {
            AlertUtils.showError("Erreur lors de la suppression", e.getMessage());
        }
    }

    @FXML
    private void onClear() {
        selected = null;
        tfNom.clear();
        taDesc.clear();
        tfNom.setStyle("-fx-border-color: transparent;");
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    private void refresh() {
        try {
            listCategories.setItems(FXCollections.observableArrayList(categorieService.findAll()));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur Base de Données", e.getMessage());
        }
    }

    /**
     * Valide tous les champs du formulaire
     */
    private boolean validateForm() {
        String nom = tfNom.getText();
        String desc = taDesc.getText();

        // Validation du nom
        if (!ValidationUtils.isValidTitre(nom)) {
            AlertUtils.showError("Champ invalide", "Le nom doit contenir au moins 3 caractères !");
            tfNom.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            return false;
        }

        // Validation de la description (optionnelle)
        if (!ValidationUtils.isValidDescription(desc)) {
            AlertUtils.showError("Champ invalide", ValidationUtils.getErrorDescription());
            taDesc.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            return false;
        }

        return true;
    }
}
