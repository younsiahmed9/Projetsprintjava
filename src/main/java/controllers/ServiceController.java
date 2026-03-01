package controllers;

import models.Produit;
import models.Service;
import services.ServiceService;
import services.ServiceProduit;
import services.PromotionService;
import api.SmsClient;
import api.APIPromos;
import config.APIConfig.ApiConfig;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceController implements Initializable {

    @FXML private Text totalServices;
    @FXML private Text totalProduits;
    @FXML private FlowPane servicesFlow;
    @FXML private FlowPane produitsFlow;
    @FXML private TextField searchServiceField;
    @FXML private ComboBox<String> filterStatutService;
    @SuppressWarnings("unused") @FXML private Button addServiceButton;
    @SuppressWarnings("unused") @FXML private Button addProductButton;
    @FXML private Button promoButton;

    // Statistiques détaillées
    @FXML private Label statTotalServices;
    @FXML private Label statServicesActifs;
    @FXML private Label statServicesSuspendus;
    @FXML private Label statServicesExpires;
    @FXML private Label statTotalProduits;
    @FXML private Label statProduitsDisponibles;
    @FXML private Label statProduitsEpuses;
    @FXML private Label statMontantTotal;

    private ServiceService serviceService;
    private ServiceProduit produitService;
    private PromotionService promotionService;
    private SmsClient smsClient;

    private final ObservableList<Service> serviceList = FXCollections.observableArrayList();
    private final ObservableList<Produit> produitList = FXCollections.observableArrayList();
    private FilteredList<Service> filteredServices;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Logger LOGGER = Logger.getLogger(ServiceController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            serviceService = new ServiceService();
            produitService = new ServiceProduit();
            promotionService = new PromotionService();
            smsClient = new SmsClient();

            // ✅ Ces méthodes doivent exister - je les ajoute plus bas
            initialiserFiltres();
            chargerDonnees();
            configurerRecherche();

            System.out.println("\n📋 État du système de promotion:");
            ApiConfig.afficherConfiguration();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation", e);
            afficherErreurCritique("Erreur de connexion",
                    "Impossible de se connecter à la base de données.",
                    "Vérifiez que le serveur est démarré.");

            if (searchServiceField != null) searchServiceField.setDisable(true);
            if (filterStatutService != null) filterStatutService.setDisable(true);
            if (servicesFlow != null) servicesFlow.setDisable(true);
            if (produitsFlow != null) produitsFlow.setDisable(true);
            if (addServiceButton != null) addServiceButton.setDisable(true);
            if (addProductButton != null) addProductButton.setDisable(true);
        }
    }

    // ==================== MÉTHODES MANQUANTES ====================

    /**
     * Initialise les filtres
     */
    private void initialiserFiltres() {
        if (filterStatutService != null) {
            filterStatutService.getItems().addAll("Tous", "actif", "suspendu", "expire");
            filterStatutService.setValue("Tous");
            filterStatutService.setOnAction(e -> filtrerServices());
        }
    }

    /**
     * Configure la recherche
     */
    private void configurerRecherche() {
        if (searchServiceField != null) {
            searchServiceField.textProperty().addListener((obs, old, n) -> filtrerServices());
        }
    }

    /**
     * Filtre les services
     */
    private void filtrerServices() {
        if (filteredServices == null) {
            filteredServices = new FilteredList<>(serviceList, p -> true);
        }
        String searchText = searchServiceField != null ? searchServiceField.getText().toLowerCase() : "";
        String statutFilter = filterStatutService != null ? filterStatutService.getValue() : "Tous";

        filteredServices.setPredicate(service -> {
            boolean searchMatch = searchText.isEmpty() || service.getNomService().toLowerCase().contains(searchText);
            boolean statutMatch = "Tous".equals(statutFilter) || statutFilter.equals(service.getStatut());
            return searchMatch && statutMatch;
        });

        if (servicesFlow != null) {
            servicesFlow.getChildren().clear();
            for (Service service : filteredServices) {
                servicesFlow.getChildren().add(createServiceCard(service));
            }
        }
    }

    /**
     * Charge les données
     */
    public void chargerDonnees() {
        try {
            serviceList.setAll(serviceService.recupererTous());
            produitList.setAll(produitService.recupererTous());
            afficherServices();
            afficherProduits();
            mettreAJourStatistiques();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des données", e);
        }
    }

    /**
     * Affiche les services
     */
    private void afficherServices() {
        if (servicesFlow != null) {
            servicesFlow.getChildren().clear();
            for (Service service : serviceList) {
                servicesFlow.getChildren().add(createServiceCard(service));
            }
        }
    }

    /**
     * Affiche les produits
     */
    private void afficherProduits() {
        if (produitsFlow != null) {
            produitsFlow.getChildren().clear();
            for (Produit produit : produitList) {
                produitsFlow.getChildren().add(createProduitCard(produit));
            }
        }
    }

    /**
     * Crée une carte service
     */
    private VBox createServiceCard(Service service) {
        VBox card = createBaseCard();
        Label nomLabel = new Label(service.getNomService());
        nomLabel.getStyleClass().add("header-text");
        Label typeLabel = new Label("Type: " + service.getTypeService());
        Label tarifLabel = new Label(String.format("%.2f DT", service.getTarif()));
        tarifLabel.getStyleClass().add("header-text");

        long jours = 0;
        if (service.getDateDebut() != null && service.getDateFin() != null) {
            jours = ChronoUnit.DAYS.between(service.getDateDebut(), service.getDateFin());
        }
        Label dureeLabel = new Label("Durée: " + jours + " jours");
        Label statutLabel = createStatutLabel(service.getStatut());

        HBox actions = createActionBox(
                e -> afficherDetailsService(service),
                e -> showServiceEditDialog(service),
                e -> supprimerService(service)
        );

        card.getChildren().addAll(nomLabel, typeLabel, tarifLabel, dureeLabel, statutLabel, actions);
        return card;
    }

    /**
     * Crée une carte produit
     */
    private VBox createProduitCard(Produit produit) {
        VBox card = createBaseCard();
        Label nomLabel = new Label(produit.getNomProduit());
        nomLabel.getStyleClass().add("header-text");
        Label typeLabel = new Label("Type: " + produit.getTypeProduit());
        Label montantLabel = new Label(String.format("%.2f DT", produit.getMontant()));
        montantLabel.getStyleClass().add("header-text");
        Label codeLabel = new Label("Code: " + produit.getCodeUnique());
        Label statutLabel = createStatutLabel(produit.getStatut());

        HBox actions = createActionBox(
                e -> afficherDetailsProduit(produit),
                e -> showProduitEditDialog(produit),
                e -> supprimerProduit(produit)
        );

        card.getChildren().addAll(nomLabel, typeLabel, montantLabel, codeLabel, statutLabel, actions);
        return card;
    }

    /**
     * Crée une carte de base
     */
    private VBox createBaseCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(250);
        return card;
    }

    /**
     * Crée une boîte d'actions
     */
    private HBox createActionBox(EventHandler<ActionEvent> detailsAction,
                                 EventHandler<ActionEvent> modifierAction,
                                 EventHandler<ActionEvent> supprimerAction) {
        HBox actions = new HBox(10);
        Button btnDetails = createStyledButton("👁️", "button-primary", detailsAction);
        Button btnModifier = createStyledButton("✏️", "button-success", modifierAction);
        Button btnSupprimer = createStyledButton("🗑️", "button-danger", supprimerAction);
        actions.getChildren().addAll(btnDetails, btnModifier, btnSupprimer);
        return actions;
    }

    /**
     * Crée un bouton stylisé
     */
    private Button createStyledButton(String text, String styleClass, EventHandler<ActionEvent> action) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setOnAction(action);
        return button;
    }

    /**
     * Crée un label de statut
     */
    private Label createStatutLabel(String statut) {
        Label statutLabel = new Label(statut);
        String statutColor = switch (statut) {
            case "actif", "disponible" -> "success-label";
            case "suspendu" -> "warning-label";
            default -> "error-label";
        };
        statutLabel.getStyleClass().add(statutColor);
        return statutLabel;
    }

    /**
     * Met à jour les statistiques
     */
    public void mettreAJourStatistiques() {
        if (totalServices != null) totalServices.setText(String.valueOf(serviceList.size()));
        if (totalProduits != null) totalProduits.setText(String.valueOf(produitList.size()));
        if (statTotalServices != null) statTotalServices.setText(String.valueOf(serviceList.size()));
        if (statTotalProduits != null) statTotalProduits.setText(String.valueOf(produitList.size()));

        long servicesActifs = serviceList.stream().filter(s -> "actif".equals(s.getStatut())).count();
        long servicesSuspendus = serviceList.stream().filter(s -> "suspendu".equals(s.getStatut())).count();
        long servicesExpires = serviceList.stream().filter(s -> "expire".equals(s.getStatut())).count();

        if (statServicesActifs != null) statServicesActifs.setText(String.valueOf(servicesActifs));
        if (statServicesSuspendus != null) statServicesSuspendus.setText(String.valueOf(servicesSuspendus));
        if (statServicesExpires != null) statServicesExpires.setText(String.valueOf(servicesExpires));

        long produitsDisponibles = produitList.stream().filter(p -> "disponible".equals(p.getStatut())).count();
        long produitsEpuses = produitList.stream().filter(p -> "epuise".equals(p.getStatut())).count();

        if (statProduitsDisponibles != null) statProduitsDisponibles.setText(String.valueOf(produitsDisponibles));
        if (statProduitsEpuses != null) statProduitsEpuses.setText(String.valueOf(produitsEpuses));

        double montantTotal = produitList.stream()
                .mapToDouble(p -> p.getMontant() != null ? p.getMontant().doubleValue() : 0)
                .sum();
        if (statMontantTotal != null) statMontantTotal.setText(String.format("%.2f DT", montantTotal));
    }

    /**
     * Affiche une erreur critique
     */
    private void afficherErreurCritique(String titre, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(header);
        alert.setContentText(content);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");
        alert.showAndWait();
    }

    // ==================== MÉTHODES EXISTANTES ====================

    @FXML
    private void ajouterService(ActionEvent event) {
        showServiceEditDialog(null);
    }

    private void showServiceEditDialog(Service service) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/AjoutServiceDialog.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            AjoutServiceController controller = loader.getController();
            controller.setDialogStage(stage);
            controller.setService(service);
            stage.showAndWait();
            if (controller.isOkClicked()) {
                chargerDonnees();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur", e);
        }
    }

    @FXML
    private void ajouterProduit(ActionEvent event) {
        showProduitEditDialog(null);
    }

    private void showProduitEditDialog(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/AjoutProduitDialog.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            AjoutProduitController controller = loader.getController();
            controller.setDialogStage(stage);
            controller.setProduit(produit);
            stage.showAndWait();
            if (controller.isOkClicked()) {
                chargerDonnees();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur", e);
        }
    }

    private void supprimerService(Service service) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer " + service.getNomService() + "?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceService.supprimer(service.getIdService());
                chargerDonnees();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur", e);
            }
        }
    }

    private void afficherDetailsService(Service service) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/ServiceDetailsView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            ServiceDetailsController controller = loader.getController();
            controller.setDialogStage(stage);
            controller.setService(service);
            stage.showAndWait();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur", e);
        }
    }

    private void supprimerProduit(Produit produit) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer " + produit.getNomProduit() + "?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                produitService.supprimer(produit.getIdProduit());
                chargerDonnees();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur", e);
            }
        }
    }

    private void afficherDetailsProduit(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/ProduitDetailsView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            ProduitDetailsController controller = loader.getController();
            controller.setDialogStage(stage);
            controller.setProduit(produit);
            stage.showAndWait();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur", e);
        }
    }

    @FXML
    private void handleFactures() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/FactureView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Gestion des factures");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre des factures:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/DashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) searchServiceField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Tableau de bord");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger DashboardView.fxml: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handlePromotion() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("🎯 Gestion des promotions");
            alert.setHeaderText("Que voulez-vous faire ?");
            alert.setContentText("Choisissez une option:");

            ButtonType btnCartesExpirees = new ButtonType("📦 Cartes expirées", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnCreerOffres = new ButtonType("🎁 Créer offres", ButtonBar.ButtonData.OTHER);
            ButtonType btnNotifier = new ButtonType("📱 Notifier clients", ButtonBar.ButtonData.OTHER);
            ButtonType btnAnnuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(btnCartesExpirees, btnCreerOffres, btnNotifier, btnAnnuler);
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == btnCartesExpirees) {
                    afficherCartesExpirees();
                } else if (result.get() == btnCreerOffres) {
                    creerOffresPromo();
                } else if (result.get() == btnNotifier) {
                    notifierClients();
                }
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void afficherCartesExpirees() {
        try {
            var cartes = promotionService.detecterCartesExpirees();
            if (cartes.isEmpty()) {
                showAlert("Information", "Aucune carte expirée trouvée", Alert.AlertType.INFORMATION);
                return;
            }
            StringBuilder sb = new StringBuilder("📦 Cartes expirées trouvées:\n\n");
            for (var carte : cartes) {
                sb.append("• ").append(carte.getNomProduit())
                        .append(" - ").append(carte.getMontant()).append(" €")
                        .append(" (").append(carte.getStatut()).append(")\n");
            }
            showAlert("Cartes expirées", sb.toString(), Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void creerOffresPromo() {
        try {
            var offres = promotionService.promouvoirCartesExpirees();
            if (offres.isEmpty()) {
                showAlert("Information", "Aucune offre créée", Alert.AlertType.INFORMATION);
                return;
            }
            StringBuilder sb = new StringBuilder("🎁 Offres créées:\n\n");
            for (var offre : offres) {
                sb.append("• ").append(offre.getMessage()).append("\n\n");
            }
            showAlert("Offres créées", sb.toString(), Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void notifierClients() {
        try {
            var offres = promotionService.getPromotionsActives();
            if (offres.isEmpty()) {
                showAlert("Information", "Aucune offre active à notifier", Alert.AlertType.INFORMATION);
                return;
            }
            String[] numeros = ApiConfig.getTestNumbers();
            int total = 0;
            for (var offre : offres) {
                for (String numero : numeros) {
                    boolean sent = smsClient.sendPromotionExpiree(
                            numero.trim(),
                            offre.getNomProduit(),
                            offre.getAncienPrix().doubleValue(),
                            offre.getNouveauPrix().doubleValue(),
                            offre.getPourcentageReduction()
                    );
                    if (sent) total++;
                }
            }
            showAlert("Succès", total + " notification(s) envoyée(s)", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void demarrerAPI() {
        try {
            APIPromos api = new APIPromos();
            api.demarrer(8080);
            showAlert("API démarrée", "API de promotion disponible sur http://localhost:8080", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de démarrer l'API: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void afficherConfig() {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 Configuration:\n");
        sb.append("Fournisseur SMS: ").append(ApiConfig.getSmsProvider()).append("\n");
        sb.append("Mode simulation: ").append(ApiConfig.isSimulationMode()).append("\n");
        sb.append("Réduction: ").append(ApiConfig.getPromotionReductionExpiree()).append("%\n");
        sb.append("Durée: ").append(ApiConfig.getPromotionDureeJours()).append(" jours\n");
        sb.append("Numéros: ").append(String.join(", ", ApiConfig.getTestNumbers()));
        showAlert("Configuration", sb.toString(), Alert.AlertType.INFORMATION);
    }

    private void showAlert(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String erreur, String s) {
        showAlert(erreur, s, Alert.AlertType.ERROR);
    }
    @FXML
    private void handleOuvrirSMS() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/EnvoiSMSDialog.fxml"));            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Envoyer un SMS");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            // ✅ Récupérer le contrôleur si besoin
            EnvoiSMSController controller = loader.getController();
            controller.setDialogStage(stage);

            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre SMS: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    }


