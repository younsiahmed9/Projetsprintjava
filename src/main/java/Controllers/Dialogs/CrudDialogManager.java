package Controllers.Dialogs;

import Models.Categorie;
import Models.Document;
import Models.Dossier;
import Services.ServiceCategorie;
import Services.ServiceDocument;
import Services.ServiceDossier;
import Controllers.AlertUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import utils.OcrService;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Gestionnaire professionnel de dialogs CRUD
 * Fournit des dialogs réutilisables pour Document, Dossier et Catégorie
 */
public class CrudDialogManager {

    private final ServiceDocument docService;
    private final ServiceDossier dossierService;
    private final ServiceCategorie categorieService;
    private final OcrService ocrService;

    public CrudDialogManager() {
        this.docService = new ServiceDocument();
        this.dossierService = new ServiceDossier();
        this.categorieService = new ServiceCategorie();
        this.ocrService = new OcrService();
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
        if (isEdit && editingDoc != null)
            tfTitre.setText(editingDoc.getTitre());

        TextArea taDescription = new TextArea();
        taDescription.getStyleClass().add("modern-input");
        taDescription.setPromptText("Description...");
        taDescription.setPrefRowCount(4);
        taDescription.setWrapText(true);
        if (isEdit && editingDoc != null)
            taDescription.setText(editingDoc.getDescription());

        ComboBox<Dossier> cbDossier = new ComboBox<>();
        cbDossier.getStyleClass().add("modern-input");
        cbDossier.setPromptText("Sélectionner un dossier");
        try {
            cbDossier.setItems(FXCollections.observableArrayList(dossierService.findAll()));
            if (isEdit && editingDoc != null)
                cbDossier.setValue(editingDoc.getDossier());
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les dossiers");
        }

        ComboBox<Categorie> cbCategorie = new ComboBox<>();
        cbCategorie.getStyleClass().add("modern-input");
        cbCategorie.setPromptText("Sélectionner une catégorie");
        try {
            cbCategorie.setItems(FXCollections.observableArrayList(categorieService.findAll()));
            if (isEdit && editingDoc != null)
                cbCategorie.setValue(editingDoc.getCategorie());
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les catégories");
        }

        // --- Logique "Très Pro" d'auto-classification ---
        tfTitre.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !tfTitre.getText().trim().isEmpty() && !isEdit) { // À la perte du focus (seulement lors de
                                                                             // l'ajout)
                autoClassifyDocument(tfTitre.getText().trim(), cbDossier, cbCategorie);
            }
        });

        TextField tfFilePath = new TextField();
        tfFilePath.getStyleClass().add("modern-input");
        tfFilePath.setPromptText("Chemin du fichier");
        tfFilePath.setEditable(false);
        if (isEdit && editingDoc != null)
            tfFilePath.setText(editingDoc.getFilePath());

        Button btnBrowse = new Button("📁 Parcourir");
        btnBrowse.getStyleClass().add("btn-secondary");
        btnBrowse.setStyle("-fx-padding: 8 15; -fx-font-size: 11;");
        btnBrowse.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir un fichier");
            File f = fc.showOpenDialog(dialog.getOwner());
            if (f != null)
                tfFilePath.setText(f.getAbsolutePath());
        });

        HBox fileBox = new HBox(10, tfFilePath, btnBrowse);
        fileBox.setPrefHeight(40);
        HBox.setHgrow(tfFilePath, Priority.ALWAYS);

        // Champ Montant (obligatoire) et Devise
        TextField tfMontant = new TextField();
        tfMontant.getStyleClass().add("modern-input");
        tfMontant.setPromptText("Ex: 250.00");
        tfMontant.setStyle("-fx-control-inner-background: #fffbea;");

        ComboBox<String> cbCurrency = new ComboBox<>(FXCollections.observableArrayList("TND", "EUR", "USD"));
        cbCurrency.getStyleClass().add("modern-input");
        cbCurrency.setValue("TND");
        cbCurrency.setPrefWidth(100);

        if (isEdit && editingDoc != null) {
            tfMontant.setText(String.valueOf(
                    editingDoc.getOriginalAmount() > 0 ? editingDoc.getOriginalAmount() : editingDoc.getMontant()));
            cbCurrency.setValue(editingDoc.getCurrency() != null ? editingDoc.getCurrency() : "TND");
        }

        Label lblConverted = new Label("");
        lblConverted.setStyle("-fx-text-fill: #059669; -fx-font-size: 11px; -fx-font-weight: bold;");

        // Logique de conversion automatique
        Runnable updateConversion = () -> {
            try {
                double val = Double.parseDouble(tfMontant.getText());
                String from = cbCurrency.getValue();
                if (from.equals("TND")) {
                    lblConverted.setText("");
                } else {
                    double converted = utils.CurrencyService.convert(val, from, "TND");
                    lblConverted.setText(String.format("≈ %.2f TND (Taux du jour)", converted));
                }
            } catch (Exception e) {
                lblConverted.setText("");
            }
        };

        tfMontant.textProperty().addListener((obs, oldV, newV) -> updateConversion.run());
        cbCurrency.valueProperty().addListener((obs, oldV, newV) -> updateConversion.run());

        VBox amountBox = new VBox(5, new HBox(10, tfMontant, cbCurrency), lblConverted);
        HBox.setHgrow(tfMontant, Priority.ALWAYS);

        // Bouton Scanner
        Button btnScanner = new Button("📸 Scanner (OCR)");
        btnScanner.getStyleClass().addAll("btn-primary");
        btnScanner.setStyle(
                "-fx-background-color: linear-gradient(to right, #ec4899, #8b5cf6);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 20;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(236, 72, 153, 0.4), 8, 0, 0, 4);");
        // Champs Dates (Déplacés pour scope)
        DatePicker dpDateFacture = new DatePicker();
        dpDateFacture.getStyleClass().add("modern-input");
        dpDateFacture.setPromptText("Sélectionner la date de facture");
        dpDateFacture.setMaxWidth(Double.MAX_VALUE);
        if (isEdit && editingDoc != null && editingDoc.getDateFacture() != null) {
            dpDateFacture.setValue(editingDoc.getDateFacture());
        }

        DatePicker dpDateLimite = new DatePicker();
        dpDateLimite.getStyleClass().add("modern-input");
        dpDateLimite.setPromptText("Sélectionner la date limite");
        dpDateLimite.setMaxWidth(Double.MAX_VALUE);
        if (isEdit && editingDoc != null && editingDoc.getDateLimitePaiement() != null) {
            dpDateLimite.setValue(editingDoc.getDateLimitePaiement());
        }

        btnScanner
                .setOnAction(e -> showScannerSourceDialog(tfTitre, tfMontant, dpDateFacture, dpDateLimite, tfFilePath));

        HBox headerBox = new HBox(btnScanner);
        headerBox.setAlignment(Pos.CENTER_RIGHT);
        headerBox.setPadding(new javafx.geometry.Insets(0, 0, 15, 0));

        // -- Section Informations Générales --
        Label lblSectionInfo = new Label("📋 Informations Générales");
        lblSectionInfo
                .setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #4b5563; -fx-padding: 10 0 5 0;");

        GridPane gridInfo = createGrid();
        gridInfo.add(createModernLabel("Titre *"), 0, 0);
        gridInfo.add(tfTitre, 1, 0);
        gridInfo.add(createModernLabel("Description"), 0, 1);
        gridInfo.add(taDescription, 1, 1);
        gridInfo.add(createModernLabel("Dossier *"), 0, 2);
        gridInfo.add(cbDossier, 1, 2);
        gridInfo.add(createModernLabel("Catégorie *"), 0, 3);
        gridInfo.add(cbCategorie, 1, 3);
        gridInfo.add(createModernLabel("Fichier *"), 0, 4);
        gridInfo.add(fileBox, 1, 4);

        // -- Section Financière --
        Label lblSectionFinance = new Label("💰 Détails Financiers");
        lblSectionFinance
                .setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #4b5563; -fx-padding: 15 0 5 0;");

        GridPane gridFinance = createGrid();
        gridFinance.add(createModernLabel("Date Facture"), 0, 0);
        gridFinance.add(dpDateFacture, 1, 0);
        gridFinance.add(createModernLabel("Date Limite"), 0, 1);
        gridFinance.add(dpDateLimite, 1, 1);
        gridFinance.add(createModernLabel("Montant *"), 0, 2);
        gridFinance.add(amountBox, 1, 2);

        // Assemblage
        VBox mainContainer = new VBox(5, headerBox, lblSectionInfo, gridInfo, lblSectionFinance, gridFinance);
        mainContainer.setPadding(new javafx.geometry.Insets(10));

        ScrollPane scroll = new ScrollPane(mainContainer);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(450); // Hauteur fixe pour assurer la visibilité des boutons
        scroll.setStyle(
                "-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0; -fx-border-width: 0;");
        dialog.getDialogPane().setContent(scroll);

        // Boutons
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null)
            okButton.getStyleClass().add("btn-primary");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null)
            cancelButton.getStyleClass().add("btn-secondary");

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
                        return;
                    }
                }

                try {
                    boolean pathExists = false;
                    if (isEdit && editingDoc != null) {
                        pathExists = docService.existsByFilePathExceptId(path, editingDoc.getId());
                    } else {
                        pathExists = docService.existsByFilePath(path);
                    }
                    if (pathExists) {
                        AlertUtils.showError("Doublon détecté",
                                "Le chemin du fichier est déjà utilisé par un autre document.\nVeuillez choisir un fichier unique.");
                        tfFilePath.requestFocus();
                        event.consume();
                        return;
                    }
                } catch (SQLException e) {
                    AlertUtils.showError("Erreur SQL", "Impossible de vérifier l'unicité du chemin de fichier.");
                    event.consume();
                    return;
                }
            });
        }

        // Résultat
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                if (tfTitre.getText().isEmpty() || cbDossier.getValue() == null || cbCategorie.getValue() == null
                        || tfFilePath.getText().isEmpty() || tfMontant.getText().isEmpty()) {
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
                    double rawAmount = Double.parseDouble(tfMontant.getText());
                    String currency = cbCurrency.getValue();

                    if (rawAmount < 0) {
                        AlertUtils.showError("Erreur", "Le montant ne peut pas être négatif");
                        return null;
                    }

                    doc.setCurrency(currency);
                    doc.setOriginalAmount(rawAmount);

                    if (currency.equals("TND")) {
                        doc.setMontant(rawAmount);
                    } else {
                        double converted = utils.CurrencyService.convert(rawAmount, currency, "TND");
                        doc.setMontant(converted);
                    }
                } catch (NumberFormatException e) {
                    AlertUtils.showError("Erreur", "Le montant doit être un nombre valide");
                    return null;
                }

                doc.setDateFacture(dpDateFacture.getValue());
                doc.setDateLimitePaiement(dpDateLimite.getValue());

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
                "Le fichier \"" + doc.getFilePath() + "\" sera supprimé.");
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
        if (isEdit && editingFolder != null)
            tfNom.setText(editingFolder.getNom());

        TextArea taDescription = new TextArea();
        taDescription.getStyleClass().add("modern-input");
        taDescription.setPromptText("Description...");
        taDescription.setPrefRowCount(4);
        taDescription.setWrapText(true);
        if (isEdit && editingFolder != null)
            taDescription.setText(editingFolder.getDescription());

        GridPane grid = createGrid();
        grid.add(createModernLabel("Nom *"), 0, 0);
        grid.add(tfNom, 1, 0);
        grid.add(createModernLabel("Description"), 0, 1);
        grid.add(taDescription, 1, 1);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        dialog.getDialogPane().setContent(scroll);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null)
            okButton.getStyleClass().add("btn-primary");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null)
            cancelButton.getStyleClass().add("btn-secondary");

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
                "Tous les documents contenus dans ce dossier resteront accessibles mais ne seront plus classés.");
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
        if (isEdit && editingCategory != null)
            tfNom.setText(editingCategory.getNom());

        TextArea taDescription = new TextArea();
        taDescription.getStyleClass().add("modern-input");
        taDescription.setPromptText("Description...");
        taDescription.setPrefRowCount(4);
        taDescription.setWrapText(true);
        if (isEdit && editingCategory != null)
            taDescription.setText(editingCategory.getDescription());

        GridPane grid = createGrid();
        grid.add(createModernLabel("Nom *"), 0, 0);
        grid.add(tfNom, 1, 0);
        grid.add(createModernLabel("Description"), 0, 1);
        grid.add(taDescription, 1, 1);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        dialog.getDialogPane().setContent(scroll);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null)
            okButton.getStyleClass().add("btn-primary");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null)
            cancelButton.getStyleClass().add("btn-secondary");

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
                "Les documents associés à cette catégorie resteront mais perdront leur classification.");
    }

    // ===== UTILITAIRES =====

    /**
     * Auto-classification intelligente basée sur le titre du document
     * Catégorie = 1er mot
     * Dossier = 2ème mot (sinon 1er mot)
     */
    private void autoClassifyDocument(String titre, ComboBox<Dossier> cbDossier, ComboBox<Categorie> cbCategorie) {
        // Découpage intelligent par espaces, tirets, underscores, deux-points, etc.
        String[] rawWords = titre.split("[\\s\\-_:;.,/]+");

        // Filtrer les mots vides ou insignifiants (moins de 2 caractères)
        java.util.List<String> wordsList = new java.util.ArrayList<>();
        for (String w : rawWords) {
            String trimmed = w.trim();
            if (trimmed.length() >= 2) {
                wordsList.add(trimmed);
            }
        }

        if (wordsList.isEmpty())
            return;

        String keywordCat = wordsList.get(0);

        // Capitaliser la première lettre
        keywordCat = keywordCat.substring(0, 1).toUpperCase() + keywordCat.substring(1).toLowerCase();
        final String finalCatKeyword = keywordCat;

        // --- Catégorie ---
        boolean catFound = cbCategorie.getItems().stream()
                .anyMatch(c -> c.getNom().equalsIgnoreCase(finalCatKeyword));

        if (catFound) {
            cbCategorie.getItems().stream()
                    .filter(c -> c.getNom().equalsIgnoreCase(finalCatKeyword))
                    .findFirst()
                    .ifPresent(cbCategorie::setValue);
        } else {
            try {
                Categorie newCat = new Categorie();
                newCat.setNom(finalCatKeyword);
                newCat.setDescription("Catégorie auto-générée depuis le titre");
                categorieService.add(newCat);

                cbCategorie.setItems(FXCollections.observableArrayList(categorieService.findAll()));
                cbCategorie.getItems().stream()
                        .filter(c -> c.getNom().equalsIgnoreCase(finalCatKeyword))
                        .findFirst()
                        .ifPresent(cbCategorie::setValue);

                cbCategorie.setStyle(cbCategorie.getStyle()
                        + "-fx-border-color: #28a745; -fx-border-width: 2px; -fx-border-radius: 4px;");
            } catch (SQLException e) {
                System.err.println("Erreur auto-création catégorie : " + e.getMessage());
            }
        }

        // --- Dossier ---
        String keywordDos = wordsList.size() > 1 ? wordsList.get(1) : keywordCat;
        if (keywordDos.equalsIgnoreCase(keywordCat) && wordsList.size() > 2) {
            keywordDos = wordsList.get(2); // Essayer d'utiliser le 3ème mot si le 2ème est redondant
        }

        keywordDos = keywordDos.substring(0, 1).toUpperCase() + keywordDos.substring(1).toLowerCase();
        final String finalDosKeyword = keywordDos;

        boolean dosFound = cbDossier.getItems().stream()
                .anyMatch(d -> d.getNom().equalsIgnoreCase(finalDosKeyword));

        if (dosFound) {
            cbDossier.getItems().stream()
                    .filter(d -> d.getNom().equalsIgnoreCase(finalDosKeyword))
                    .findFirst()
                    .ifPresent(cbDossier::setValue);
        } else {
            try {
                Dossier newDos = new Dossier();
                newDos.setNom(finalDosKeyword);
                newDos.setDescription("Dossier auto-généré depuis le titre");
                dossierService.add(newDos);

                cbDossier.setItems(FXCollections.observableArrayList(dossierService.findAll()));
                cbDossier.getItems().stream()
                        .filter(d -> d.getNom().equalsIgnoreCase(finalDosKeyword))
                        .findFirst()
                        .ifPresent(cbDossier::setValue);

                cbDossier.setStyle(cbDossier.getStyle()
                        + "-fx-border-color: #28a745; -fx-border-width: 2px; -fx-border-radius: 4px;");
            } catch (SQLException e) {
                System.err.println("Erreur auto-création dossier : " + e.getMessage());
            }
        }
    }

    /**
     * Crée une GridPane avec les styles appropriés
     */
    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        grid.getStylesheets().add("styles.css");

        return grid;
    }

    /**
     * Affiche une boîte de dialogue professionnelle pour choisir la source du scan.
     */
    private void showScannerSourceDialog(TextField tfTitre, TextField tfMontant, DatePicker dpFacture,
            DatePicker dpLimite,
            TextField tfFile) {
        Dialog<String> sourceDialog = new Dialog<>();
        sourceDialog.setTitle("Source du Scanner");
        sourceDialog.setHeaderText("Comment souhaitez-vous numériser le document ?");
        UiStyles.applyDialogStyles(sourceDialog.getDialogPane());

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f8fafc;");

        HBox optionsBox = new HBox(20);
        optionsBox.setAlignment(Pos.CENTER);

        // Option 1: Importer Fichier
        VBox btnFile = createSourceOption("📁", "Importer un fichier", "Sélectionner une image ou un PDF");
        btnFile.setOnMouseClicked(e -> {
            sourceDialog.setResult("FILE");
            sourceDialog.close();
        });

        // Option 2: Caméra
        VBox btnCamera = createSourceOption("📸", "Scanner via Caméra", "Utiliser la webcam en direct");
        btnCamera.setOnMouseClicked(e -> {
            sourceDialog.setResult("CAMERA");
            sourceDialog.close();
        });

        optionsBox.getChildren().addAll(btnFile, btnCamera);

        Button btnCancel = new Button("Annuler");
        btnCancel.getStyleClass().add("btn-secondary");
        btnCancel.setOnAction(e -> {
            sourceDialog.setResult("CANCEL");
            sourceDialog.close();
        });

        root.getChildren().addAll(optionsBox, btnCancel);
        sourceDialog.getDialogPane().setContent(root);

        Optional<String> result = sourceDialog.showAndWait();
        result.ifPresent(choice -> {
            if ("FILE".equals(choice)) {
                FileChooser fc = new FileChooser();
                fc.setTitle("Sélectionner le document à scanner");
                fc.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Images/PDF", "*.jpg", "*.png", "*.pdf"));
                File f = fc.showOpenDialog(null);
                if (f != null) {
                    tfFile.setText(f.getAbsolutePath());
                    performRealOcr(f, tfTitre, tfMontant, dpFacture, dpLimite, "Analyse du fichier...");
                }
            } else if ("CAMERA".equals(choice)) {
                CameraCaptureDialog cameraDialog = new CameraCaptureDialog();
                cameraDialog.showAndWait().ifPresent(file -> {
                    tfFile.setText(file.getAbsolutePath());
                    performRealOcr(file, tfTitre, tfMontant, dpFacture, dpLimite, "Traitement de l'image capturée...");
                });
            }
        });
    }

    /**
     * Effectue une véritable extraction OCR et remplit les champs.
     */
    private void performRealOcr(File file, TextField tfTitre, TextField tfMontant, DatePicker dpFacture,
            DatePicker dpLimite, String initialMsg) {
        Dialog<Void> scanDialog = new Dialog<>();
        scanDialog.setTitle("Scanner OCR");
        scanDialog.setHeaderText("Analyse intelligente du document...");

        VBox scanBox = new VBox(15);
        scanBox.setAlignment(Pos.CENTER);
        scanBox.setPadding(new Insets(20));
        scanBox.setStyle(
                "-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-border-color: #8b5cf6; -fx-border-width: 0.5;");

        Label lblStatus = new Label(initialMsg);
        lblStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #4b5563;");

        ProgressBar pb = new ProgressBar(-1);
        pb.setPrefWidth(350);
        pb.setStyle("-fx-accent: #8b5cf6;");

        scanBox.getChildren().addAll(lblStatus, pb);
        scanDialog.getDialogPane().setContent(scanBox);
        scanDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        new Thread(() -> {
            try {
                // Temps artificiel court pour l'effet visuel "Pro"
                Thread.sleep(1200);
                Map<String, Object> data = ocrService.extractData(file);

                Platform.runLater(() -> {
                    if (data.containsKey("titre"))
                        tfTitre.setText((String) data.get("titre"));
                    if (data.containsKey("montant"))
                        tfMontant.setText((String) data.get("montant"));
                    if (data.containsKey("date_facture"))
                        dpFacture.setValue((LocalDate) data.get("date_facture"));
                    if (data.containsKey("date_limite"))
                        dpLimite.setValue((LocalDate) data.get("date_limite"));

                    lblStatus.setText("✅ Analyse terminée avec succès !");
                    lblStatus.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    pb.setProgress(1.0);

                    AlertUtils.showSuccess("OCR Terminé",
                            "Les données ont été extraites avec succès du document professionnel.");

                    // Fermeture auto
                    new Thread(() -> {
                        try {
                            Thread.sleep(1500);
                        } catch (Exception ignored) {
                        }
                        Platform.runLater(scanDialog::close);
                    }).start();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    AlertUtils.showError("Erreur OCR",
                            "Une erreur est survenue lors de l'extraction: " + ex.getMessage());
                    scanDialog.close();
                });
            }
        }).start();

        scanDialog.showAndWait();
    }

    private VBox createSourceOption(String icon, String title, String subtitle) {
        VBox box = new VBox(10);
        box.getStyleClass().add("source-button");
        box.setAlignment(Pos.CENTER);

        Label lblIcon = new Label(icon);
        lblIcon.getStyleClass().add("source-button-icon");

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("source-button-label");

        Label lblSub = new Label(subtitle);
        lblSub.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        box.getChildren().addAll(lblIcon, lblTitle, lblSub);
        return box;
    }

    private Label createModernLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-font-size: 13px;");
        return label;
    }
}
