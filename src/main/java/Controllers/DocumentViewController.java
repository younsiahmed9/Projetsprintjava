package Controllers;

import Controllers.Dialogs.CrudDialogManager;
import Models.Categorie;
import Models.Document;
import Models.Dossier;
import Services.ServiceCategorie;
import Services.ServiceDocument;
import Services.ServiceDossier;
import Services.ServiceDoublon;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.UiStyles;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentViewController {

    @FXML
    private Button btnAddDocument;
    @FXML
    private Button btnManageFolders;
    @FXML
    private Button btnAddFolder;
    @FXML
    private Button btnAllDocuments;
    @FXML
    private Button btnManageCategories;
    @FXML
    private Button btnRefresh;
    @FXML
    private Button btnTrash;
    @FXML
    private Button btnGoToDashboard;

    @FXML
    private VBox containerFolders;
    @FXML
    private VBox containerDocuments;

    @FXML
    private TextField tfSearch;
    @FXML
    private ComboBox<Categorie> cbCategory;

    @FXML
    private Label lblDocCount;
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblFilterInfo;

    private final ServiceDocument docService = new ServiceDocument();
    private final ServiceDossier dossierService = new ServiceDossier();
    private final ServiceCategorie categorieService = new ServiceCategorie();
    private final CrudDialogManager dialogManager = new CrudDialogManager();

    private static DocumentViewController instance;
    private Dossier selectedFolder;
    private List<Document> allDocuments;

    public DocumentViewController() {
        instance = this;
    }

    public static DocumentViewController getCurrentInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        loadAllData();
        setupEventHandlers();
        updateDocumentCount();
        setupEmptyState();
        try {
            docService.purgeOldDeletedDocuments();
        } catch (SQLException e) {
            System.err.println("Erreur purge corbeille: " + e.getMessage());
        }
    }

    private void setupEventHandlers() {
        btnAddDocument.setOnAction(e -> createDocument());
        btnAddFolder.setOnAction(e -> createFolder());
        btnManageFolders.setOnAction(e -> manageFolders());
        btnManageCategories.setOnAction(e -> manageCategories());
        btnAllDocuments.getStyleClass().add("nav-item");
        btnAllDocuments.setOnAction(e -> showAllDocuments());
        btnTrash.setOnAction(e -> showTrash());
        btnRefresh.setOnAction(e -> refresh());
        if (btnGoToDashboard != null) {
            btnGoToDashboard.setOnAction(e -> MainController.getInstance().loadDashboardView());
        }

        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> filterDocuments());
        cbCategory.valueProperty().addListener((obs, oldVal, newVal) -> filterDocuments());
    }

    private VBox createFolderCard(Dossier dossier) {
        VBox card = new VBox(8);
        card.getStyleClass().add("folder-card-modern");

        Label lblNom = new Label("📁 " + dossier.getNom());
        lblNom.getStyleClass().add("folder-title");

        HBox actionsBox = new HBox(5);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnFilter = new Button("Filtrer");
        btnFilter.getStyleClass().add("btn-yellow-premium");
        btnFilter.getStyleClass().add("btn-filter-small");
        btnFilter.setOnAction(e -> {
            selectedFolder = dossier;
            updateDocumentList();
            updateFilterInfo();
            updateNavigationStyles();
        });

        Button btnEdit = new Button("✎");
        btnEdit.getStyleClass().add("btn-icon-small");
        btnEdit.getStyleClass().add("btn-icon-edit-small");
        btnEdit.setTooltip(new Tooltip("Modifier le dossier"));
        btnEdit.setOnAction(e -> editFolder(dossier));

        Button btnDelete = new Button("✕");
        btnDelete.getStyleClass().add("btn-icon-small");
        btnDelete.getStyleClass().add("btn-icon-delete-small");
        btnDelete.setTooltip(new Tooltip("Supprimer le dossier"));
        btnDelete.setOnAction(e -> deleteFolder(dossier));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        actionsBox.getChildren().addAll(btnFilter, spacer, btnEdit, btnDelete);
        card.getChildren().addAll(lblNom, actionsBox);

        return card;
    }

    private VBox createDocumentCard(Document doc) {
        VBox card = new VBox(8);
        card.getStyleClass().add("document-card");

        Label lblTitle = new Label("📄 " + doc.getTitre());
        lblTitle.getStyleClass().add("document-title");

        HBox titleBox = new HBox(10, lblTitle);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        // Ajout du badge d'alerte si échéance proche
        if (doc.getDateLimitePaiement() != null) {
            Models.Echeance tempEch = new Models.Echeance();
            tempEch.setDateEcheance(doc.getDateLimitePaiement());
            String urgence = tempEch.getUrgence();
            if (!urgence.equals("NORMAL")) {
                Label lblBadge = new Label(urgence);
                lblBadge.getStyleClass().add("badge-urgent");
                titleBox.getChildren().add(lblBadge);
            }
        }

        Label lblDesc = new Label(doc.getDescription() != null && !doc.getDescription().isEmpty()
                ? doc.getDescription()
                : "(Pas de description)");
        lblDesc.getStyleClass().add("document-description");
        lblDesc.setWrapText(true);

        HBox hboxMeta = new HBox(15);
        Label lblDossier = new Label("📁 " + (doc.getDossier() != null ? doc.getDossier().getNom() : "N/A"));
        lblDossier.getStyleClass().add("document-meta-label");

        Label lblCategorie = new Label("🏷️ " + (doc.getCategorie() != null ? doc.getCategorie().getNom() : "N/A"));
        lblCategorie.getStyleClass().add("document-meta-label");

        hboxMeta.getChildren().addAll(lblDossier, lblCategorie);

        // Affichage du montant avec conversion si nécessaire
        Label lblMontant;
        if (doc.getCurrency() != null && !doc.getCurrency().equals("TND") && doc.getOriginalAmount() > 0) {
            lblMontant = new Label(String.format("💰 %.2f %s (≈ %.2f TND)",
                    doc.getOriginalAmount(), doc.getCurrency(), doc.getMontant()));
        } else {
            lblMontant = new Label(String.format("💰 %.2f TND", doc.getMontant()));
        }
        lblMontant.getStyleClass().add("document-meta-label");
        lblMontant.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;"); // Un beau vert financier

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnDetect = new Button("🔍 Détecter");
        btnDetect.getStyleClass().add("btn-detect");
        btnDetect.setOnAction(e -> detectSimilarDocuments(doc));

        Button btnView = new Button("Détail");
        btnView.getStyleClass().add("btn-orange-small");
        btnView.setOnAction(e -> showDocumentDetail(doc));

        Button btnEdit = new Button("Modifier");
        btnEdit.getStyleClass().add("btn-modify-small");
        btnEdit.setOnAction(e -> editDocument(doc));

        Button btnDelete = new Button("Supprimer");
        btnDelete.getStyleClass().add("btn-delete-small");
        btnDelete.setOnAction(e -> deleteDocument(doc));

        btnBox.getChildren().addAll(btnDetect, btnView, btnEdit, btnDelete);

        card.getChildren().addAll(titleBox, lblDesc, hboxMeta, lblMontant, btnBox);
        card.setUserData(doc.getId()); // Store ID for selection
        return card;
    }

    private void analyzeWithAi(Document doc) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("AI Document Analysis");
        info.setHeaderText("Analyzing " + doc.getTitre() + "...");
        info.setContentText("Contacting Gemini AI for document summarization and classification...");
        info.show();

        new Thread(() -> {
            String result = utils.GeminiService.analyzeDocument(doc.getDescription() + " " + doc.getTitre());
            javafx.application.Platform.runLater(() -> {
                info.close();
                Alert resAlert = new Alert(Alert.AlertType.INFORMATION);
                resAlert.setTitle("AI Analysis Result");
                resAlert.setHeaderText("Analysis for: " + doc.getTitre());

                TextArea area = new TextArea(result);
                area.setWrapText(true);
                area.setEditable(false);
                resAlert.getDialogPane().setContent(area);
                resAlert.showAndWait();
            });
        }).start();
    }

    private void loadAllData() {
        loadFolders();
        loadCategories();
        loadDocuments();
    }

    private void loadFolders() {
        try {
            containerFolders.getChildren().clear();
            List<Dossier> dossiers = dossierService.findAll();
            for (Dossier dossier : dossiers) {
                containerFolders.getChildren().add(createFolderCard(dossier));
            }
            updateStatus("Dossiers chargés (" + dossiers.size() + ")");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les dossiers: " + e.getMessage());
        }
    }

    private void loadCategories() {
        try {
            List<Categorie> categories = categorieService.findAll();
            cbCategory.setItems(FXCollections.observableArrayList(categories));
            cbCategory.setPromptText("📁 Toutes les catégories");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les catégories");
        }
    }

    private void loadDocuments() {
        try {
            allDocuments = docService.findAll();
            updateDocumentList();
            updateStatus("Documents chargés (" + allDocuments.size() + ")");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les documents");
        }
    }

    private void updateDocumentList() {
        List<Document> filteredDocs = allDocuments;

        if (selectedFolder != null) {
            filteredDocs = filteredDocs.stream()
                    .filter(d -> d.getDossier().getId() == selectedFolder.getId())
                    .collect(Collectors.toList());
        }

        String searchText = tfSearch.getText().toLowerCase();
        if (!searchText.isEmpty()) {
            filteredDocs = filteredDocs.stream()
                    .filter(d -> d.getTitre().toLowerCase().contains(searchText) ||
                            (d.getDescription() != null && d.getDescription().toLowerCase().contains(searchText)))
                    .collect(Collectors.toList());
        }

        Categorie selectedCategory = cbCategory.getValue();
        if (selectedCategory != null) {
            filteredDocs = filteredDocs.stream()
                    .filter(d -> d.getCategorie() != null && d.getCategorie().getId() == selectedCategory.getId())
                    .collect(Collectors.toList());
        }

        containerDocuments.getChildren().clear();
        for (Document doc : filteredDocs) {
            containerDocuments.getChildren().add(createDocumentCard(doc));
        }

        updateDocumentCount();
    }

    private void filterDocuments() {
        updateDocumentList();
    }

    private void createDocument() {
        dialogManager.showDocumentDialog(null, false).ifPresent(doc -> {
            try {
                docService.add(doc);
                AlertUtils.showSuccess("Succès", "Document créé avec succès !");
                loadDocuments();
                updateStatus("Document créé");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de créer le document: " + e.getMessage());
            }
        });
    }

    private void editDocument(Document doc) {
        dialogManager.showDocumentDialog(doc, true).ifPresent(updated -> {
            try {
                docService.update(updated);
                AlertUtils.showSuccess("Succès", "Document modifié avec succès !");
                loadDocuments();
                updateStatus("Document modifié");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de modifier le document: " + e.getMessage());
            }
        });
    }

    private void deleteDocument(Document doc) {
        if (dialogManager.showDeleteDocumentConfirmation(doc)) {
            try {
                docService.delete(doc.getId());
                AlertUtils.showSuccess("Succès", "Document supprimé avec succès !");
                loadDocuments();
                updateStatus("Document supprimé");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de supprimer le document: " + e.getMessage());
            }
        }
    }

    private void detectSimilarDocuments(Document document) {
        try {
            ServiceDoublon doublonService = new ServiceDoublon();
            // Utiliser un seuil par défaut de 70%
            List<ServiceDoublon.DoublonPair> similars = doublonService.findDoublonsOf(document, 0.70);

            if (similars.isEmpty()) {
                AlertUtils.showInfo("Aucun doublon",
                        "Aucun document similaire n'a été trouvé pour : " + document.getTitre());
                return;
            }

            // Créer une alerte personnalisée pour afficher les résultats
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Documents Similaires Détectés");
            alert.setHeaderText("Documents similaires à : " + document.getTitre());

            VBox content = new VBox(10);
            content.setPadding(new javafx.geometry.Insets(10));

            boolean exactMatchFound = false;

            for (ServiceDoublon.DoublonPair pair : similars) {
                Document doc2 = pair.getDoc2();
                boolean exactMatch = isExactMatch(document, doc2);
                if (exactMatch)
                    exactMatchFound = true;

                VBox card = new VBox(5);
                if (exactMatch) {
                    card.setStyle(
                            "-fx-background-color: #fee2e2; -fx-padding: 10; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
                } else {
                    card.setStyle(
                            "-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5;");
                }

                Label lblTitre = new Label("📄 " + doc2.getTitre());
                lblTitre.setStyle("-fx-font-weight: bold;");

                String pathStr = doc2.getDossier() != null ? doc2.getDossier().getNom() : "N/A";
                String catStr = doc2.getCategorie() != null ? doc2.getCategorie().getNom() : "N/A";

                Label lblDetails = new Label(String.format("Dossier: %s | Catégorie: %s", pathStr, catStr));
                lblDetails.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

                Label lblSimilarity;
                if (exactMatch) {
                    lblSimilarity = new Label("⚠️ ATTENTION : Les informations sont identiques !");
                    lblSimilarity.setStyle("-fx-font-weight: bold; -fx-text-fill: #b91c1c;");
                } else {
                    lblSimilarity = new Label("Similarité : " + pair.getSimilarityPercent());
                    lblSimilarity.setStyle(String.format("-fx-font-weight: bold; -fx-text-fill: %s;",
                            pair.getSimilarity() >= 0.9 ? "#dc2626"
                                    : pair.getSimilarity() >= 0.75 ? "#f59e0b" : "#10b981"));
                }

                card.getChildren().addAll(lblTitre, lblDetails, lblSimilarity);
                content.getChildren().add(card);
            }

            if (exactMatchFound) {
                alert.setHeaderText("⚠️ Documents avec les mêmes informations détectés !");
            }

            ScrollPane scroll = new ScrollPane(content);
            scroll.setFitToWidth(true);
            scroll.setPrefViewportHeight(300);

            alert.getDialogPane().setContent(scroll);
            alert.showAndWait();

        } catch (SQLException e) {
            AlertUtils.showError("Erreur d'analyse", "Impossible d'analyser les doublons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isExactMatch(Document d1, Document d2) {
        if (!java.util.Objects.equals(d1.getTitre(), d2.getTitre()))
            return false;

        // Si les deux montants sont > 0 et ne correspondent pas, ce n'est pas un match
        // exact
        if ((d1.getMontant() > 0 || d2.getMontant() > 0) && d1.getMontant() != d2.getMontant())
            return false;

        if (!java.util.Objects.equals(d1.getDescription(), d2.getDescription()))
            return false;

        Integer d1DosId = d1.getDossier() != null ? d1.getDossier().getId() : null;
        Integer d2DosId = d2.getDossier() != null ? d2.getDossier().getId() : null;
        if (!java.util.Objects.equals(d1DosId, d2DosId))
            return false;

        Integer d1CatId = d1.getCategorie() != null ? d1.getCategorie().getId() : null;
        Integer d2CatId = d2.getCategorie() != null ? d2.getCategorie().getId() : null;
        if (!java.util.Objects.equals(d1CatId, d2CatId))
            return false;

        return true;
    }

    private void createFolder() {
        dialogManager.showFolderDialog(null, false).ifPresent(folder -> {
            try {
                dossierService.add(folder);
                AlertUtils.showSuccess("Succès", "Dossier créé avec succès !");
                loadFolders();
                updateStatus("Dossier créé");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de créer le dossier: " + e.getMessage());
            }
        });
    }

    private void editFolder(Dossier folder) {
        dialogManager.showFolderDialog(folder, true).ifPresent(updated -> {
            try {
                dossierService.update(updated);
                AlertUtils.showSuccess("Succès", "Dossier modifié avec succès !");
                loadFolders();
                loadDocuments();
                updateStatus("Dossier modifié");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de modifier le dossier: " + e.getMessage());
            }
        });
    }

    private void deleteFolder(Dossier folder) {
        if (dialogManager.showDeleteFolderConfirmation(folder)) {
            try {
                dossierService.delete(folder.getId());
                AlertUtils.showSuccess("Succès", "Dossier supprimé avec succès !");
                loadFolders();
                loadDocuments();
                selectedFolder = null;
                updateStatus("Dossier supprimé");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de supprimer le dossier: " + e.getMessage());
            }
        }
    }

    private void manageCategories() {
        CategoryManagerDialog manager = new CategoryManagerDialog();
        manager.showCategoryManager();
        loadCategories(); // Refresh filters after management
        updateStatus("Gestion des catégories terminée");
    }

    private void showAllDocuments() {
        selectedFolder = null;
        updateDocumentList();
        updateFilterInfo();
        updateNavigationStyles();
        updateStatus("Tous les documents");
    }

    private void updateNavigationStyles() {
        if (selectedFolder == null) {
            if (!btnAllDocuments.getStyleClass().contains("nav-item-active")) {
                btnAllDocuments.getStyleClass().add("nav-item-active");
            }
        } else {
            btnAllDocuments.getStyleClass().remove("nav-item-active");
        }
    }

    private void showTrash() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().loadTrashView();
        } else {
            // Fallback for standalone mode (if any)
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/trash_view.fxml"));
                Scene scene = new Scene(loader.load());
                Stage stage = (Stage) btnTrash.getScene().getWindow();
                stage.setScene(scene);
            } catch (IOException e) {
                AlertUtils.showError("Erreur", "Impossible d'ouvrir la corbeille: " + e.getMessage());
            }
        }
    }

    private void updateFilterInfo() {
        if (selectedFolder != null) {
            lblFilterInfo.setText("📁 " + selectedFolder.getNom());
        } else {
            lblFilterInfo.setText("");
        }
    }

    private void manageFolders() {
        Dialog<Void> dialog = new Dialog<>();
        UiStyles.applyDialogStyles(dialog.getDialogPane());
        dialog.setTitle("Gérer les dossiers");
        dialog.setHeaderText("Créer, modifier ou supprimer des dossiers");
        dialog.setWidth(700);
        dialog.setHeight(600);

        ListView<Dossier> folderList = new ListView<>();
        folderList.getStyleClass().add("folders-list");
        folderList.setPrefHeight(350);

        // Custom Cell Factory for premium look
        folderList.setCellFactory(lv -> new ListCell<Dossier>() {
            @Override
            protected void updateItem(Dossier item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText("📁  " + item.getNom());
                    getStyleClass().add("folder-list-cell");
                }
            }
        });

        try {
            folderList.setItems(FXCollections.observableArrayList(dossierService.findAll()));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les dossiers");
        }

        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("✎ Modifier");
        editItem.setOnAction(e -> {
            Dossier selected = folderList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                editFolder(selected);
                try {
                    folderList.setItems(FXCollections.observableArrayList(dossierService.findAll()));
                } catch (SQLException ex) {
                    AlertUtils.showError("Erreur", ex.getMessage());
                }
            }
        });

        MenuItem deleteItem = new MenuItem("✕ Supprimer");
        deleteItem.setOnAction(e -> {
            Dossier selected = folderList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteFolder(selected);
                try {
                    folderList.setItems(FXCollections.observableArrayList(dossierService.findAll()));
                } catch (SQLException ex) {
                    AlertUtils.showError("Erreur", ex.getMessage());
                }
            }
        });

        contextMenu.getItems().addAll(editItem, deleteItem);
        folderList.setContextMenu(contextMenu);

        VBox content = new VBox(20);
        content.getStyleClass().add("card");
        content.setStyle("-fx-padding: 30; -fx-background-color: white;");

        Label lblInstruction = new Label("Liste des dossiers (clic-droit pour éditer/supprimer) :");
        lblInstruction.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-font-size: 13;");

        Label lblCreate = new Label("Ou créer un nouveau dossier :");
        lblCreate.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-font-size: 13; -fx-padding: 10 0 0 0;");

        Button btnAdd = new Button("+ Ajouter un dossier");
        btnAdd.getStyleClass().add("btn-yellow-premium");
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnAdd.setOnAction(e -> {
            createFolder();
            try {
                folderList.setItems(FXCollections.observableArrayList(dossierService.findAll()));
            } catch (SQLException ex) {
                AlertUtils.showError("Erreur", ex.getMessage());
            }
        });

        content.getChildren().addAll(
                lblInstruction,
                folderList,
                lblCreate,
                btnAdd);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait().ifPresent(v -> {
            try {
                loadFolders();
                loadDocuments();
            } catch (Exception ex) {
                AlertUtils.showError("Erreur", ex.getMessage());
            }
        });
    }

    @FXML
    public void refresh() {
        loadAllData();
        tfSearch.clear();
        cbCategory.setValue(null);
        selectedFolder = null;
        updateFilterInfo();
        updateStatus("Données rafraîchies");
    }

    private void updateDocumentCount() {
        int count = containerDocuments.getChildren().size();
        lblDocCount.setText(count + " document" + (count > 1 ? "s" : ""));
    }

    private void updateStatus(String message) {
        lblStatus.setText(message);
    }

    private void setupEmptyState() {
    }

    private void showDocumentDetail(Document document) {
        try {
            var resource = getClass().getResource("/fxml/document_detail_simple.fxml");
            if (resource == null) {
                AlertUtils.showError("Erreur",
                        "Fichier FXML non trouvé: /fxml/document_detail_simple.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            BorderPane root = loader.load();

            DocumentDetailController controller = loader.getController();
            if (controller == null) {
                AlertUtils.showError("Erreur",
                        "Contrôleur DocumentDetailController non initialisé");
                return;
            }

            Stage detailStage = new Stage();
            detailStage.setTitle("Détail du Document - " + document.getTitre());
            Scene scene = new Scene(root, 1000, 700);
            detailStage.setScene(scene);
            detailStage.setResizable(true);

            controller.setStage(detailStage);
            controller.showDocument(document);

            detailStage.show();

            updateStatus("Détail du document ouvert: " + document.getTitre());
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur IO",
                    "Impossible de charger le fichier FXML:\n" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur",
                    "Erreur inattendue:\n" + e.getMessage());
        }
    }

    /**
     * Sélectionne et met en évidence un document par son ID
     */
    public void selectDocumentById(int id) {
        showAllDocuments(); // Ensure all are shown
        for (javafx.scene.Node node : containerDocuments.getChildren()) {
            if (node instanceof VBox && node.getUserData() != null && (int) node.getUserData() == id) {
                VBox card = (VBox) node;
                // Highlight effect
                card.setStyle(card.getStyle()
                        + "; -fx-border-color: #182d88; -fx-border-width: 2; -fx-background-color: #eff6ff;");

                // Scroll to (requires finding the ScrollPane parent)
                javafx.application.Platform.runLater(() -> {
                    if (card.getScene() != null) {
                        card.requestFocus();
                        // Find ScrollPane
                        javafx.scene.Node parent = card.getParent();
                        while (parent != null && !(parent instanceof ScrollPane)) {
                            parent = parent.getParent();
                        }
                        if (parent instanceof ScrollPane) {
                            ScrollPane sp = (ScrollPane) parent;
                            double vValue = card.getBoundsInParent().getMinY() / containerDocuments.getHeight();
                            sp.setVvalue(vValue);
                        }
                    }
                });
                break;
            }
        }
    }
}
