package Controllers;

import Models.Document;
import Services.ServiceCategorie;
import Services.ServiceDocument;
import Services.ServiceDossier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML private Label lblTotalDocuments;
    @FXML private Label lblTotalDossiers;
    @FXML private Label lblTotalCategories;
    @FXML private Label lblDocumentsThisMonth;

    @FXML private Label lblMontantTotal;
    @FXML private Label lblMontantAvg;
    @FXML private Label lblMontantMax;
    @FXML private Label lblMontantMin;

    @FXML private PieChart pieChartCategories;
    @FXML private BarChart<String, Number> barChartDossiers;

    @FXML private PieChart pieChartMontantCategories;
    @FXML private BarChart<String, Number> barChartMontantDossiers;

    @FXML private ListView<Document> lvRecentDocuments;
    @FXML private VBox containerTopDocuments;

    private final ServiceDocument documentService = new ServiceDocument();
    private final ServiceDossier dossierService = new ServiceDossier();
    private final ServiceCategorie categorieService = new ServiceCategorie();

    @FXML
    public void initialize() {
        loadStatistics();
        loadCategoriesChart();
        loadDossiersChart();
        loadRecentDocuments();
        loadMontantStats();
        loadMontantByCategorieChart();
        loadMontantByDossierChart();
        loadTopDocuments();
    }

    /**
     * Crée une card personnalisée pour un document récent
     */
    private VBox createRecentDocumentCard(Document doc) {
        VBox card = new VBox(5);
        card.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

        Label lblTitle = new Label(doc.getTitre());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #182d88;");

        Label lblDesc = new Label(doc.getDescription() != null && !doc.getDescription().isEmpty()
                ? doc.getDescription() : "Pas de description");
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

    /**
     * Crée une card personnalisée pour un document top (par montant)
     */
    private VBox createTopDocumentCard(Document doc) {
        VBox card = new VBox(8);
        card.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblTitle = new Label(doc.getTitre());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #182d88;");

        Label lblMontant = new Label(String.format("%.2f €", doc.getMontant()));
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
            AlertUtils.showError("Erreur Base de Données", "Impossible de charger les stats des catégories: " + e.getMessage());
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
            AlertUtils.showError("Erreur Base de Données", "Impossible de charger les stats des dossiers: " + e.getMessage());
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
            AlertUtils.showError("Erreur Base de Données", "Impossible de charger les documents récents: " + e.getMessage());
        }
    }

    private void loadMontantStats() {
        try {
            lblMontantTotal.setText(String.format("%.2f €", documentService.getMontantTotal()));
            lblMontantAvg.setText(String.format("%.2f €", documentService.getMontantAverage()));
            lblMontantMax.setText(String.format("%.2f €", documentService.getMontantMax()));
            lblMontantMin.setText(String.format("%.2f €", documentService.getMontantMin()));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur Base de Données", "Impossible de charger l'analyse montant: " + e.getMessage());
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
            AlertUtils.showError("Erreur Base de Données", "Impossible de charger le montant par catégorie: " + e.getMessage());
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
            AlertUtils.showError("Erreur Base de Données", "Impossible de charger le montant par dossier: " + e.getMessage());
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
        AlertUtils.showSuccess("Succès", "Dashboard actualisé !");
    }
}

