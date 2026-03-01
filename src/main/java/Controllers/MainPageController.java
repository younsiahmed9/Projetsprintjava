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
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.concurrent.Task;
import javafx.scene.Group;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import Models.Budget;
import Models.AiContext;
import Services.AiAssistantService;
import Services.BudgetInsightsService;
import Services.AssistantIA;

public class MainPageController {

    private static final int ALERT_THRESHOLD_PERCENT = 80;
    private static final double ALERT_THRESHOLD = ALERT_THRESHOLD_PERCENT / 100.0;

    @FXML
    private FlowPane budgetList;

    @FXML
    private AnchorPane mainContent;

    @FXML
    private javafx.scene.control.ScrollPane mainPageContent;

    @FXML
    private VBox chartContainer;

    @FXML
    private VBox notificationContainer;

    @FXML
    private VBox notificationList;

    @FXML
    private ListView<String> aiChatListView;

    @FXML
    private TextField aiInput;

    @FXML
    private Button aiSendButton;

    private List<Budget> budgets = new ArrayList<>();
    private Map<Integer, Integer> depenseCount = new HashMap<>();

    private final AiAssistantService aiAssistantService = new AiAssistantService();
    private final BudgetInsightsService budgetInsightsService = new BudgetInsightsService();
    private final AssistantIA assistantIA = new AssistantIA();

    // Méthode appelée au démarrage de la page
    @FXML
    public void initialize() {
        setupAiChatList();
        refreshBudgets();
    }

