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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;

public class DocumentController {
    @FXML
    private VBox containerDocuments;
    @FXML
    private TextField tfTitre;
    @FXML
    private TextArea taDesc;
    @FXML
    private TextField tfPath;
    @FXML
    private ComboBox<Dossier> cbDossier;
    @FXML
    private ComboBox<Categorie> cbCategorie;

    private final ServiceDocument docService = new ServiceDocument();
    private final ServiceDossier dossierService = new ServiceDossier();
    private final ServiceCategorie categorieService = new ServiceCategorie();
    private Document selected;

    @FXML
    public void initialize() {
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
        cbCategorie.valueProperty().addListener((obs, oldVal, newVal) -> validateCategorieField());

        refresh();
    }

    private VBox createDocumentCard(Document doc) {
        VBox card = new VBox(5);
        card.setStyle(
                "-fx-padding: 12; -fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

        Label lblTitle = new Label("📄 " + doc.getTitre());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #1e293b;");

        Label lblStatus = new Label(doc.getStatus());
        String statusColor = "#64748b"; // default
        if ("VALIDATED".equals(doc.getStatus()))
            statusColor = "#22c55e";
        else if ("REJECTED".equals(doc.getStatus()))
            statusColor = "#ef4444";
        else if ("PENDING".equals(doc.getStatus()))
            statusColor = "#f59e0b";
        lblStatus.setStyle("-fx-background-color: " + statusColor
                + "; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 9; -fx-font-weight: bold;");

        HBox titleStatus = new HBox(8, lblTitle, lblStatus);
        titleStatus.setAlignment(Pos.CENTER_LEFT);

        Label lblDesc = new Label(doc.getDescription() != null && !doc.getDescription().isEmpty()
                ? doc.getDescription()
                : "(Pas de description)");
        lblDesc.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");
        lblDesc.setWrapText(true);

        HBox hboxMeta = new HBox(12);
        Label lblDossier = new Label("📁 " + (doc.getDossier() != null ? doc.getDossier().getNom() : "N/A"));
        lblDossier.setStyle("-fx-font-size: 9; -fx-text-fill: #999;");

        Label lblCategorie = new Label("🏷️ " + (doc.getCategorie() != null ? doc.getCategorie().getNom() : "N/A"));
        lblCategorie.setStyle("-fx-font-size: 9; -fx-text-fill: #999;");

        hboxMeta.getChildren().addAll(lblDossier, lblCategorie);

        HBox btnBox = new HBox(8);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnEdit = new Button("Modifier");
        btnEdit.getStyleClass().add("btn-modify");
        btnEdit.setStyle("-fx-font-size: 10; -fx-min-width: 70;");
        btnEdit.setOnAction(e -> selectDocument(doc));

        Button btnDelete = new Button("Supprimer");
        btnDelete.getStyleClass().add("btn-delete");
        btnDelete.setStyle("-fx-font-size: 10; -fx-min-width: 70;");
        btnDelete.setOnAction(e -> deleteDocumentCard(doc));

        btnBox.getChildren().addAll(btnEdit, btnDelete);

        card.getChildren().addAll(titleStatus, lblDesc, hboxMeta, btnBox);
        return card;
    }

    /**
     * Sélectionne un document et le charge dans le formulaire
     */
    private void selectDocument(Document doc) {
        selected = doc;
        tfTitre.setText(doc.getTitre());
        taDesc.setText(doc.getDescription());
        tfPath.setText(doc.getFilePath());
        cbDossier.setValue(doc.getDossier());
        cbCategorie.setValue(doc.getCategorie());
    }

    /**
     * Supprime un document avec confirmation (depuis la carte)
     */
    private void deleteDocumentCard(Document doc) {
        if (!AlertUtils.showConfirmation("Confirmation",
                "Êtes-vous sûr de vouloir supprimer le document \"" + doc.getTitre() + "\" ?")) {
            return;
        }

        try {
            docService.delete(doc.getId());
            AlertUtils.showSuccess("Succès", "Document supprimé avec succès !");
            refresh();
            onClear();
        } catch (Exception e) {
            AlertUtils.showError("Erreur lors de la suppression", e.getMessage());
        }
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
            tfPath.setStyle("-fx-border-color: #f59e0b; -fx-border-width: 2;");
        }
    }

    private void validateDossierField() {
        if (cbDossier.getValue() != null) {
            cbDossier.setStyle("-fx-border-color: #22c55e; -fx-border-width: 2;");
        } else {
            cbDossier.setStyle("-fx-border-color: transparent;");
        }
    }

    private void validateCategorieField() {
        if (cbCategorie.getValue() != null) {
            cbCategorie.setStyle("-fx-border-color: #22c55e; -fx-border-width: 2;");
        } else {
            cbCategorie.setStyle("-fx-border-color: transparent;");
        }
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
        if (!validateForm())
            return;

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

        if (!validateForm())
            return;

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
        taDesc.setStyle("-fx-border-color: transparent;");
        tfPath.setStyle("-fx-border-color: transparent;");
        cbDossier.setStyle("-fx-border-color: transparent;");
        cbCategorie.setStyle("-fx-border-color: transparent;");
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    private void refresh() {
        try {
            containerDocuments.getChildren().clear();
            for (Document doc : docService.findAll()) {
                containerDocuments.getChildren().add(createDocumentCard(doc));
            }
        } catch (SQLException e) {
            AlertUtils.showError("Erreur Base de Données", e.getMessage());
        }
    }

    /**
     * Valide tous les champs du formulaire
     */
    private boolean validateForm() {
        String titre = tfTitre.getText();
        String desc = taDesc.getText();
        Dossier dossier = cbDossier.getValue();
        Categorie categorie = cbCategorie.getValue();
        String path = tfPath.getText();

        // Validation du titre
        if (!ValidationUtils.isValidTitre(titre)) {
            AlertUtils.showError("Champ invalide", ValidationUtils.getErrorTitre());
            tfTitre.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            return false;
        }

        // Validation de la description (optionnelle)
        if (!ValidationUtils.isValidDescription(desc)) {
            AlertUtils.showError("Champ invalide", ValidationUtils.getErrorDescription());
            taDesc.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            return false;
        }

        // Validation du dossier
        if (!ValidationUtils.isValidDossier(dossier)) {
            AlertUtils.showError("Champ invalide", ValidationUtils.getErrorDossier());
            cbDossier.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            return false;
        }

        // Validation de la catégorie
        if (!ValidationUtils.isValidCategorie(categorie)) {
            AlertUtils.showError("Champ invalide", ValidationUtils.getErrorCategorie());
            cbCategorie.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
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
