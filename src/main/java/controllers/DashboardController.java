package controllers;

import models.*;
import services.ServiceDashboard;
import services.ServiceService;
import services.ServiceProduit;
import services.ServicePaiement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private VBox root;

    // Cartes de statistiques
    @FXML private Text totalServices;
    @FXML private Text totalProduits;
    @FXML private Text totalPaiements;
    @FXML private Text chiffreAffaires;
    @FXML private Text caMois;

    // Graphiques
    @FXML private PieChart servicesPieChart;
    @FXML private PieChart produitsPieChart;
    @FXML private BarChart<String, Number> revenusBarChart;
    @FXML private CategoryAxis moisAxis;
    @FXML private NumberAxis montantAxis;

    // Alertes
    @FXML private VBox alertesContainer;

    // Tables récentes
    @FXML private TableView<Service> servicesTable;
    @FXML private TableColumn<Service, Integer> serviceIdCol;
    @FXML private TableColumn<Service, String> serviceNomCol;
    @FXML private TableColumn<Service, String> serviceTypeCol;
    @FXML private TableColumn<Service, BigDecimal> serviceTarifCol;
    @FXML private TableColumn<Service, String> serviceStatutCol;

    @FXML private TableView<Produit> produitsTable;
    @FXML private TableColumn<Produit, Integer> produitIdCol;
    @FXML private TableColumn<Produit, String> produitNomCol;
    @FXML private TableColumn<Produit, String> produitTypeCol;
    @FXML private TableColumn<Produit, BigDecimal> produitMontantCol;
    @FXML private TableColumn<Produit, String> produitStatutCol;

    @FXML private TableView<Paiement> paiementsTable;
    @FXML private TableColumn<Paiement, Integer> paiementIdCol;
    @FXML private TableColumn<Paiement, BigDecimal> paiementMontantCol;
    @FXML private TableColumn<Paiement, String> paiementDateCol;
    @FXML private TableColumn<Paiement, String> paiementModeCol;
    @FXML private TableColumn<Paiement, String> paiementStatutCol;

    private ServiceDashboard dashboardService;
    private ServiceService serviceService;
    private ServiceProduit produitService;
    private ServicePaiement paiementService;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dashboardService = new ServiceDashboard();
        serviceService = new ServiceService();
        produitService = new ServiceProduit();
        paiementService = new ServicePaiement();

        setupTables();
        loadData();
    }

    private void setupTables() {
        // Configuration des colonnes pour les services
        serviceIdCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getIdService()).asObject());
        serviceNomCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNomService()));
        serviceTypeCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTypeService().name()));
        serviceTarifCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTarif()));
        serviceStatutCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatut().name()));

        // Configuration des colonnes pour les produits
        produitIdCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getIdProduit()).asObject());
        produitNomCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNomProduit()));
        produitTypeCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTypeProduit().name()));
        produitMontantCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getMontant()));
        produitStatutCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatut().name()));

        // Configuration des colonnes pour les paiements
        paiementIdCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getIdPaiement()).asObject());
        paiementMontantCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getMontant()));
        paiementDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDatePaiement().format(dateFormatter)));
        paiementModeCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getModePaiement().name()));
        paiementStatutCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getStatut().name()));

        // Double-clic pour ouvrir les détails
        servicesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && servicesTable.getSelectionModel().getSelectedItem() != null) {
                openServiceDetails(servicesTable.getSelectionModel().getSelectedItem());
            }
        });

        produitsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && produitsTable.getSelectionModel().getSelectedItem() != null) {
                openProduitDetails(produitsTable.getSelectionModel().getSelectedItem());
            }
        });

        paiementsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && paiementsTable.getSelectionModel().getSelectedItem() != null) {
                openPaiementDetails(paiementsTable.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void loadData() {
        try {
            DashboardStats stats = dashboardService.getStats();

            // Mettre à jour les cartes de statistiques
            totalServices.setText(String.valueOf(stats.getTotalServices()));
            totalProduits.setText(String.valueOf(stats.getTotalProduits()));
            totalPaiements.setText(String.valueOf(stats.getTotalPaiements()));
            chiffreAffaires.setText(stats.getChiffreAffaires() + " TND");
            caMois.setText(stats.getChiffreAffairesMois() + " TND");

            // Mettre à jour les graphiques
            updateServicesChart(stats);
            updateProduitsChart(stats);
            updateRevenusChart();

            // Mettre à jour les alertes
            updateAlertes(stats);

            // Charger les tables
            loadServicesTable();
            loadProduitsTable();
            loadPaiementsTable();

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données: " + e.getMessage());
        }
    }

    private void updateServicesChart(DashboardStats stats) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        stats.getServicesParType().forEach((type, count) ->
                pieData.add(new PieChart.Data(type + " (" + count + ")", count)));
        servicesPieChart.setData(pieData);
        servicesPieChart.setTitle("Répartition des services");
    }

    private void updateProduitsChart(DashboardStats stats) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        stats.getProduitsParType().forEach((type, count) ->
                pieData.add(new PieChart.Data(type + " (" + count + ")", count)));
        produitsPieChart.setData(pieData);
        produitsPieChart.setTitle("Répartition des produits");
    }

    private void updateRevenusChart() throws SQLException {
        int annee = LocalDate.now().getYear();
        revenusBarChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenus " + annee);

        dashboardService.getRevenusParMois(annee).forEach((mois, montant) ->
                series.getData().add(new XYChart.Data<>(mois.substring(0, 3), montant)));

        revenusBarChart.getData().add(series);
    }

    private void updateAlertes(DashboardStats stats) {
        alertesContainer.getChildren().clear();

        if (stats.getServicesExpires() > 0) {
            alertesContainer.getChildren().add(createAlerte(
                    "⚠️ " + stats.getServicesExpires() + " service(s) expiré(s)",
                    "warning"
            ));
        }

        if (stats.getServicesSuspendus() > 0) {
            alertesContainer.getChildren().add(createAlerte(
                    "⏸️ " + stats.getServicesSuspendus() + " service(s) suspendu(s)",
                    "info"
            ));
        }

        if (stats.getProduitsVendus() > 0) {
            alertesContainer.getChildren().add(createAlerte(
                    "💰 " + stats.getProduitsVendus() + " produit(s) vendu(s)",
                    "success"
            ));
        }

        if (stats.getProduitsExpires() > 0) {
            alertesContainer.getChildren().add(createAlerte(
                    "⌛ " + stats.getProduitsExpires() + " produit(s) expiré(s)",
                    "danger"
            ));
        }

        if (alertesContainer.getChildren().isEmpty()) {
            Label noAlert = new Label("✅ Aucune alerte");
            noAlert.setStyle("-fx-text-fill: #8dbc71; -fx-padding: 10;");
            alertesContainer.getChildren().add(noAlert);
        }
    }

    private HBox createAlerte(String message, String type) {
        HBox alerte = new HBox(10);
        alerte.setPadding(new Insets(10));
        alerte.setStyle("-fx-background-color: " + getColorForType(type) + "; -fx-background-radius: 5;");

        Label label = new Label(message);
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        alerte.getChildren().add(label);

        return alerte;
    }

    private String getColorForType(String type) {
        switch (type) {
            case "warning": return "#f78f34";
            case "danger": return "#f56565";
            case "success": return "#8dbc71";
            case "info": return "#667eea";
            default: return "#182d88";
        }
    }

    private void loadServicesTable() throws SQLException {
        ObservableList<Service> services = FXCollections.observableArrayList(
                serviceService.recuperer()
        );
        servicesTable.setItems(services);
    }

    private void loadProduitsTable() throws SQLException {
        ObservableList<Produit> produits = FXCollections.observableArrayList(
                produitService.recuperer()
        );
        produitsTable.setItems(produits);
    }

    private void loadPaiementsTable() throws SQLException {
        ObservableList<Paiement> paiements = FXCollections.observableArrayList(
                paiementService.recupererTous()
        );
        paiementsTable.setItems(paiements);
    }

    private void openServiceDetails(Service service) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/ServiceDetailsView.fxml"));
            Parent root = loader.load();

            ServiceDetailsController controller = loader.getController();
            controller.setService(service);

            Stage stage = new Stage();
            stage.setTitle("Détails du service");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openProduitDetails(Produit produit) {
        // Implémenter selon vos besoins
    }

    private void openPaiementDetails(Paiement paiement) {
        // Implémenter selon vos besoins
    }

    @FXML
    private void refreshDashboard() {
        loadData();
    }

    @FXML
    private void exportStats() {
        // Implémenter l'export PDF/Excel
        showAlert("Info", "Fonctionnalité d'export à venir");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}