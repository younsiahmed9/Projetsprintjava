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
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

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

    @FXML private ListView<Dossier> listFolders;
    @FXML private ListView<Document> listDocuments;

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
        setupListViews();
        loadAllData();
        setupEventHandlers();
        updateDocumentCount();
    }

    // ===== SETUP =====

    private void setupListViews() {
        listFolders.setCellFactory(param -> new DocumentFolderListCell());
        listFolders.setOnMouseClicked(event -> onFolderSelected());

        listDocuments.setCellFactory(param -> new DocumentCardListCell());
        listDocuments.setOnMouseClicked(event -> onDocumentSelected());
    }

    private void setupEventHandlers() {
        btnAddDocument.setOnAction(e -> createDocument());
        btnAddFolder.setOnAction(e -> createFolder());
        btnManageFolders.setOnAction(e -> manageFolders());
        btnManageCategories.setOnAction(e -> manageCategories());
        btnAllDocuments.setOnAction(e -> showAllDocuments());
        btnRefresh.setOnAction(e -> refresh());

        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> filterDocuments());
        cbCategory.valueProperty().addListener((obs, oldVal, newVal) -> filterDocuments());

        // Clic simple sur un dossier pour filtrer
        listFolders.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedFolder = newVal;
                updateDocumentList();
                updateFilterInfo();
            }
        });

        // Double-clic sur document pour éditer
        listDocuments.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Document selected = listDocuments.getSelectionModel().getSelectedItem();
                if (selected != null) editDocument(selected);
            }
        });

        // Double-clic sur dossier pour éditer
        listFolders.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Dossier selected = listFolders.getSelectionModel().getSelectedItem();
                if (selected != null) editFolder(selected);
            }
        });
    }

    // ===== CHARGEMENT DONNÉES =====

    private void loadAllData() {
        loadFolders();
        loadCategories();
        loadDocuments();
    }

    private void loadFolders() {
        try {
            List<Dossier> dossiers = dossierService.findAll();
            listFolders.setItems(FXCollections.observableArrayList(dossiers));
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

        listDocuments.setItems(FXCollections.observableArrayList(filteredDocs));
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

    private void onDocumentSelected() {
        Document selected = listDocuments.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateStatus("Sélectionné: " + selected.getTitre());
            // Optionnel : afficher menu contextuel ou détails
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
        dialog.setTitle("Gérer les dossiers");
        dialog.setHeaderText("Créer, modifier ou supprimer des dossiers");
        dialog.setWidth(700);
        dialog.setHeight(600);

        ListView<Dossier> folderList = new ListView<>();
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
        content.setStyle("-fx-padding: 20;");
        content.getChildren().addAll(
            new Label("Liste des dossiers (clic-droit pour éditer/supprimer) :"),
            folderList,
            new Label("Ou créer un nouveau dossier :"),
            new Button("+ Ajouter un dossier") {{
                setStyle("-fx-padding: 8 15; -fx-font-size: 12;");
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
        listFolders.getSelectionModel().clearSelection();
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
        listFolders.getSelectionModel().clearSelection();
        listDocuments.getSelectionModel().clearSelection();
        updateFilterInfo();
        updateStatus("Données rafraîchies");
    }

    private void updateDocumentCount() {
        int count = listDocuments.getItems().size();
        lblDocCount.setText(count + " document" + (count > 1 ? "s" : ""));
    }

    private void updateStatus(String message) {
        lblStatus.setText(message);
    }
}

