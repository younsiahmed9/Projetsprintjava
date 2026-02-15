package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.scene.layout.Priority;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import Models.Budget;

public class MainPageController {

    @FXML
    private FlowPane budgetList;

    @FXML
    private AnchorPane mainContent;

    @FXML
    private VBox mainPageContent;

    @FXML
    private VBox chartContainer;

    private List<Budget> budgets = new ArrayList<>();
    private Map<Integer, Integer> depenseCount = new HashMap<>();

    // Méthode appelée au démarrage de la page
    @FXML
    public void initialize() {
        refreshBudgets();
    }

    public void refreshBudgets() {
        if (budgetList != null) {
            budgetList.getChildren().clear();
        }
        budgets.clear();
        depenseCount.clear();
        chargerBudgetsDepuisBase();
        afficherGraphique();
    }


    private void chargerDonneesDepenses() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/fintrack", "root", "")) {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT id_budget, COUNT(*) as count FROM depense GROUP BY id_budget");

            while (rs.next()) {
                int idBudget = rs.getInt("id_budget");
                int count = rs.getInt("count");
                depenseCount.put(idBudget, count);
            }

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des données de dépenses:");
            e.printStackTrace();
        }
    }

    // Bouton Ajouter (+) : affiche le formulaire AjouterBudget.fxml
    @FXML
    private void onAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterBudget.fxml"));
            Parent form = loader.load();

            showInMainContent(form);

            // Passer une référence de ce contrôleur au contrôleur AjouterBudget
            AjouterBudgetController ctrl = loader.getController();
            ctrl.setMainPageController(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showInMainContent(Parent content) {
        if (mainContent == null) {
            return;
        }
        mainContent.getChildren().setAll(content);
        AnchorPane.setTopAnchor(content, 0.0);
        AnchorPane.setRightAnchor(content, 0.0);
        AnchorPane.setBottomAnchor(content, 0.0);
        AnchorPane.setLeftAnchor(content, 0.0);
    }

    // Afficher une carte budget dans la liste
    public void afficherBudget(String nom, double montant, String periode, String statut) {
        afficherBudgetComplet(new Budget(), nom, montant, periode, statut);
    }

    public void afficherBudgetComplet(Budget budget, String nom, double montant, String periode, String statut) {
        VBox card = new VBox(12);
        card.getStyleClass().add("account-card");

        // Header avec boutons d'action à gauche
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle("-fx-padding: 0 0 10 0; -fx-border-color: #F0F0F0; -fx-border-width: 0 0 1 0;");

        // Bouton Supprimer (ROUGE)
        Button btnDelete = new Button("🗑️");
        btnDelete.getStyleClass().addAll("btn-delete");
        btnDelete.setPrefWidth(45);
        btnDelete.setPrefHeight(45);
        btnDelete.setStyle("-fx-font-size: 20;");
        btnDelete.setOnAction(e -> onSupprimerBudget(budget));

        // Bouton Modifier (ORANGE) - Flèches
        Button btnModify = new Button("↔️");
        btnModify.getStyleClass().addAll("btn-modify");
        btnModify.setPrefWidth(45);
        btnModify.setPrefHeight(45);
        btnModify.setStyle("-fx-font-size: 20;");
        btnModify.setOnAction(e -> onModifierBudget(budget));

        Label header = new Label(nom);
        header.getStyleClass().add("section-title");
        header.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        headerBox.getChildren().addAll(btnDelete, btnModify, header);

        // Contenu principal
        Label typeLabel = new Label(periode);
        typeLabel.getStyleClass().add("card-type-header");

        Label amountCaption = new Label("Montant Total:");
        amountCaption.getStyleClass().add("card-type-header");

        Label amount = new Label(String.format("%.2f DT", montant));
        amount.getStyleClass().add("card-solde-text");

        Label badge = new Label(statut.equalsIgnoreCase("actif") ? "✓ Actif" : "✗ Bloqué");
        if ("actif".equalsIgnoreCase(statut)) {
            badge.getStyleClass().add("badge-actif");
        } else {
            badge.getStyleClass().add("badge-bloque");
        }

        // Bouton Ajouter une dépense (ORANGE)
        Button btnAddExpense = new Button("➕ Ajouter une dépense");
        btnAddExpense.getStyleClass().add("btn-add-expense");
        btnAddExpense.setStyle("-fx-font-size: 14; -fx-padding: 10 20;");
        btnAddExpense.setOnAction(e -> onAjouterDepense(budget));

        card.getChildren().addAll(headerBox, typeLabel, amountCaption, amount, badge, btnAddExpense);
        budgetList.getChildren().add(card);
    }

    @FXML
    private void onModifierBudget(Budget budget) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierBudget.fxml"));
            Parent form = loader.load();

            showInMainContent(form);

            ModifierBudgetController ctrl = loader.getController();
            ctrl.setMainPageController(this);
            ctrl.setBudget(budget);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onAjouterDepense(Budget budget) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterDepenseForm.fxml"));
            Parent form = loader.load();

            showInMainContent(form);

            AjouterDepenseController ctrl = loader.getController();
            ctrl.setMainPageController(this);
            ctrl.setBudget(budget);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSupprimerBudget(Budget budget) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le budget ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le budget \"" + budget.getNomBudget() + "\" ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Services.ServiceBudget serviceBudget = new Services.ServiceBudget();
                serviceBudget.supprimer(budget);

                System.out.println("Budget supprimé avec succès!");
                refreshBudgets();

            } catch (Exception e) {
                System.err.println("Erreur lors de la suppression du budget:");
                e.printStackTrace();
            }
        }
    }

    // Méthode pour revenir à la liste après ajout
    public void retournerAListe() {
        if (mainPageContent == null) {
            return;
        }
        showInMainContent(mainPageContent);
    }

    // Charger les budgets existants depuis la base MySQL
    private void chargerBudgetsDepuisBase() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/fintrack", "root", "")) {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_budget, nom_budget, montant_total, periode, statut, date_creation, id_utilisateur FROM budget");

            while (rs.next()) {
                Budget budget = new Budget();
                budget.setIdBudget(rs.getInt("id_budget"));
                budget.setIdUtilisateur(rs.getInt("id_utilisateur"));
                budget.setNomBudget(rs.getString("nom_budget"));
                budget.setMontantTotal(rs.getDouble("montant_total"));
                budget.setPeriode(rs.getString("periode"));
                budget.setStatut(rs.getString("statut"));
                budget.setDateCreation(rs.getDate("date_creation"));

                // Calculer le montant initial (montant initial = montant_total + somme des dépenses)
                double montantInitial = budget.getMontantTotal() + calculerMontantDepenses(budget.getIdBudget());
                budget.setMontantInitial(montantInitial);

                budgets.add(budget);
                afficherBudgetComplet(budget, budget.getNomBudget(), budget.getMontantTotal(),
                                    budget.getPeriode(), budget.getStatut());
            }

        } catch (Exception e) {
            System.err.println("Erreur de connexion à la base de données:");
            System.err.println("Vérifiez que MySQL est lancé et que la base 'fintrack' existe.");
            System.err.println("Détails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double calculerMontantDepenses(int idBudget) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/fintrack", "root", "")) {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT SUM(montant) as total FROM depense WHERE id_budget = " + idBudget);

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (Exception e) {
            System.err.println("Erreur lors du calcul des dépenses:");
            e.printStackTrace();
        }
        return 0;
    }

    private void afficherGraphique() {
        if (chartContainer == null || budgets.isEmpty()) {
            return;
        }

        chargerDonneesDepenses();

        // Créer les axes
        final CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Budgets");

        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Pourcentage de dépenses (%)");
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);

        // Créer le graphique en barres
        final BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Analyse des Dépenses par Budget");
        barChart.setStyle("-fx-font-size: 12;");
        barChart.setPrefHeight(350);
        barChart.setLegendVisible(false);

        // Créer une seule série pour toutes les barres
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("% Dépenses");

        // Couleurs pour chaque budget
        String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2", "#F8B500", "#52C41A"};
        int colorIndex = 0;

        // Ajouter les données pour chaque budget
        for (Budget budget : budgets) {
            // Calculer le pourcentage de dépenses
            double montantDepense = budget.getMontantInitial() - budget.getMontantTotal();
            double pourcentageDepense = (budget.getMontantInitial() > 0)
                ? (montantDepense / budget.getMontantInitial()) * 100
                : 0;

            // Limiter entre 0 et 100
            pourcentageDepense = Math.max(0, Math.min(100, pourcentageDepense));

            // Ajouter un point de données
            XYChart.Data<String, Number> data = new XYChart.Data<>(budget.getNomBudget(), pourcentageDepense);
            series.getData().add(data);

            // Stocker la couleur pour chaque barre
            String color = colors[colorIndex % colors.length];
            data.setExtraValue(color);
            colorIndex++;
        }

        barChart.getData().add(series);

        // Appliquer les couleurs directement aux nœuds
        for (int i = 0; i < series.getData().size(); i++) {
            XYChart.Data<String, Number> data = series.getData().get(i);
            String color = colors[i % colors.length];
            data.nodeProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    newValue.setStyle("-fx-bar-fill: " + color + ";");
                }
            });
        }

        // Appliquer les couleurs après un court délai pour s'assurer que les nœuds sont créés
        javafx.application.Platform.runLater(() -> {
            int index = 0;
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    String color = colors[index % colors.length];
                    data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
                index++;
            }
        });

        barChart.setStyle("-fx-padding: 20; -fx-font-size: 12;");

        chartContainer.getChildren().clear();
        Label title = new Label("📊 Analyse des Dépenses");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #182d88;");
        chartContainer.getChildren().addAll(title, barChart);
        VBox.setVgrow(barChart, Priority.ALWAYS);
    }

    // ...existing code...
}
