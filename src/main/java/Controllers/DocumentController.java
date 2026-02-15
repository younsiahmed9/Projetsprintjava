package Controllers;

import Models.Categorie;
import Models.Dossier;
import Models.Document;
import Services.ServiceCategorie;
import Services.ServiceDossier;
import Services.ServiceDocument;
import utils.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;

public class DocumentController {
    @FXML private ListView<Document> listDocuments;
    @FXML private TextField tfTitre;
    @FXML private TextArea taDesc;
    @FXML private TextField tfPath;
    @FXML private ComboBox<Dossier> cbDossier;
    @FXML private ComboBox<Categorie> cbCategorie;

    private final ServiceDocument docService = new ServiceDocument();
    private final ServiceDossier dossierService = new ServiceDossier();
    private final ServiceCategorie categorieService = new ServiceCategorie();
    private Document selected;

    @FXML
    public void initialize() {
        listDocuments.setCellFactory(param -> new DocumentListCell());
        listDocuments.setOnMouseClicked(event -> onListClick());

        // Charger les dossiers
        try {
            cbDossier.setItems(FXCollections.observableArrayList(dossierService.findAll()));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur Base de Données", e.getMessage());
        }

        // Charger les catégories
        try {
            cbCategorie.setItems(FXCollections.observableArrayList(categorieService.findAll()));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur Base de Données", e.getMessage());
        }

        // Validation en temps réel
        tfTitre.textProperty().addListener((obs, oldVal, newVal) -> validateTitreField());
        tfPath.textProperty().addListener((obs, oldVal, newVal) -> validatePathField());
        cbDossier.valueProperty().addListener((obs, oldVal, newVal) -> validateDossierField());

        refresh();
    }

    /**
     * Valide le champ Titre en temps réel
     */
    private void validateTitreField() {
        String titre = tfTitre.getText();
        if (titre.isEmpty()) {
            tfTitre.setStyle("-fx-border-color: transparent;");
        } else if (!ValidationUtils.isValidTitre(titre)) {
            tfTitre.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
        } else {
            tfTitre.setStyle("-fx-border-color: #22c55e; -fx-border-width: 2;");
        }
    }

    /**
     * Valide le champ Chemin du fichier en temps réel
     */
    private void validatePathField() {
        String path = tfPath.getText();
        if (path.isEmpty()) {
            tfPath.setStyle("-fx-border-color: transparent;");
        } else if (!ValidationUtils.isValidFilePath(path)) {
            tfPath.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
        } else if (ValidationUtils.isFileExists(path)) {
            tfPath.setStyle("-fx-border-color: #22c55e; -fx-border-width: 2;");
        } else {
            tfPath.setStyle("-fx-border-color: #f59e0b; -fx-border-width: 2;"); // Orange pour fichier inexistant
        }
    }

    /**
     * Valide le champ Dossier en temps réel
     */
    private void validateDossierField() {
        if (cbDossier.getValue() != null) {
            cbDossier.setStyle("-fx-border-color: #22c55e; -fx-border-width: 2;");
        } else {
            cbDossier.setStyle("-fx-border-color: transparent;");
        }
    }

    private void onListClick() {
        selected = listDocuments.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        tfTitre.setText(selected.getTitre());
        taDesc.setText(selected.getDescription());
        tfPath.setText(selected.getFilePath());
        cbDossier.getSelectionModel().select(selected.getDossier());
        cbCategorie.getSelectionModel().select(selected.getCategorie());
    }

    @FXML
    private void onBrowse() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir un fichier");
        File f = fc.showOpenDialog(tfTitre.getScene().getWindow());
        if (f != null) {
            tfPath.setText(f.getAbsolutePath());
        }
    }

    @FXML
    private void onAdd() {
        if (!validateForm()) return;

        try {
            docService.add(readForm());
            AlertUtils.showSuccess("Succès", "Document ajouté avec succès !");
            refresh();
            onClear();
        } catch (Exception e) {
            AlertUtils.showError("Erreur lors de l'ajout", e.getMessage());
        }
    }

    @FXML
    private void onUpdate() {
        if (selected == null) {
            AlertUtils.showWarning("Attention", "Veuillez sélectionner un document à modifier.");
            return;
        }

        if (!validateForm()) return;

        try {
            Document d = readForm();
            d.setId(selected.getId());
            docService.update(d);
            AlertUtils.showSuccess("Succès", "Document modifié avec succès !");
            refresh();
            onClear();
        } catch (Exception e) {
            AlertUtils.showError("Erreur lors de la modification", e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        if (selected == null) {
            AlertUtils.showWarning("Attention", "Veuillez sélectionner un document à supprimer.");
            return;
        }

        if (!AlertUtils.showConfirmation("Confirmation",
                "Êtes-vous sûr de vouloir supprimer le document \"" + selected.getTitre() + "\" ?")) {
            return;
        }

        try {
            docService.delete(selected.getId());
            AlertUtils.showSuccess("Succès", "Document supprimé avec succès !");
            refresh();
            onClear();
        } catch (Exception e) {
            AlertUtils.showError("Erreur lors de la suppression", e.getMessage());
        }
    }

    @FXML
    private void onClear() {
        selected = null;
        tfTitre.clear();
        taDesc.clear();
        tfPath.clear();
        cbDossier.getSelectionModel().clearSelection();
        cbCategorie.getSelectionModel().clearSelection();
        tfTitre.setStyle("-fx-border-color: transparent;");
        tfPath.setStyle("-fx-border-color: transparent;");
        cbDossier.setStyle("-fx-border-color: transparent;");
        listDocuments.getSelectionModel().clearSelection();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    private void refresh() {
        try {
            listDocuments.setItems(FXCollections.observableArrayList(docService.findAll()));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur Base de Données", e.getMessage());
        }
    }

    /**
     * Valide tous les champs du formulaire
     */
    private boolean validateForm() {
        String titre = tfTitre.getText();
        Dossier dossier = cbDossier.getValue();
        String path = tfPath.getText();

        // Validation du titre
        if (!ValidationUtils.isValidTitre(titre)) {
            AlertUtils.showError("Champ invalide", ValidationUtils.getErrorTitre());
            tfTitre.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            return false;
        }

        // Validation du dossier
        if (!ValidationUtils.isValidDossier(dossier)) {
            AlertUtils.showError("Champ invalide", ValidationUtils.getErrorDossier());
            cbDossier.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            return false;
        }

        // Validation du chemin du fichier
        if (!ValidationUtils.isValidFilePath(path)) {
            AlertUtils.showError("Champ invalide", ValidationUtils.getErrorFilePath());
            tfPath.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            return false;
        }

        // Vérifier que le fichier existe
        if (!ValidationUtils.isFileExists(path)) {
            if (!AlertUtils.showConfirmation("Fichier inexistant",
                    "Le fichier n'existe pas. Voulez-vous continuer quand même ?")) {
                tfPath.setStyle("-fx-border-color: #f59e0b; -fx-border-width: 2;");
                return false;
            }
        }

        return true;
    }

    private Document readForm() {
        String titre = ValidationUtils.sanitize(tfTitre.getText());
        String desc = ValidationUtils.sanitize(taDesc.getText());
        String path = ValidationUtils.sanitize(tfPath.getText());
        Dossier dossier = cbDossier.getValue();
        Categorie categorie = cbCategorie.getValue();

        Document d = new Document();
        d.setTitre(titre);
        d.setDescription(desc);
        d.setFilePath(path);
        d.setDossier(dossier);
        d.setCategorie(categorie);
        return d;
    }
}

