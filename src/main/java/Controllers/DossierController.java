package Controllers;

import Models.Dossier;
import Services.ServiceDossier;
import utils.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

public class DossierController {
    @FXML private ListView<Dossier> listDossiers;
    @FXML private TextField tfNom;
    @FXML private TextArea taDesc;

    private final ServiceDossier service = new ServiceDossier();
    private Dossier selected;

    @FXML
    public void initialize() {
        listDossiers.setCellFactory(param -> new DossierListCell());
        listDossiers.setOnMouseClicked(event -> onListClick());

        // Ajouter validation en temps réel
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
        } else if (!ValidationUtils.isValidNom(nom)) {
            tfNom.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
        } else {
            tfNom.setStyle("-fx-border-color: #22c55e; -fx-border-width: 2;");
        }
    }

    private void onListClick() {
        selected = listDossiers.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        tfNom.setText(selected.getNom());
        taDesc.setText(selected.getDescription());
    }

    @FXML
    private void onAdd() {
        if (!validateForm()) return;

        try {
            service.add(readForm());
            AlertUtils.showSuccess("Succès", "Dossier ajouté avec succès !");
            refresh();
            onClear();
        } catch (Exception e) {
            AlertUtils.showError("Erreur lors de l'ajout", e.getMessage());
        }
    }

    @FXML
    private void onUpdate() {
        if (selected == null) {
            AlertUtils.showWarning("Attention", "Veuillez sélectionner un dossier à modifier.");
            return;
        }

        if (!validateForm()) return;

        try {
            Dossier d = readForm();
            d.setId(selected.getId());
            service.update(d);
            AlertUtils.showSuccess("Succès", "Dossier modifié avec succès !");
            refresh();
            onClear();
        } catch (Exception e) {
            AlertUtils.showError("Erreur lors de la modification", e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        if (selected == null) {
            AlertUtils.showWarning("Attention", "Veuillez sélectionner un dossier à supprimer.");
            return;
        }

        if (!AlertUtils.showConfirmation("Confirmation",
                "Êtes-vous sûr de vouloir supprimer le dossier \"" + selected.getNom() + "\" ?")) {
            return;
        }

        try {
            service.delete(selected.getId());
            AlertUtils.showSuccess("Succès", "Dossier supprimé avec succès !");
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
            listDossiers.setItems(FXCollections.observableArrayList(service.findAll()));
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
        if (!ValidationUtils.isValidNom(nom)) {
            AlertUtils.showError("Champ invalide", ValidationUtils.getErrorNom());
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

    private Dossier readForm() {
        String nom = ValidationUtils.sanitize(tfNom.getText());
        String desc = ValidationUtils.sanitize(taDesc.getText());
        return new Dossier(nom, desc);
    }
}

