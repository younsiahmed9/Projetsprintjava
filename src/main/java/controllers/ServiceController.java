package controllers;

import services.ServicePersonne;
import models.Service;
import models.Produit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Optional;

public class ServiceController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<Service.TypeService> filterType;
    @FXML private ComboBox<Service.StatutService> filterStatut;
    @FXML private FlowPane servicesFlow;
    @FXML private FlowPane produitsFlow;
    @FXML private Text totalServices;
    @FXML private Text totalProduits;

    // Éléments de navigation
    @FXML private HBox navDashboard;
    @FXML private HBox navServices;
    @FXML private HBox navComptes;
    @FXML private HBox navDepenses;
    @FXML private HBox navDocuments;
    @FXML private HBox navPaiements;

    private ServicePersonne servicePersonne;
    private ObservableList<Service> serviceList;
    private ObservableList<Produit> produitList;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        servicePersonne = new ServicePersonne();
        serviceList = FXCollections.observableArrayList();
        produitList = FXCollections.observableArrayList();

        setupFilters();
        loadData();
    }

    private void setupFilters() {
        filterType.setItems(FXCollections.observableArrayList(Service.TypeService.values()));
        filterStatut.setItems(FXCollections.observableArrayList(Service.StatutService.values()));

        filterType.setOnAction(e -> filterServices());
        filterStatut.setOnAction(e -> filterServices());
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterServices());
    }

    private void loadData() {
        try {
            List<Service> services = servicePersonne.afficherTousServices();
            serviceList.setAll(services);

            List<Produit> produits = servicePersonne.afficherTousProduits();
            produitList.setAll(produits);

            displayAll();
            updateStats();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStats() {
        try {
            List<Service> allServices = servicePersonne.afficherTousServices();
            List<Produit> allProduits = servicePersonne.afficherTousProduits();

            if (totalServices != null) totalServices.setText(String.valueOf(allServices.size()));
            if (totalProduits != null) totalProduits.setText(String.valueOf(allProduits.size()));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterServices() {
        String searchText = txtSearch.getText().toLowerCase();
        Service.TypeService typeFilter = filterType.getValue();
        Service.StatutService statutFilter = filterStatut.getValue();

        List<Service> filtered = serviceList.stream()
                .filter(s -> searchText.isEmpty() ||
                        s.getNomService().toLowerCase().contains(searchText))
                .filter(s -> typeFilter == null || s.getTypeService() == typeFilter)
                .filter(s -> statutFilter == null || s.getStatut() == statutFilter)
                .toList();

        displayServices(filtered);
    }

    private void displayAll() {
        displayServices(serviceList);
        displayProduits(produitList);
    }

    private void displayServices(List<Service> services) {
        if (servicesFlow != null) servicesFlow.getChildren().clear();

        for (Service service : services) {
            VBox card = createServiceCard(service);
            servicesFlow.getChildren().add(card);
        }
    }

    private void displayProduits(List<Produit> produits) {
        if (produitsFlow != null) produitsFlow.getChildren().clear();

        for (Produit produit : produits) {
            VBox card = createProduitCard(produit);
            produitsFlow.getChildren().add(card);
        }
    }

    private VBox createServiceCard(Service service) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15;");
        card.setPadding(new Insets(20));
        card.setPrefWidth(320);
        card.setMaxWidth(320);
        card.getStyleClass().add("card");

        // Double-clic pour voir les détails
        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openServiceDetails(service);
            }
        });

        // Effet d'ombre
        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setOffsetX(0);
        shadow.setOffsetY(2);
        shadow.setColor(Color.rgb(24, 45, 136, 0.1));
        card.setEffect(shadow);

        // En-tête
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle iconCircle = new Circle(25);
        if (service.getTypeService() == Service.TypeService.abonnement) {
            iconCircle.setFill(Color.web("#8dbc71"));
        } else {
            iconCircle.setFill(Color.web("#f78f34"));
        }

        VBox titleBox = new VBox(5);
        Text title = new Text(service.getNomService());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #182d88;");

        Text type = new Text(service.getTypeService().toString());
        type.setStyle("-fx-font-size: 12px; -fx-fill: #182d88; -fx-opacity: 0.7;");

        titleBox.getChildren().addAll(title, type);
        header.getChildren().addAll(iconCircle, titleBox);

        // Détails
        VBox details = new VBox(8);
        details.setStyle("-fx-padding: 10 0;");

        // Prix
        HBox priceRow = new HBox(10);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        Text priceIcon = new Text("💰");
        Text priceLabel = new Text("Tarif: " + service.getTarif() + " TND");
        priceLabel.setStyle("-fx-font-size: 14px; -fx-fill: #182d88;");
        priceRow.getChildren().addAll(priceIcon, priceLabel);
        details.getChildren().add(priceRow);

        // Infos supplémentaires pour abonnements
        if (service.getTypeService() == Service.TypeService.abonnement) {
            if (service.getFrequence() != null) {
                HBox freqRow = new HBox(10);
                freqRow.setAlignment(Pos.CENTER_LEFT);
                Text freqIcon = new Text("📅");
                Text freqText = new Text("Fréquence: " + service.getFrequence().toString());
                freqText.setStyle("-fx-font-size: 13px; -fx-fill: #182d88; -fx-opacity: 0.7;");
                freqRow.getChildren().addAll(freqIcon, freqText);
                details.getChildren().add(freqRow);
            }

            if (service.getDateDebut() != null) {
                HBox dateRow = new HBox(10);
                dateRow.setAlignment(Pos.CENTER_LEFT);
                Text dateIcon = new Text("⏰");
                Text dateText = new Text("Début: " + service.getDateDebut().format(dateFormatter));
                dateText.setStyle("-fx-font-size: 13px; -fx-fill: #182d88; -fx-opacity: 0.7;");
                dateRow.getChildren().addAll(dateIcon, dateText);
                details.getChildren().add(dateRow);
            }
        }

        // Statut
        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        Circle statusDot = new Circle(6);
        Text statusText = new Text(service.getStatut().toString());
        statusText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        switch (service.getStatut()) {
            case actif:
                statusDot.setFill(Color.web("#8dbc71"));
                statusText.setFill(Color.web("#8dbc71"));
                break;
            case suspendu:
                statusDot.setFill(Color.web("#fecf47"));
                statusText.setFill(Color.web("#fecf47"));
                break;
            case expire:
                statusDot.setFill(Color.web("#f78f34"));
                statusText.setFill(Color.web("#f78f34"));
                break;
        }

        statusRow.getChildren().addAll(statusDot, statusText);

        // Boutons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #182d88; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 12px; -fx-cursor: hand;");
        editBtn.setPadding(new Insets(6, 15, 6, 15));
        editBtn.setOnAction(e -> handleEditService(service));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #f78f34; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 12px; -fx-cursor: hand;");
        deleteBtn.setPadding(new Insets(6, 15, 6, 15));
        deleteBtn.setOnAction(e -> handleDeleteService(service));

        actions.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(header, details, statusRow, actions);
        return card;
    }

    private VBox createProduitCard(Produit produit) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15;");
        card.setPadding(new Insets(20));
        card.setPrefWidth(320);
        card.getStyleClass().add("card");

        // Double-clic pour détails (à implémenter)
        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                showAlert("Info", "Détails du produit à venir");
            }
        });

        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setColor(Color.rgb(24, 45, 136, 0.1));
        card.setEffect(shadow);

        // En-tête
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle iconCircle = new Circle(25);
        iconCircle.setFill(Color.web("#8dbc71"));

        VBox titleBox = new VBox(5);
        Text title = new Text(produit.getNomProduit());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #182d88;");

        Text type = new Text(produit.getTypeProduit().toString());
        type.setStyle("-fx-font-size: 12px; -fx-fill: #182d88; -fx-opacity: 0.7;");

        titleBox.getChildren().addAll(title, type);
        header.getChildren().addAll(iconCircle, titleBox);

        // Détails
        VBox details = new VBox(8);

        HBox montantRow = new HBox(10);
        montantRow.setAlignment(Pos.CENTER_LEFT);
        Text montantIcon = new Text("💰");
        Text montantLabel = new Text("Montant: " + produit.getMontant() + " TND");
        montantLabel.setStyle("-fx-font-size: 14px; -fx-fill: #182d88;");
        montantRow.getChildren().addAll(montantIcon, montantLabel);

        HBox codeRow = new HBox(10);
        codeRow.setAlignment(Pos.CENTER_LEFT);
        Text codeIcon = new Text("🔑");
        Text codeLabel = new Text("Code: " + produit.getCodeUnique());
        codeLabel.setStyle("-fx-font-size: 13px; -fx-fill: #182d88; -fx-opacity: 0.7;");
        codeRow.getChildren().addAll(codeIcon, codeLabel);

        details.getChildren().addAll(montantRow, codeRow);

        // Statut
        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        Circle statusDot = new Circle(6);
        Text statusText = new Text(produit.getStatut().toString());
        statusText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        switch (produit.getStatut()) {
            case disponible:
                statusDot.setFill(Color.web("#8dbc71"));
                statusText.setFill(Color.web("#8dbc71"));
                break;
            case vendu:
                statusDot.setFill(Color.web("#f78f34"));
                statusText.setFill(Color.web("#f78f34"));
                break;
            case expire:
                statusDot.setFill(Color.web("#fecf47"));
                statusText.setFill(Color.web("#fecf47"));
                break;
        }

        statusRow.getChildren().addAll(statusDot, statusText);

        // Boutons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #182d88; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 12px; -fx-cursor: hand;");
        editBtn.setPadding(new Insets(6, 15, 6, 15));
        editBtn.setOnAction(e -> handleEditProduit(produit));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #f78f34; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 12px; -fx-cursor: hand;");
        deleteBtn.setPadding(new Insets(6, 15, 6, 15));
        deleteBtn.setOnAction(e -> handleDeleteProduit(produit));

        actions.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(header, details, statusRow, actions);
        return card;
    }

    // ========== MÉTHODES CRUD ==========

    @FXML
    private void handleAjouterService() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/AjoutServiceDialog.fxml"));
            DialogPane dialogPane = loader.load();

            AjoutServiceController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Ajouter un service");
            dialog.setDialogPane(dialogPane);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Service newService = controller.getService();
                if (newService != null) {
                    try {
                        int newId = generateNewServiceId();
                        newService.setIdService(newId);
                        servicePersonne.ajouterService(newService);
                        loadData();
                        showAlert("Succès", "Service ajouté avec succès!");
                    } catch (SQLException e) {
                        showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    showAlert("Erreur", "Formulaire invalide!");
                }
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouterProduit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/AjoutProduitDialog.fxml"));
            DialogPane dialogPane = loader.load();

            AjoutProduitController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Ajouter un produit");
            dialog.setDialogPane(dialogPane);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Produit newProduit = controller.getProduit();
                if (newProduit != null) {
                    try {
                        int newId = generateNewProduitId();
                        newProduit.setIdProduit(newId);
                        servicePersonne.ajouterProduit(newProduit);
                        loadData();
                        showAlert("Succès", "Produit ajouté avec succès!");
                    } catch (SQLException e) {
                        showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    showAlert("Erreur", "Formulaire invalide!");
                }
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openAjoutService() {
        handleAjouterService();
    }

    @FXML
    private void openAjoutProduit() {
        handleAjouterProduit();
    }

    private void handleEditService(Service service) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/ModifierServiceDialog.fxml"));
            DialogPane dialogPane = loader.load();

            AjoutServiceController controller = loader.getController();
            controller.setService(service);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifier le service");
            dialog.setDialogPane(dialogPane);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Service updatedService = controller.getService();
                if (updatedService != null) {
                    updatedService.setIdService(service.getIdService());
                    try {
                        servicePersonne.modifierService(updatedService);
                        loadData();
                        showAlert("Succès", "Service modifié avec succès!");
                    } catch (SQLException e) {
                        showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteService(Service service) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le service");
        confirm.setContentText("Voulez-vous vraiment supprimer " + service.getNomService() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    servicePersonne.supprimerService(service.getIdService());
                    loadData();
                    showAlert("Succès", "Service supprimé avec succès!");
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    private void handleEditProduit(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/ModifierProduitDialog.fxml"));
            DialogPane dialogPane = loader.load();

            AjoutProduitController controller = loader.getController();
            controller.setProduit(produit);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifier le produit");
            dialog.setDialogPane(dialogPane);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Produit updatedProduit = controller.getProduit();
                if (updatedProduit != null) {
                    updatedProduit.setIdProduit(produit.getIdProduit());
                    updatedProduit.setDateCreation(produit.getDateCreation());
                    try {
                        servicePersonne.modifierProduit(updatedProduit);
                        loadData();
                        showAlert("Succès", "Produit modifié avec succès!");
                    } catch (SQLException e) {
                        showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteProduit(Produit produit) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le produit");
        confirm.setContentText("Voulez-vous vraiment supprimer " + produit.getNomProduit() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    servicePersonne.supprimerProduit(produit.getIdProduit());
                    loadData();
                    showAlert("Succès", "Produit supprimé avec succès!");
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    // ========== MÉTHODES DE DÉTAILS ==========

    private void openServiceDetails(Service service) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/ServiceDetailsView.fxml"));
            Parent root = loader.load();

            ServiceDetailsController controller = loader.getController();
            controller.setService(service);

            Stage stage = new Stage();
            stage.setTitle("Détails du service - " + service.getNomService());
            stage.setScene(new Scene(root, 600, 700));
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir les détails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== MÉTHODES DE GÉNÉRATION D'ID ==========

    private int generateNewServiceId() throws SQLException {
        List<Service> services = servicePersonne.afficherTousServices();
        return services.stream()
                .mapToInt(Service::getIdService)
                .max()
                .orElse(0) + 1;
    }

    private int generateNewProduitId() throws SQLException {
        List<Produit> produits = servicePersonne.afficherTousProduits();
        return produits.stream()
                .mapToInt(Produit::getIdProduit)
                .max()
                .orElse(0) + 1;
    }

    // ========== MÉTHODES DE NAVIGATION ==========

    @FXML
    private void handleNavigation(MouseEvent event) {
        HBox clicked = (HBox) event.getSource();

        resetNavStyles();
        clicked.setStyle("-fx-background-color: #d2e7fe; -fx-background-radius: 10; -fx-padding: 12 15;");

        if (clicked == navDashboard) {
            openDashboard();
        } else if (clicked == navServices) {
            System.out.println("Navigation vers Services");
        } else if (clicked == navComptes) {
            openComptes();
        } else if (clicked == navDepenses) {
            openDepenses();
        } else if (clicked == navDocuments) {
            openDocuments();
        } else if (clicked == navPaiements) {
            openPaiements();
        }
    }

    private void resetNavStyles() {
        if (navDashboard != null)
            navDashboard.setStyle("-fx-background-radius: 10; -fx-padding: 12 15;");
        if (navServices != null)
            navServices.setStyle("-fx-background-radius: 10; -fx-padding: 12 15;");
        if (navComptes != null)
            navComptes.setStyle("-fx-background-radius: 10; -fx-padding: 12 15;");
        if (navDepenses != null)
            navDepenses.setStyle("-fx-background-radius: 10; -fx-padding: 12 15;");
        if (navDocuments != null)
            navDocuments.setStyle("-fx-background-radius: 10; -fx-padding: 12 15;");
        if (navPaiements != null)
            navPaiements.setStyle("-fx-background-radius: 10; -fx-padding: 12 15;");
    }

    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/DashboardView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Dashboard Administrateur - FinTrack");
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openComptes() {
        showAlert("Info", "Module Comptes en cours de développement");
    }

    private void openDepenses() {
        showAlert("Info", "Module Dépenses en cours de développement");
    }

    private void openDocuments() {
        showAlert("Info", "Module Documents en cours de développement");
    }

    private void openPaiements() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/PaiementView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion des Paiements - FinTrack");
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir la vue des paiements: " + e.getMessage());
        }
    }

    // ========== MÉTHODE D'ALERTE ==========

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}