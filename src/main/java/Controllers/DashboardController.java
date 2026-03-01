package Controllers;

import Models.Document;
import Services.ServiceCategorie;
import Services.ServiceDocument;
import Services.ServiceDossier;
import Services.AlertService;
import Models.Echeance;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML
    private Label lblTotalDocuments;
    @FXML
    private Label lblTotalDossiers;
    @FXML
    private Label lblTotalCategories;
    @FXML
    private Label lblDocumentsThisMonth;

    @FXML
    private Label lblMontantTotal;
    @FXML
    private Label lblMontantAvg;
    @FXML
    private Label lblMontantMax;
    @FXML
    private Label lblMontantMin;

    @FXML
    private PieChart pieChartCategories;
    @FXML
    private BarChart<String, Number> barChartDossiers;

    @FXML
    private PieChart pieChartMontantCategories;
    @FXML
    private BarChart<String, Number> barChartMontantDossiers;

    @FXML
    private ListView<Document> lvRecentDocuments;
    @FXML
    private VBox containerTopDocuments;
    @FXML
    private VBox containerAlerts;
    @FXML
    private VBox listAlerts;

    private final ServiceDocument documentService = new ServiceDocument();
    private final ServiceDossier dossierService = new ServiceDossier();
    private final ServiceCategorie categorieService = new ServiceCategorie();
    private AlertService alertService;

    @FXML
    public void initialize() {
        try {
            try {
                alertService = new AlertService();
            } catch (Exception e) {
                System.err.println("Could not init AlertService: " + e.getMessage());
            }

            loadStatistics();
            loadCategoriesChart();
            loadDossiersChart();
            loadRecentDocuments();
            loadMontantStats();
            loadMontantByCategorieChart();
            loadMontantByDossierChart();
            loadTopDocuments();
            loadAlerts();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error during Dashboard initialization: " + e.getMessage());
            // We don't show a blocking alert here to avoid interfering with FXMLLoader
        }
    }

    /**
     * Crée une card personnalisée pour un document récent
     */
    private VBox createRecentDocumentCard(Document doc) {
        VBox card = new VBox(5);
        card.setStyle(
                "-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

        Label lblTitle = new Label(doc.getTitre());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #182d88;");

        Label lblDesc = new Label(doc.getDescription() != null && !doc.getDescription().isEmpty()
                ? doc.getDescription()
                : "Pas de description");
        lblDesc.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");
        lblDesc.setWrapText(true);
        lblDesc.setMaxWidth(500);

        HBox hboxMeta = new HBox(15);
        Label lblDossier = new Label("📁 " + (doc.getDossier() != null ? doc.getDossier().getNom() : "N/A"));
        lblDossier.setStyle("-fx-font-size: 10; -fx-text-fill: #999;");

        Label lblCategorie = new Label("🏷️ " + (doc.getCategorie() != null ? doc.getCategorie().getNom() : "N/A"));
        lblCategorie.setStyle("-fx-font-size: 10; -fx-text-fill: #999;");

        Label lblDate = new Label("📅 " + (doc.getUploadedAt() != null
                ? doc.getUploadedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "Date inconnue"));
        lblDate.setStyle("-fx-font-size: 10; -fx-text-fill: #999;");

        hboxMeta.getChildren().addAll(lblDossier, lblCategorie, lblDate);
        card.getChildren().addAll(lblTitle, lblDesc, hboxMeta);

        return card;
    }

    private VBox createTopDocumentCard(Document doc) {
        VBox card = new VBox(8);
        card.setStyle(
                "-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label(doc.getTitre());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #182d88;");

        Label lblMontant = new Label(String.format("%.2f TND", doc.getMontant()));
        lblMontant.setStyle("-fx-font-size: 12; -fx-text-fill: #111827; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(lblTitle, spacer, lblMontant);
        card.getChildren().add(row);

        return card;
    }

    private void loadStatistics() {
        try {
            int totalDocs = documentService.countDocuments();
            int totalDossiers = dossierService.findAll().size();
            int totalCategories = categorieService.findAll().size();
            int docsThisMonth = documentService.countDocumentsThisMonth();

            lblTotalDocuments.setText(String.valueOf(totalDocs));
            lblTotalDossiers.setText(String.valueOf(totalDossiers));
            lblTotalCategories.setText(String.valueOf(totalCategories));
            lblDocumentsThisMonth.setText(String.valueOf(docsThisMonth));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur Base de Données", "Impossible de charger les statistiques: " + e.getMessage());
        }
    }

    private void loadCategoriesChart() {
        try {
            Map<String, Integer> stats = documentService.getStatsDocsByCategorie();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            stats.forEach((categoryName, count) -> {
                pieData.add(new PieChart.Data(categoryName, count));
            });

            pieChartCategories.setData(pieData);
            pieChartCategories.setTitle("Répartition par Catégorie");
            pieChartCategories.setLegendVisible(true);

            pieChartCategories.getData().forEach(data -> {
                data.nameProperty().set(data.getName() + " (" + (int) data.getPieValue() + ")");
            });

        } catch (SQLException e) {
            AlertUtils.showError("Erreur Base de Données",
                    "Impossible de charger les stats des catégories: " + e.getMessage());
        }
    }

    private void loadDossiersChart() {
        try {
            Map<String, Integer> stats = documentService.getStatsDocsByDossier();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de documents");

            stats.forEach((dossierName, count) -> {
                series.getData().add(new XYChart.Data<>(dossierName, count));
            });

            barChartDossiers.getData().clear();
            barChartDossiers.getData().add(series);
            barChartDossiers.setTitle("Documents par Dossier");
            barChartDossiers.setLegendVisible(false);

        } catch (SQLException e) {
            AlertUtils.showError("Erreur Base de Données",
                    "Impossible de charger les stats des dossiers: " + e.getMessage());
        }
    }

    private void loadRecentDocuments() {
        try {
            List<Document> recentDocs = documentService.getRecentDocuments(5);
            lvRecentDocuments.setItems(FXCollections.observableArrayList(recentDocs));
            lvRecentDocuments.setCellFactory(listView -> new javafx.scene.control.ListCell<Document>() {
                @Override
                protected void updateItem(Document doc, boolean empty) {
                    super.updateItem(doc, empty);
                    if (empty || doc == null) {
                        setGraphic(null);
                    } else {
                        setGraphic(createRecentDocumentCard(doc));
                    }
                }
            });
        } catch (SQLException e) {
            AlertUtils.showError("Erreur Base de Données",
                    "Impossible de charger les documents récents: " + e.getMessage());
        }
    }

    private void loadMontantStats() {
        try {
            lblMontantTotal.setText(String.format("%.2f TND", documentService.getMontantTotal()));
            lblMontantAvg.setText(String.format("%.2f TND", documentService.getMontantAverage()));
            lblMontantMax.setText(String.format("%.2f TND", documentService.getMontantMax()));
            lblMontantMin.setText(String.format("%.2f TND", documentService.getMontantMin()));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur Base de Données",
                    "Impossible de charger l'analyse montant: " + e.getMessage());
        }
    }

    private void loadMontantByCategorieChart() {
        try {
            Map<String, Double> stats = documentService.getMontantByCategorie();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            stats.forEach((name, total) -> pieData.add(new PieChart.Data(name, total)));
            pieChartMontantCategories.setData(pieData);
            pieChartMontantCategories.setTitle("Montant par Catégorie");
            pieChartMontantCategories.setLegendVisible(true);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur Base de Données",
                    "Impossible de charger le montant par catégorie: " + e.getMessage());
        }
    }

    private void loadMontantByDossierChart() {
        try {
            Map<String, Double> stats = documentService.getMontantByDossier();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Montant total");
            stats.forEach((name, total) -> series.getData().add(new XYChart.Data<>(name, total)));

            barChartMontantDossiers.getData().clear();
            barChartMontantDossiers.getData().add(series);
            barChartMontantDossiers.setTitle("Montant par Dossier");
            barChartMontantDossiers.setLegendVisible(false);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur Base de Données",
                    "Impossible de charger le montant par dossier: " + e.getMessage());
        }
    }

    private void loadTopDocuments() {
        try {
            List<Document> topDocs = documentService.getTopDocumentsByMontant(5);
            containerTopDocuments.getChildren().clear();
            for (Document doc : topDocs) {
                containerTopDocuments.getChildren().add(createTopDocumentCard(doc));
            }
        } catch (SQLException e) {
            AlertUtils.showError("Erreur Base de Données", "Impossible de charger le top documents: " + e.getMessage());
        }
    }

    private void loadAlerts() {
        if (alertService == null || containerAlerts == null)
            return;
        try {
            alertService.generateAlerts();
            List<Echeance> activeAlerts = alertService.getActiveAlerts();

            listAlerts.getChildren().clear();
            if (activeAlerts.isEmpty()) {
                containerAlerts.setVisible(false);
                containerAlerts.setManaged(false);
            } else {
                containerAlerts.setVisible(true);
                containerAlerts.setManaged(true);
                for (Echeance alert : activeAlerts) {
                    listAlerts.getChildren().add(createAlertCard(alert));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createAlertCard(Echeance alert) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-padding: 10; -fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #fca5a5; -fx-border-radius: 5; -fx-cursor: hand;");

        Label lblUrgency = new Label(alert.getUrgence());
        lblUrgency.setStyle(
                "-fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 3; -fx-background-color: #fee2e2; -fx-text-fill: #991b1b;");

        VBox info = new VBox(2);
        Label lblTitle = new Label(alert.getDocument().getTitre() + " (" + alert.getTypeEcheance() + ")");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

        Label lblDate = new Label(
                "Échéance: " + alert.getDateEcheance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblDate.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11;");

        info.getChildren().addAll(lblTitle, lblDate);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button btnView = new Button("Voir");
        btnView.setStyle(
                "-fx-background-color: #182d88; -fx-text-fill: white; -fx-background-radius: 15; -fx-font-size: 10;");
        btnView.setOnAction(e -> onViewDocument(alert));

        card.getChildren().addAll(lblUrgency, info, btnView);
        card.setOnMouseClicked(e -> onViewDocument(alert));

        return card;
    }

    private void onViewDocument(Echeance alert) {
        try {
            alertService.markAsSeen(alert.getId());
            // Global navigation
            MainController.navigateToDocumentsWithSelect(alert.getDocument().getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRefresh() {
        loadStatistics();
        loadCategoriesChart();
        loadDossiersChart();
        loadRecentDocuments();
        loadMontantStats();
        loadMontantByCategorieChart();
        loadMontantByDossierChart();
        loadTopDocuments();
        loadAlerts();
        AlertUtils.showSuccess("Succès", "Dashboard actualisé !");
    }

    @FXML
    private void onExportPDF() {
        try {
            Map<String, Integer> catStats = documentService.getStatsDocsByCategorie();
            List<Document> recentDocsList = documentService.getRecentDocuments(5);
            List<String> recentTitles = new java.util.ArrayList<>();
            for (Document d : recentDocsList) {
                recentTitles.add(d.getTitre() + " (" + d.getMontant() + " TND)");
            }

            utils.PdfReportService.generateDashboardReport(
                    lblTotalDocuments.getText(),
                    lblTotalDossiers.getText(),
                    lblTotalCategories.getText(),
                    lblDocumentsThisMonth.getText(),
                    lblMontantTotal.getText(),
                    lblMontantAvg.getText(),
                    catStats,
                    recentTitles);
            AlertUtils.showSuccess("Export Réussi", "Le rapport PDF a été généré avec succès.");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur Export", "Impossible de générer le PDF : " + e.getMessage());
        }
    }
}
