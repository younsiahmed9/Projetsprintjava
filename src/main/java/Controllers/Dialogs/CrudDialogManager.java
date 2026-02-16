package Controllers.Dialogs;

import Models.Categorie;
import Models.Document;
import Models.Dossier;
import Services.ServiceCategorie;
import Services.ServiceDocument;
import Services.ServiceDossier;
import Controllers.AlertUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import utils.UiStyles;
import javafx.event.ActionEvent;
import utils.ValidationUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Gestionnaire professionnel de dialogs CRUD
 * Fournit des dialogs réutilisables pour Document, Dossier et Catégorie
 */
public class CrudDialogManager {

    private final ServiceDocument docService;
    private final ServiceDossier dossierService;
    private final ServiceCategorie categorieService;

    public CrudDialogManager() {
        this.docService = new ServiceDocument();
        this.dossierService = new ServiceDossier();
        this.categorieService = new ServiceCategorie();
    }

    // ===== DIALOGS DOCUMENT =====

    /**
     * Dialog d'ajout/modification de document
     */
    public Optional<Document> showDocumentDialog(Document editingDoc, boolean isEdit) {
        Dialog<Document> dialog = new Dialog<>();
        UiStyles.applyDialogStyles(dialog.getDialogPane());
        dialog.setTitle(isEdit ? "Modifier un document" : "Ajouter un document");
        dialog.setHeaderText(isEdit ? "Éditer le document" : "Créer un nouveau document");
        dialog.setWidth(600);

        // Contrôles
        TextField tfTitre = new TextField();
        tfTitre.getStyleClass().add("modern-input");
        tfTitre.setPromptText("Titre du document...");
        if (isEdit && editingDoc != null) tfTitre.setText(editingDoc.getTitre());

        TextArea taDescription = new TextArea();
        taDescription.getStyleClass().add("modern-input");
        taDescription.setPromptText("Description...");
        taDescription.setPrefRowCount(4);
        taDescription.setWrapText(true);
        if (isEdit && editingDoc != null) taDescription.setText(editingDoc.getDescription());

        ComboBox<Dossier> cbDossier = new ComboBox<>();
        cbDossier.getStyleClass().add("modern-input");
        cbDossier.setPromptText("Sélectionner un dossier");
        try {
            cbDossier.setItems(FXCollections.observableArrayList(dossierService.findAll()));
            if (isEdit && editingDoc != null) cbDossier.setValue(editingDoc.getDossier());
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les dossiers");
        }

        ComboBox<Categorie> cbCategorie = new ComboBox<>();
        cbCategorie.getStyleClass().add("modern-input");
        cbCategorie.setPromptText("Sélectionner une catégorie");
        try {
            cbCategorie.setItems(FXCollections.observableArrayList(categorieService.findAll()));
            if (isEdit && editingDoc != null) cbCategorie.setValue(editingDoc.getCategorie());
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les catégories");
        }

        TextField tfFilePath = new TextField();
        tfFilePath.getStyleClass().add("modern-input");
        tfFilePath.setPromptText("Chemin du fichier");
        tfFilePath.setEditable(false);
        if (isEdit && editingDoc != null) tfFilePath.setText(editingDoc.getFilePath());

        Button btnBrowse = new Button("📁 Parcourir");
        btnBrowse.getStyleClass().add("btn-secondary");
        btnBrowse.setStyle("-fx-padding: 8 15; -fx-font-size: 11;");
        btnBrowse.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir un fichier");
            File f = fc.showOpenDialog(dialog.getOwner());
            if (f != null) tfFilePath.setText(f.getAbsolutePath());
        });

        HBox fileBox = new HBox(10, tfFilePath, btnBrowse);
        fileBox.setPrefHeight(40);
        HBox.setHgrow(tfFilePath, Priority.ALWAYS);

        // Champ Montant (obligatoire)
        TextField tfMontant = new TextField();
        tfMontant.getStyleClass().add("modern-input");
        tfMontant.setPromptText("Ex: 250.00");
        tfMontant.setStyle("-fx-control-inner-background: #fffbea;");
        if (isEdit && editingDoc != null) {
            tfMontant.setText(String.valueOf(editingDoc.getMontant()));
        }

        // Layout avec GridPane
        GridPane grid = createGrid();
        grid.add(new Label("Titre *"), 0, 0);
        grid.add(tfTitre, 1, 0);
        grid.add(new Label("Description"), 0, 1);
        grid.add(taDescription, 1, 1);
        grid.add(new Label("Dossier *"), 0, 2);
        grid.add(cbDossier, 1, 2);
        grid.add(new Label("Catégorie *"), 0, 3);
        grid.add(cbCategorie, 1, 3);
        grid.add(new Label("Fichier *"), 0, 4);
        grid.add(fileBox, 1, 4);
        grid.add(new Label("Montant 💰 *"), 0, 5);
        grid.add(tfMontant, 1, 5);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        dialog.getDialogPane().setContent(scroll);

        // Boutons
        dialog.getDialogPane().getButtonTypes().addAll(
            ButtonType.OK,
            ButtonType.CANCEL
        );
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) okButton.getStyleClass().add("btn-primary");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) cancelButton.getStyleClass().add("btn-secondary");

        if (okButton != null) {
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                String titre = tfTitre.getText();
                String path = tfFilePath.getText();
                if (!ValidationUtils.isValidTitre(titre)) {
                    AlertUtils.showError("Champ invalide", ValidationUtils.getErrorTitre());
                    tfTitre.requestFocus();
                    event.consume();
                    return;
                }
                if (!ValidationUtils.isValidDescription(taDescription.getText())) {
                    AlertUtils.showError("Champ invalide", ValidationUtils.getErrorDescription());
                    taDescription.requestFocus();
                    event.consume();
                    return;
                }
                if (!ValidationUtils.isValidDossier(cbDossier.getValue())) {
                    AlertUtils.showError("Champ invalide", ValidationUtils.getErrorDossier());
                    cbDossier.requestFocus();
                    event.consume();
                    return;
                }
                if (!ValidationUtils.isValidCategorie(cbCategorie.getValue())) {
                    AlertUtils.showError("Champ invalide", ValidationUtils.getErrorCategorie());
                    cbCategorie.requestFocus();
                    event.consume();
                    return;
                }
                if (!ValidationUtils.isValidFilePath(path)) {
                    AlertUtils.showError("Champ invalide", ValidationUtils.getErrorFilePath());
                    tfFilePath.requestFocus();
                    event.consume();
                    return;
                }
                // Montant est obligatoire
                if (tfMontant.getText().isEmpty()) {
                    AlertUtils.showError("Champ obligatoire", "Le montant est obligatoire");
                    tfMontant.requestFocus();
                    event.consume();
                    return;
                }
                if (!ValidationUtils.isValidMontant(tfMontant.getText())) {
                    AlertUtils.showError("Montant invalide", "Le montant doit être un nombre positif (ex: 250.00)");
                    tfMontant.requestFocus();
                    event.consume();
                    return;
                }
                if (!ValidationUtils.isFileExists(path)) {
                    if (!AlertUtils.showConfirmation("Fichier inexistant",
                            "Le fichier n'existe pas. Voulez-vous continuer quand même ?")) {
                        tfFilePath.requestFocus();
                        event.consume();
                    }
                }
            });
        }

        // Résultat
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                if (tfTitre.getText().isEmpty() || cbDossier.getValue() == null || cbCategorie.getValue() == null || tfFilePath.getText().isEmpty() || tfMontant.getText().isEmpty()) {
                    AlertUtils.showError("Erreur", "Veuillez remplir tous les champs obligatoires (*)");
                    return null;
                }

                Document doc = isEdit && editingDoc != null ? editingDoc : new Document();
                doc.setTitre(tfTitre.getText());
                doc.setDescription(taDescription.getText());
                doc.setFilePath(tfFilePath.getText());
                doc.setDossier(cbDossier.getValue());
                doc.setCategorie(cbCategorie.getValue());

                // Montant est obligatoire
                try {
                    double montant = Double.parseDouble(tfMontant.getText());
                    if (montant < 0) {
                        AlertUtils.showError("Erreur", "Le montant ne peut pas être négatif");
                        return null;
                    }
                    doc.setMontant(montant);
                } catch (NumberFormatException e) {
                    AlertUtils.showError("Erreur", "Le montant doit être un nombre valide");
                    return null;
                }

                return doc;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Dialog professionnel de suppression de document
     */
    public boolean showDeleteDocumentConfirmation(Document doc) {
        return DeleteConfirmationDialog.showAndWait(
            "Document",
            doc.getTitre(),
            "Le fichier \"" + doc.getFilePath() + "\" sera supprimé."
        );
    }

    // ===== DIALOGS DOSSIER =====

    /**
     * Dialog d'ajout/modification de dossier
     */
    public Optional<Dossier> showFolderDialog(Dossier editingFolder, boolean isEdit) {
        Dialog<Dossier> dialog = new Dialog<>();
        UiStyles.applyDialogStyles(dialog.getDialogPane());
        dialog.setTitle(isEdit ? "Modifier un dossier" : "Ajouter un dossier");
        dialog.setHeaderText(isEdit ? "Éditer le dossier" : "Créer un nouveau dossier");
        dialog.setWidth(500);

        TextField tfNom = new TextField();
        tfNom.getStyleClass().add("modern-input");
        tfNom.setPromptText("Nom du dossier...");
        if (isEdit && editingFolder != null) tfNom.setText(editingFolder.getNom());

        TextArea taDescription = new TextArea();
        taDescription.getStyleClass().add("modern-input");
        taDescription.setPromptText("Description...");
        taDescription.setPrefRowCount(4);
        taDescription.setWrapText(true);
        if (isEdit && editingFolder != null) taDescription.setText(editingFolder.getDescription());

        GridPane grid = createGrid();
        grid.add(new Label("Nom *"), 0, 0);
        grid.add(tfNom, 1, 0);
        grid.add(new Label("Description"), 0, 1);
        grid.add(taDescription, 1, 1);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        dialog.getDialogPane().setContent(scroll);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) okButton.getStyleClass().add("btn-primary");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) cancelButton.getStyleClass().add("btn-secondary");

        if (okButton != null) {
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (!ValidationUtils.isValidNom(tfNom.getText())) {
                    AlertUtils.showError("Champ invalide", ValidationUtils.getErrorNom());
                    tfNom.requestFocus();
                    event.consume();
                    return;
                }
                if (!ValidationUtils.isValidDescription(taDescription.getText())) {
                    AlertUtils.showError("Champ invalide", ValidationUtils.getErrorDescription());
                    taDescription.requestFocus();
                    event.consume();
                }
            });
        }

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                if (tfNom.getText().isEmpty()) {
                    AlertUtils.showError("Erreur", "Veuillez entrer un nom pour le dossier");
                    return null;
                }

                Dossier folder = isEdit && editingFolder != null ? editingFolder : new Dossier();
                folder.setNom(tfNom.getText());
                folder.setDescription(taDescription.getText());
                return folder;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Dialog professionnel de suppression de dossier
     */
    public boolean showDeleteFolderConfirmation(Dossier folder) {
        return DeleteConfirmationDialog.showAndWait(
            "Dossier",
            folder.getNom(),
            "Tous les documents contenus dans ce dossier resteront accessibles mais ne seront plus classés."
        );
    }

    // ===== DIALOGS CATÉGORIE =====

    /**
     * Dialog d'ajout/modification de catégorie
     */
    public Optional<Categorie> showCategoryDialog(Categorie editingCategory, boolean isEdit) {
        Dialog<Categorie> dialog = new Dialog<>();
        UiStyles.applyDialogStyles(dialog.getDialogPane());
        dialog.setTitle(isEdit ? "Modifier une catégorie" : "Ajouter une catégorie");
        dialog.setHeaderText(isEdit ? "Éditer la catégorie" : "Créer une nouvelle catégorie");
        dialog.setWidth(500);

        TextField tfNom = new TextField();
        tfNom.getStyleClass().add("modern-input");
        tfNom.setPromptText("Nom de la catégorie...");
        if (isEdit && editingCategory != null) tfNom.setText(editingCategory.getNom());

        TextArea taDescription = new TextArea();
        taDescription.getStyleClass().add("modern-input");
        taDescription.setPromptText("Description...");
        taDescription.setPrefRowCount(4);
        taDescription.setWrapText(true);
        if (isEdit && editingCategory != null) taDescription.setText(editingCategory.getDescription());

        GridPane grid = createGrid();
        grid.add(new Label("Nom *"), 0, 0);
        grid.add(tfNom, 1, 0);
        grid.add(new Label("Description"), 0, 1);
        grid.add(taDescription, 1, 1);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        dialog.getDialogPane().setContent(scroll);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) okButton.getStyleClass().add("btn-primary");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) cancelButton.getStyleClass().add("btn-secondary");

        if (okButton != null) {
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (!ValidationUtils.isValidNom(tfNom.getText())) {
                    AlertUtils.showError("Champ invalide", ValidationUtils.getErrorNom());
                    tfNom.requestFocus();
                    event.consume();
                    return;
                }
                if (!ValidationUtils.isValidDescription(taDescription.getText())) {
                    AlertUtils.showError("Champ invalide", ValidationUtils.getErrorDescription());
                    taDescription.requestFocus();
                    event.consume();
                }
            });
        }

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                if (tfNom.getText().isEmpty()) {
                    AlertUtils.showError("Erreur", "Veuillez entrer un nom pour la catégorie");
                    return null;
                }

                Categorie category = isEdit && editingCategory != null ? editingCategory : new Categorie();
                category.setNom(tfNom.getText());
                category.setDescription(taDescription.getText());
                return category;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Dialog professionnel de suppression de catégorie
     */
    public boolean showDeleteCategoryConfirmation(Categorie category) {
        return DeleteConfirmationDialog.showAndWait(
            "Catégorie",
            category.getNom(),
            "Les documents associés à cette catégorie resteront mais perdront leur classification."
        );
    }

    // ===== UTILITAIRES =====


    /**
     * Crée une GridPane avec les styles appropriés
     */
    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        // Styles
        grid.getStylesheets().add("styles.css");

        // Colonne 0 : Labels (30%)
        // Colonne 1 : Inputs (70%)
        return grid;
    }
}

