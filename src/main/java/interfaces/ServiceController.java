package interfaces;

import services.ServicePersonne;
import models.Service;
import models.Produit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.geometry.Pos;
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
        }
    }

    private void updateStats() {
        try {
            List<Service> allServices = servicePersonne.afficherTousServices();
            List<Produit> allProduits = servicePersonne.afficherTousProduits();

            long servCount = allServices.size();
            long prodCount = allProduits.size();

            if (totalServices != null) totalServices.setText(String.valueOf(servCount));
            if (totalProduits != null) totalProduits.setText(String.valueOf(prodCount));

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
        servicesFlow.getChildren().clear();

        for (Service service : services) {
            VBox card = createServiceCard(service);
            servicesFlow.getChildren().add(card);
        }
    }

    private void displayProduits(List<Produit> produits) {
        produitsFlow.getChildren().clear();

        for (Produit produit : produits) {
            VBox card = createProduitCard(produit);
            produitsFlow.getChildren().add(card);
        }
    }

    private VBox createServiceCard(Service service) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15;");
        card.setPadding(new Insets(15));
        card.setPrefWidth(300);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setOffsetY(2);
        shadow.setColor(Color.rgb(24, 45, 136, 0.1));
        card.setEffect(shadow);

        // En-tête
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle icon = new Circle(8);
        icon.setFill(service.getTypeService() == Service.TypeService.abonnement ?
                Color.web("#8dbc71") : Color.web("#f78f34"));

        Label title = new Label(service.getNomService());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #182d88;");

        header.getChildren().addAll(icon, title);

        // Détails sous forme de texte
        VBox details = new VBox(5);
        details.setPadding(new Insets(5, 0, 5, 15));

        Label typeLabel = new Label("Type: " + service.getTypeService());
        typeLabel.setStyle("-fx-text-fill: #182d88;");

        Label tarifLabel = new Label("Tarif: " + service.getTarif() + " TND");
        tarifLabel.setStyle("-fx-text-fill: #182d88; -fx-font-weight: bold;");

        details.getChildren().addAll(typeLabel, tarifLabel);

        if (service.getTypeService() == Service.TypeService.abonnement) {
            if (service.getFrequence() != null) {
                Label freqLabel = new Label("Fréquence: " + service.getFrequence());
                freqLabel.setStyle("-fx-text-fill: #182d88; -fx-opacity: 0.8;");
                details.getChildren().add(freqLabel);
            }
            if (service.getDateDebut() != null) {
                Label dateLabel = new Label("Début: " + service.getDateDebut().format(dateFormatter));
                dateLabel.setStyle("-fx-text-fill: #182d88; -fx-opacity: 0.8;");
                details.getChildren().add(dateLabel);
            }
        }

        // Statut
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Circle statusDot = new Circle(5);
        Label statusLabel = new Label(service.getStatut().toString());

        switch (service.getStatut()) {
            case actif:
                statusDot.setFill(Color.web("#8dbc71"));
                statusLabel.setStyle("-fx-text-fill: #8dbc71; -fx-font-weight: bold;");
                break;
            case suspendu:
                statusDot.setFill(Color.web("#fecf47"));
                statusLabel.setStyle("-fx-text-fill: #fecf47; -fx-font-weight: bold;");
                break;
            case expire:
                statusDot.setFill(Color.web("#f78f34"));
                statusLabel.setStyle("-fx-text-fill: #f78f34; -fx-font-weight: bold;");
                break;
        }

        statusBox.getChildren().addAll(statusDot, statusLabel);

        // Boutons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #182d88; -fx-text-fill: white; -fx-background-radius: 5;");
        editBtn.setOnAction(e -> openModifierServiceDialog(service));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #f78f34; -fx-text-fill: white; -fx-background-radius: 5;");
        deleteBtn.setOnAction(e -> handleDeleteService(service));

        buttons.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(header, details, statusBox, buttons);
        return card;
    }

    private VBox createProduitCard(Produit produit) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15;");
        card.setPadding(new Insets(15));
        card.setPrefWidth(300);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setColor(Color.rgb(24, 45, 136, 0.1));
        card.setEffect(shadow);

        // En-tête
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle icon = new Circle(8);
        icon.setFill(Color.web("#8dbc71"));

        Label title = new Label(produit.getNomProduit());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #182d88;");

        header.getChildren().addAll(icon, title);

        // Détails sous forme de texte
        VBox details = new VBox(5);
        details.setPadding(new Insets(5, 0, 5, 15));

        Label typeLabel = new Label("Type: " + produit.getTypeProduit());
        typeLabel.setStyle("-fx-text-fill: #182d88;");

        Label montantLabel = new Label("Montant: " + produit.getMontant() + " TND");
        montantLabel.setStyle("-fx-text-fill: #182d88; -fx-font-weight: bold;");

        Label codeLabel = new Label("Code: " + produit.getCodeUnique());
        codeLabel.setStyle("-fx-text-fill: #182d88; -fx-opacity: 0.8;");

        details.getChildren().addAll(typeLabel, montantLabel, codeLabel);

        // Statut
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Circle statusDot = new Circle(5);
        Label statusLabel = new Label(produit.getStatut().toString());

        switch (produit.getStatut()) {
            case disponible:
                statusDot.setFill(Color.web("#8dbc71"));
                statusLabel.setStyle("-fx-text-fill: #8dbc71; -fx-font-weight: bold;");
                break;
            case vendu:
                statusDot.setFill(Color.web("#f78f34"));
                statusLabel.setStyle("-fx-text-fill: #f78f34; -fx-font-weight: bold;");
                break;
            case expire:
                statusDot.setFill(Color.web("#fecf47"));
                statusLabel.setStyle("-fx-text-fill: #fecf47; -fx-font-weight: bold;");
                break;
        }

        statusBox.getChildren().addAll(statusDot, statusLabel);

        // Boutons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #182d88; -fx-text-fill: white; -fx-background-radius: 5;");
        editBtn.setOnAction(e -> openModifierProduitDialog(produit));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #f78f34; -fx-text-fill: white; -fx-background-radius: 5;");
        deleteBtn.setOnAction(e -> handleDeleteProduit(produit));

        buttons.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(header, details, statusBox, buttons);
        return card;
    }

    @FXML
    private void openAjoutServiceDialog() {
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
                        newService.setIdService(generateNewServiceId());
                        servicePersonne.ajouterService(newService);
                        loadData();
                        showAlert("Succès", "Service ajouté avec succès!");
                    } catch (SQLException e) {
                        showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
                    }
                } else {
                    showAlert("Erreur", "Données invalides!");
                }
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void openAjoutProduitDialog() {
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
                        newProduit.setIdProduit(generateNewProduitId());
                        newProduit.setDateCreation(LocalDate.now());
                        servicePersonne.ajouterProduit(newProduit);
                        loadData();
                        showAlert("Succès", "Produit ajouté avec succès!");
                    } catch (SQLException e) {
                        showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
                    }
                } else {
                    showAlert("Erreur", "Données invalides!");
                }
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void openModifierServiceDialog(Service service) {
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
        }
    }

    private void openModifierProduitDialog(Produit produit) {
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}