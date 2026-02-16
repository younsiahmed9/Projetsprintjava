package Controllers;

import Controllers.Dialogs.CrudDialogManager;
import Models.Categorie;
import Models.Document;
import Models.Dossier;
import Services.ServiceCategorie;
import Services.ServiceDocument;
import Services.ServiceDossier;
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

/**
 * Contrôleur principal pour la vue des documents
 * Gère le CRUD complet (Create, Read, Update, Delete) pour :
 * - Documents
 * - Dossiers
 * - Catégories
 */
public class DocumentViewController {

    // ===== ÉLÉMENTS FXML =====
    @FXML private Button btnAddDocument;
    @FXML private Button btnManageFolders;
    @FXML private Button btnAddFolder;
    @FXML private Button btnAllDocuments;
    @FXML private Button btnRefresh;
    @FXML private Button btnManageCategories;

    @FXML private VBox containerFolders;
    @FXML private VBox containerDocuments;

    @FXML private TextField tfSearch;
    @FXML private ComboBox<Categorie> cbCategory;

    @FXML private Label lblDocCount;
    @FXML private Label lblStatus;
    @FXML private Label lblFilterInfo;

    // ===== SERVICES =====
    private final ServiceDocument docService = new ServiceDocument();
    private final ServiceDossier dossierService = new ServiceDossier();
    private final ServiceCategorie categorieService = new ServiceCategorie();
    private final CrudDialogManager dialogManager = new CrudDialogManager();

    // ===== ÉTAT =====
    private Dossier selectedFolder;
    private List<Document> allDocuments;

    @FXML
    public void initialize() {
        loadAllData();
        setupEventHandlers();
        updateDocumentCount();
        setupEmptyState();
    }

    // ===== SETUP =====


    private void setupEventHandlers() {
        btnAddDocument.setOnAction(e -> createDocument());
        btnAddFolder.setOnAction(e -> createFolder());
        btnManageFolders.setOnAction(e -> manageFolders());
        btnManageCategories.setOnAction(e -> manageCategories());
        btnAllDocuments.setOnAction(e -> showAllDocuments());
        btnRefresh.setOnAction(e -> refresh());

        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> filterDocuments());
        cbCategory.valueProperty().addListener((obs, oldVal, newVal) -> filterDocuments());
    }

    // ===== CRÉATION DES CARTES =====

    /**
     * Crée une card personnalisée pour un dossier
     */
    private VBox createFolderCard(Dossier dossier) {
        VBox card = new VBox(6);
        card.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

        Label lblNom = new Label("📁 " + dossier.getNom());
        lblNom.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #182d88;");

        HBox btnBox = new HBox(6);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnSelect = new Button("Filtrer");
        btnSelect.getStyleClass().add("btn-secondary");
        btnSelect.setStyle("-fx-font-size: 10; -fx-min-width: 70;");
        btnSelect.setOnAction(e -> {
            selectedFolder = dossier;
            updateDocumentList();
            updateFilterInfo();
        });

        btnBox.getChildren().add(btnSelect);
        card.getChildren().addAll(lblNom, btnBox);

        return card;
    }

    /**
     * Crée une card personnalisée pour un document
     */
    private VBox createDocumentCard(Document doc) {
        VBox card = new VBox(5);
        card.setStyle("-fx-padding: 12; -fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

        Label lblTitle = new Label("📄 " + doc.getTitre());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #1d4ed8;");


        Label lblDesc = new Label(doc.getDescription() != null && !doc.getDescription().isEmpty()
                ? doc.getDescription() : "(Pas de description)");
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

        Button btnView = new Button("Détail");
        btnView.getStyleClass().add("btn-primary");
        btnView.setStyle("-fx-font-size: 10; -fx-min-width: 70;");
        btnView.setOnAction(e -> showDocumentDetail(doc));

        Button btnEdit = new Button("Modifier");
        btnEdit.getStyleClass().add("btn-modify");
        btnEdit.setStyle("-fx-font-size: 10; -fx-min-width: 70;");
        btnEdit.setOnAction(e -> editDocument(doc));

        Button btnDelete = new Button("Supprimer");
        btnDelete.getStyleClass().add("btn-delete");
        btnDelete.setStyle("-fx-font-size: 10; -fx-min-width: 70;");
        btnDelete.setOnAction(e -> deleteDocument(doc));

        btnBox.getChildren().addAll(btnView, btnEdit, btnDelete);

        card.getChildren().addAll(lblTitle, lblDesc, hboxMeta, btnBox);
        return card;
    }

    // ===== CHARGEMENT DONNÉES =====

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

    // ===== FILTRAGE =====

    private void updateDocumentList() {
        List<Document> filteredDocs = allDocuments;

        if (selectedFolder != null) {
            filteredDocs = filteredDocs.stream()
                    .filter(d -> d.getDossier().getId() == selectedFolder.getId())
                    .toList();
        }

        String searchText = tfSearch.getText().toLowerCase();
        if (!searchText.isEmpty()) {
            filteredDocs = filteredDocs.stream()
                    .filter(d -> d.getTitre().toLowerCase().contains(searchText) ||
                                 (d.getDescription() != null && d.getDescription().toLowerCase().contains(searchText)))
                    .toList();
        }

        Categorie selectedCategory = cbCategory.getValue();
        if (selectedCategory != null) {
            filteredDocs = filteredDocs.stream()
                    .filter(d -> d.getCategorie() != null && d.getCategorie().getId() == selectedCategory.getId())
                    .toList();
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

    // ===== CRUD DOCUMENTS =====

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


    // ===== CRUD DOSSIERS =====

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

    private void onFolderSelected() {
        // Cette méthode n'est plus utilisée - remplacée par le listener dans setupEventHandlers
    }

    private void manageCategories() {
        CategoryManagerDialog manager = new CategoryManagerDialog();
        manager.showCategoryManager();
        // Rafraîchir les catégories après gestion
        loadCategories();
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
        folderList.setPrefHeight(300);
        try {
            folderList.setItems(FXCollections.observableArrayList(dossierService.findAll()));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les dossiers");
        }

        // Menu contextuel
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

        VBox content = new VBox(15);
        content.getStyleClass().add("card");
        content.setStyle("-fx-padding: 20;");
        content.getChildren().addAll(
            new Label("Liste des dossiers (clic-droit pour éditer/supprimer) :"),
            folderList,
            new Label("Ou créer un nouveau dossier :"),
            new Button("+ Ajouter un dossier") {{
                getStyleClass().add("btn-secondary");
                setOnAction(e -> {
                    createFolder();
                    try {
                        folderList.setItems(FXCollections.observableArrayList(dossierService.findAll()));
                    } catch (SQLException ex) {
                        AlertUtils.showError("Erreur", ex.getMessage());
                    }
                });
            }}
        );

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

    // ===== AUTRES =====

    private void showAllDocuments() {
        selectedFolder = null;
        updateDocumentList();
        updateFilterInfo();
        updateStatus("Tous les documents");
    }

    @FXML
    private void refresh() {
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
        // Pas nécessaire avec les cartes VBox
    }

    /**
     * Affiche la fenêtre de détail d'un document
     */
    private void showDocumentDetail(Document document) {
        try {
            // Vérifier que la ressource existe
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
}