    private void setupAiChatList() {
        if (aiChatListView == null) {
            return;
        }
        aiChatListView.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setWrapText(true);
            }
        });
    }

    public void refreshBudgets() {
        if (budgetList != null) {
            budgetList.getChildren().clear();
        }
        budgets.clear();
        depenseCount.clear();
        chargerBudgetsDepuisBase();
        mettreAJourNotifications();
        afficherGraphique();
    }

    private void mettreAJourNotifications() {
        if (notificationContainer == null || notificationList == null) {
            return;
        }

        notificationList.getChildren().clear();
        ensureNotificationTable();

        for (Budget budget : budgets) {
            double montantInitial = budget.getMontantInitial();
            if (montantInitial <= 0) {
                continue;
            }
            double montantDepense = montantInitial - budget.getMontantTotal();
            double ratio = montantDepense / montantInitial;

            if (ratio >= ALERT_THRESHOLD) {
                ajouterNotificationBudget(budget, ratio, montantDepense, montantInitial);
                if (!isBudgetNotified(budget.getIdBudget(), ALERT_THRESHOLD_PERCENT)) {
                    markBudgetNotified(budget.getIdBudget(), ALERT_THRESHOLD_PERCENT);
                }
            } else {
                deleteBudgetNotification(budget.getIdBudget(), ALERT_THRESHOLD_PERCENT);
            }
        }

        boolean hasAlerts = !notificationList.getChildren().isEmpty();
        notificationContainer.setVisible(hasAlerts);
        notificationContainer.setManaged(hasAlerts);
    }

    private void deleteBudgetNotification(int budgetId, int seuilPercent) {
        String sql = "DELETE FROM budget_notification WHERE id_budget = ? AND seuil_percent = ?";
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/fintrack", "root", "");
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, budgetId);
            ps.setInt(2, seuilPercent);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression de la notification budget:");
            e.printStackTrace();
        }
    }

    private void ajouterNotificationBudget(Budget budget, double ratio, double montantDepense, double montantInitial) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("budget-alert-row");

        Label icon = new Label("⚠");
        icon.getStyleClass().add("budget-alert-icon");

        String message = String.format(
                "Le budget \"%s\" a atteint %.0f%% des dépenses (%.2f / %.2f DT).",
                budget.getNomBudget(),
                ratio * 100,
                montantDepense,
                montantInitial);
        Label text = new Label(message);
        text.getStyleClass().add("budget-alert-text");

        row.getChildren().addAll(icon, text);
        notificationList.getChildren().add(row);
    }

    private void ensureNotificationTable() {
        String sql = "CREATE TABLE IF NOT EXISTS budget_notification ("
                + "id_notification INT AUTO_INCREMENT PRIMARY KEY, "
                + "id_budget INT NOT NULL, "
                + "seuil_percent INT NOT NULL, "
                + "date_notified TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "UNIQUE KEY uniq_budget_seuil (id_budget, seuil_percent)"
                + ")";
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/fintrack", "root", "");
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la table budget_notification:");
            e.printStackTrace();
        }
    }

    private boolean isBudgetNotified(int budgetId, int seuilPercent) {
        String sql = "SELECT 1 FROM budget_notification WHERE id_budget = ? AND seuil_percent = ? LIMIT 1";
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/fintrack", "root", "");
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, budgetId);
            ps.setInt(2, seuilPercent);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des notifications:");
            e.printStackTrace();
        }
        return false;
    }

    private void markBudgetNotified(int budgetId, int seuilPercent) {
        String sql = "INSERT INTO budget_notification (id_budget, seuil_percent) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/fintrack", "root", "");
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, budgetId);
            ps.setInt(2, seuilPercent);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement de la notification:");
            e.printStackTrace();
        }
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

    @FXML
    private void onOpenSimulateur() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SimulateurEpargne.fxml"));
            Parent form = loader.load();

            showInMainContent(form);

            double totalRemaining = 0;
            for (Budget b : budgets) {
                totalRemaining += b.getMontantTotal();
            }

            SimulateurEpargneController ctrl = loader.getController();
            ctrl.setMainPageController(this, totalRemaining);

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

        Label amount = new Label();
        double amountAbs = Math.abs(montant);
        String amountText = (montant < 0 ? "- " : "") + String.format("%.2f DT", amountAbs);
        amount.setText(amountText);
        amount.getStyleClass().add("card-solde-text");
        if (montant < 0) {
            amount.getStyleClass().add("card-solde-negative");
        }

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
            ResultSet rs = stmt.executeQuery(
                    "SELECT id_budget, nom_budget, montant_total, periode, statut, date_creation, id_utilisateur FROM budget");

            while (rs.next()) {
                Budget budget = new Budget();
                budget.setIdBudget(rs.getInt("id_budget"));
                budget.setIdUtilisateur(rs.getInt("id_utilisateur"));
                budget.setNomBudget(rs.getString("nom_budget"));
                budget.setMontantTotal(rs.getDouble("montant_total"));
                budget.setPeriode(rs.getString("periode"));
                budget.setStatut(rs.getString("statut"));
                budget.setDateCreation(rs.getDate("date_creation"));

                // Calculer le montant initial (montant initial = montant_total + somme des
                // dépenses)
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
        xAxis.setTickLabelRotation(30);
        xAxis.setTickLabelGap(8);
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickLabelFill(javafx.scene.paint.Color.web("#334155"));
        xAxis.setTickLabelFont(javafx.scene.text.Font.font("Segoe UI", 12));

        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Pourcentage de dépenses (%)");
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);
        yAxis.setTickUnit(10);
        yAxis.setMinorTickVisible(false);
        yAxis.setTickLabelsVisible(true);
        yAxis.setTickLabelFill(javafx.scene.paint.Color.web("#334155"));
        yAxis.setTickLabelFont(javafx.scene.text.Font.font("Segoe UI", 12));

        // Créer le graphique en barres
        final BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Analyse des Dépenses par Budget");
        barChart.setStyle("-fx-font-size: 12;");
        barChart.setPrefHeight(430);
        barChart.setMinHeight(430);
        barChart.setLegendVisible(false);
        barChart.setHorizontalGridLinesVisible(true);
        barChart.setVerticalGridLinesVisible(false);
        barChart.setVerticalZeroLineVisible(true);
        barChart.setAlternativeRowFillVisible(false);
        barChart.setAlternativeColumnFillVisible(false);
        barChart.setCategoryGap(18);
        barChart.setBarGap(6);

        // Créer une seule série pour toutes les barres
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("% Dépenses");

        // Couleurs pour chaque budget (uniquement bleu foncé)
        String color = "#182d88";

        // Ajouter les données pour chaque budget
        for (Budget budget : budgets) {
            double montantInitial = budget.getMontantInitial();
            double montantDepense = calculerMontantDepenses(budget.getIdBudget());
            double pourcentageDepense = (montantInitial > 0)
                    ? (montantDepense / montantInitial) * 100
                    : 0;

            // Limiter entre 0 et 100
            pourcentageDepense = Math.max(0, Math.min(100, pourcentageDepense));

            // Ajouter un point de données (Format: "Nom (Montant DT)")
            String xLabel = String.format("%s (%.0f DT)", budget.getNomBudget(), montantInitial);
            XYChart.Data<String, Number> data = new XYChart.Data<>(xLabel, pourcentageDepense);
            series.getData().add(data);
        }

        barChart.getData().add(series);

        // Appliquer les couleurs et ajouter l'animation (sans labels, comme dans
        // l'image)
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                javafx.scene.Node node = data.getNode();
                if (node instanceof javafx.scene.layout.StackPane) {
                    javafx.scene.layout.StackPane bar = (javafx.scene.layout.StackPane) node;
                    bar.setStyle("-fx-bar-fill: " + color + "; -fx-background-color: " + color
                            + "; -fx-background-radius: 10 10 0 0;");

                    // Animation propre fluide
                    double targetValue = data.getYValue().doubleValue();
                    data.setYValue(0);

                    javafx.animation.Timeline timeline = new javafx.animation.Timeline();
                    javafx.animation.KeyValue kv = new javafx.animation.KeyValue(data.YValueProperty(), targetValue,
                            javafx.animation.Interpolator.EASE_OUT);
                    javafx.animation.KeyFrame kf = new javafx.animation.KeyFrame(javafx.util.Duration.millis(1200), kv);
                    timeline.getKeyFrames().add(kf);
                    timeline.play();
                }
            }
        });

        barChart.setStyle("-fx-padding: 20; -fx-font-size: 12;");

        chartContainer.getChildren().clear();
        Label title = new Label("📊 Analyse des Dépenses");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #182d88;");
        chartContainer.getChildren().addAll(title, barChart);
        VBox.setVgrow(barChart, Priority.ALWAYS);
    }

    @FXML
    private void onAiSend() {
        if (aiInput == null || aiChatListView == null) {
            return;
        }
        String message = aiInput.getText();
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        appendChatMessage("Vous", message.trim());
        aiInput.clear();
        if (aiSendButton != null) {
            aiSendButton.setDisable(true);
        }

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                AiContext context = budgetInsightsService.buildContext(budgets);
                return aiAssistantService.getReply(message, context);
            }
        };

        task.setOnSucceeded(event -> {
            appendChatMessage("IA", task.getValue());
            if (aiSendButton != null) {
                aiSendButton.setDisable(false);
            }
        });

        task.setOnFailed(event -> {
            String details = task.getException() != null ? task.getException().getMessage() : "";
            appendChatMessage("IA", "Erreur lors du traitement de la demande. " + details);
            if (aiSendButton != null) {
                aiSendButton.setDisable(false);
            }
        });

        Thread worker = new Thread(task, "ai-chat-worker");
        worker.setDaemon(true);
        worker.start();
    }

    private void appendChatMessage(String sender, String message) {
        if (aiChatListView == null) {
            return;
        }
        aiChatListView.getItems().add(sender + ": " + message);
        aiChatListView.scrollTo(aiChatListView.getItems().size() - 1);
    }

    @FXML
    private void onAiAdvice() {
        if (budgets == null || budgets.isEmpty()) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Conseil IA");
            alert.setHeaderText("Aucune donnee disponible");
            alert.setContentText("Ajoutez des budgets et des depenses pour obtenir un conseil.");
            alert.showAndWait();
            return;
        }

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return assistantIA.analyserDepenses(budgets);
            }
        };

        task.setOnSucceeded(event -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Conseil IA");
            alert.setHeaderText("Analyse des depenses");
            alert.setContentText(task.getValue());
            alert.showAndWait();
        });

        task.setOnFailed(event -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Conseil IA");
            alert.setHeaderText("Erreur IA");
            String details = task.getException() != null ? task.getException().getMessage() : "";
            alert.setContentText("Impossible d'obtenir un conseil. " + details);
            alert.showAndWait();
        });

        Thread worker = new Thread(task, "ai-advice-worker");
        worker.setDaemon(true);
        worker.start();
    }
}
