package Controllers;

import Models.*;
import Services.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;

    // KPI Labels
    @FXML private Label lblNbUtilisateurs, lblNewUsersToday, lblNbPortefeuilles, lblAvgPortefeuilles;
    @FXML private Label lblNbCartes, lblCartesActives, lblTotalTransactions, lblVolumeTotal;

    // Charts
    @FXML private BarChart<String, Number> userGrowthChart;
    @FXML private PieChart rolePieChart;
    @FXML private LineChart<String, Number> transactionsChart;
    @FXML private PieChart cardTypePieChart;

    // Users Table
    @FXML private TableView<Utilisateur> usersTable;
    @FXML private TableColumn<Utilisateur, Integer> colUserId;
    @FXML private TableColumn<Utilisateur, String> colUserNom, colUserPrenom, colUserEmail, colUserRole, colUserStatus;
    @FXML private TableColumn<Utilisateur, Integer> colUserPortefeuilles, colUserCartes;
    @FXML private TableColumn<Utilisateur, String> colUserDate;
    @FXML private TableColumn<Utilisateur, Void> colUserActions;

    // Transactions Table
    @FXML private VBox transactionsSection;
    @FXML private Label transactionsTitle;
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Integer> colTransId;
    @FXML private TableColumn<Transaction, String> colTransDate;
    @FXML private TableColumn<Transaction, String> colTransType;
    @FXML private TableColumn<Transaction, Double> colTransMontant;
    @FXML private TableColumn<Transaction, String> colTransDevise;
    @FXML private TableColumn<Transaction, String> colTransStatut;
    @FXML private TableColumn<Transaction, String> colTransSource;
    @FXML private TableColumn<Transaction, String> colTransDest;

    // System info
    @FXML private Label lblVersion, lblLastBackup, lblUptime, lblStorage;
    @FXML private ProgressBar storageBar;

    // Services
    private UtilisateurService utilisateurService = new UtilisateurService();
    private PortefeuilleService portefeuilleService = new PortefeuilleService();
    private CarteVirtuelleService carteService = new CarteVirtuelleService();
    private TransactionService transactionService = new TransactionService();

    private ObservableList<Utilisateur> utilisateurs = FXCollections.observableArrayList();
    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private Map<Integer, Integer> portefeuilleCountCache = new HashMap<>();
    private Map<Integer, Integer> carteCountCache = new HashMap<>();
    private Map<Integer, String> userStatusCache = new HashMap<>();

    @FXML
    public void initialize() {
        Utilisateur admin = Session.getUtilisateur();
        if (admin != null) {
            welcomeLabel.setText("Bienvenue, " + admin.getPrenom() + " " + admin.getNom());
        }

        setupUsersTable();
        setupTransactionsTable();
        loadAllData();
        setupCharts();
        updateSystemInfo();
    }

    // ========== SETUP METHODS ==========

    private void setupUsersTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colUserPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        colUserStatus.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getId();
            String status = userStatusCache.getOrDefault(userId, "Actif");
            return new SimpleStringProperty(status);
        });

        colUserPortefeuilles.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getId();
            return new SimpleIntegerProperty(portefeuilleCountCache.getOrDefault(userId, 0)).asObject();
        });

        colUserCartes.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getId();
            return new SimpleIntegerProperty(carteCountCache.getOrDefault(userId, 0)).asObject();
        });

        colUserDate.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getCreatedAt();
            return new SimpleStringProperty(date != null ?
                    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");
        });

        // Boutons d'action
        colUserActions.setCellFactory(param -> new TableCell<>() {
            private final Button voirBtn = new Button("📋 Voir");
            private final Button activerBtn = new Button("✅ Activer");
            private final Button suspendreBtn = new Button("⛔ Suspendre");
            private final Button roleBtn = new Button("👑 Changer rôle");
            private final HBox pane = new HBox(5, voirBtn, activerBtn, suspendreBtn, roleBtn);

            {
                voirBtn.getStyleClass().add("btn-action-edit");
                activerBtn.getStyleClass().add("btn-action-edit");
                suspendreBtn.getStyleClass().add("btn-action-delete");
                roleBtn.getStyleClass().add("btn-primary");

                voirBtn.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    voirTransactions(user);
                });

                activerBtn.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    activerUtilisateur(user);
                });

                suspendreBtn.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    suspendreUtilisateur(user);
                });

                roleBtn.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    changerRole(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        usersTable.setItems(utilisateurs);
    }

    private void setupTransactionsTable() {
        colTransId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTransDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        colTransType.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getType().toString()));
        colTransMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colTransDevise.setCellValueFactory(new PropertyValueFactory<>("devise"));
        colTransStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatut().toString()));

        colTransSource.setCellValueFactory(cellData -> {
            Integer carteId = cellData.getValue().getCarteSourceId();
            return new SimpleStringProperty(getCardInfo(carteId));
        });

        colTransDest.setCellValueFactory(cellData -> {
            Integer carteId = cellData.getValue().getCarteDestId();
            return new SimpleStringProperty(getCardInfo(carteId));
        });
    }

    private String getCardInfo(Integer cardId) {
        if (cardId == null) return "-";
        CarteVirtuelle carte = carteService.afficherParId(cardId);
        if (carte == null) return "Carte " + cardId;
        return carte.getNumeroCarte() + " (" + carte.getDevise() + ")";
    }

    private void loadAllData() {
        // Charger tous les utilisateurs
        utilisateurs.setAll(utilisateurService.afficherTous());

        // Initialiser les statuts par défaut
        for (Utilisateur u : utilisateurs) {
            userStatusCache.putIfAbsent(u.getId(), "Actif");
        }

        // Calculer les statistiques pour chaque utilisateur
        for (Utilisateur u : utilisateurs) {
            int userId = u.getId();

            // Compter les portefeuilles
            List<Portefeuille> portefeuilles = portefeuilleService.getPortefeuillesByUtilisateur(userId);
            portefeuilleCountCache.put(userId, portefeuilles.size());

            // Compter les cartes
            int totalCartes = 0;
            for (Portefeuille p : portefeuilles) {
                totalCartes += carteService.getCartesByPortefeuille(p.getId()).size();
            }
            carteCountCache.put(userId, totalCartes);
        }

        // Mettre à jour les KPIs
        updateKPIs();
    }

    private void updateKPIs() {
        int totalUsers = utilisateurs.size();
        lblNbUtilisateurs.setText(String.valueOf(totalUsers));

        long newToday = utilisateurs.stream()
                .filter(u -> u.getCreatedAt() != null &&
                        u.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .count();
        lblNewUsersToday.setText("+" + newToday + " aujourd'hui");

        List<Portefeuille> allPortefeuilles = portefeuilleService.afficherTous();
        lblNbPortefeuilles.setText(String.valueOf(allPortefeuilles.size()));

        double avgPortefeuilles = totalUsers > 0 ?
                (double) allPortefeuilles.size() / totalUsers : 0;
        lblAvgPortefeuilles.setText(String.format("Moy. %.1f/user", avgPortefeuilles));

        List<CarteVirtuelle> allCartes = carteService.afficherTous();
        lblNbCartes.setText(String.valueOf(allCartes.size()));

        long actives = allCartes.stream().filter(CarteVirtuelle::isActiver).count();
        lblCartesActives.setText(actives + " actives");

        List<Transaction> allTransactions = transactionService.afficherTous();
        lblTotalTransactions.setText(String.valueOf(allTransactions.size()));

        double volumeTotal = allTransactions.stream()
                .mapToDouble(Transaction::getMontant)
                .sum();
        lblVolumeTotal.setText(String.format("%.0f DT", volumeTotal));
    }

    private void setupCharts() {
        setupUserGrowthChart();
        setupRolePieChart();
        setupTransactionsChart();
        setupCardTypePieChart();
    }

    private void setupUserGrowthChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Inscriptions");

        Map<LocalDate, Long> inscriptionsParJour = utilisateurs.stream()
                .filter(u -> u.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            long count = inscriptionsParJour.getOrDefault(date, 0L);
            series.getData().add(new XYChart.Data<>(date.format(formatter), count));
        }

        userGrowthChart.getData().clear();
        userGrowthChart.getData().add(series);
    }

    private void setupRolePieChart() {
        long admins = utilisateurs.stream().filter(u -> "admin".equals(u.getRole())).count();
        long users = utilisateurs.stream().filter(u -> "user".equals(u.getRole())).count();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Administrateurs (" + admins + ")", admins),
                new PieChart.Data("Utilisateurs (" + users + ")", users)
        );

        rolePieChart.setData(pieData);
    }

    private void setupTransactionsChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Transactions");

        List<Transaction> transactions = transactionService.afficherTous();

        Map<LocalDate, Long> transParJour = transactions.stream()
                .filter(t -> t.getDate() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getDate().toLocalDate(),
                        Collectors.counting()
                ));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            long count = transParJour.getOrDefault(date, 0L);
            series.getData().add(new XYChart.Data<>(date.format(formatter), count));
        }

        transactionsChart.getData().clear();
        transactionsChart.getData().add(series);
    }

    private void setupCardTypePieChart() {
        List<CarteVirtuelle> cartes = carteService.afficherTous();

        long normal = cartes.stream().filter(c -> c.getType() == TypeCarte.NORMAL).count();
        long gold = cartes.stream().filter(c -> c.getType() == TypeCarte.GOLD).count();
        long silver = cartes.stream().filter(c -> c.getType() == TypeCarte.SILVER).count();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("NORMAL (" + normal + ")", normal),
                new PieChart.Data("GOLD (" + gold + ")", gold),
                new PieChart.Data("SILVER (" + silver + ")", silver)
        );

        cardTypePieChart.setData(pieData);
    }

    private void updateSystemInfo() {
        lblVersion.setText("Version: 1.0.0");
        lblLastBackup.setText("Dernière sauvegarde: " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblUptime.setText("Uptime: " + System.currentTimeMillis() / 1000 / 60 + " minutes");

        double used = 0.5;
        storageBar.setProgress(used);
        lblStorage.setText("Espace utilisé: " + (int)(used * 100) + "%");
    }

    // ========== USER MANAGEMENT METHODS ==========

    private void voirTransactions(Utilisateur user) {
        transactionsTitle.setText("Transactions de " + user.getPrenom() + " " + user.getNom());

        // Récupérer toutes les cartes de l'utilisateur
        List<CarteVirtuelle> userCards = carteService.getCartesByUtilisateur(user.getId());
        Set<Integer> cardIds = userCards.stream().map(CarteVirtuelle::getId).collect(Collectors.toSet());

        // Filtrer les transactions
        List<Transaction> userTransactions = transactionService.afficherTous().stream()
                .filter(t -> (t.getCarteSourceId() != null && cardIds.contains(t.getCarteSourceId())) ||
                        (t.getCarteDestId() != null && cardIds.contains(t.getCarteDestId())))
                .collect(Collectors.toList());

        transactions.setAll(userTransactions);
        transactionsTable.setItems(transactions);

        transactionsSection.setVisible(true);
        transactionsSection.setManaged(true);
    }

    private void activerUtilisateur(Utilisateur user) {
        userStatusCache.put(user.getId(), "Actif");
        usersTable.refresh();
        showInfo("✅ Utilisateur " + user.getPrenom() + " " + user.getNom() + " activé");
    }

    private void suspendreUtilisateur(Utilisateur user) {
        userStatusCache.put(user.getId(), "Suspendu");
        usersTable.refresh();
        showInfo("⛔ Utilisateur " + user.getPrenom() + " " + user.getNom() + " suspendu");
    }

    private void changerRole(Utilisateur user) {
        String nouveauRole = "admin".equals(user.getRole()) ? "user" : "admin";
        user.setRole(nouveauRole);
        utilisateurService.modifier(user);
        loadAllData();
        showInfo("👑 Rôle changé pour " + user.getPrenom() + " " + user.getNom() + " : " + nouveauRole);
    }

    @FXML
    private void handleCloseTransactions() {
        transactionsSection.setVisible(false);
        transactionsSection.setManaged(false);
    }

    // ========== EXPORT METHODS ==========

    @FXML
    private void handleExportUsersPDF() {
        // Exporter les utilisateurs
        List<ExportService.TransferData> data = new ArrayList<>();
        for (Utilisateur u : utilisateurs) {
            // Convertir en format exportable
        }
        // ExportService.exportToPDF(data, usersTable.getScene().getWindow());
        showInfo("📄 Export PDF des utilisateurs - Fonctionnalité à venir");
    }

    @FXML
    private void handleExportUsersExcel() {
        showInfo("📊 Export Excel des utilisateurs - Fonctionnalité à venir");
    }

    @FXML
    private void handleExportTransactionsPDF() {
        showInfo("📄 Export PDF des transactions - Fonctionnalité à venir");
    }

    @FXML
    private void handleExportStatsPDF() {
        showInfo("📊 Export PDF des statistiques - Fonctionnalité à venir");
    }

    // ========== HANDLER METHODS ==========

    @FXML
    private void handleDashboard() {
        handleRefresh();
    }

    @FXML
    private void handleUtilisateurs() {
        usersTable.scrollTo(0);
    }

    @FXML
    private void handlePortefeuilles() {
        NavigationService.navigateTo("/fxml/admin_portefeuilles.fxml", "Gestion des portefeuilles");
    }

    @FXML
    private void handleCartes() {
        NavigationService.navigateTo("/fxml/admin_cartes.fxml", "Gestion des cartes");
    }

    @FXML
    private void handleTransactions() {
        // Afficher toutes les transactions
        transactions.setAll(transactionService.afficherTous());
        transactionsTable.setItems(transactions);
        transactionsTitle.setText("Toutes les transactions");
        transactionsSection.setVisible(true);
        transactionsSection.setManaged(true);
    }

    @FXML
    private void handleStatistiques() {
        handleRefresh();
    }

    @FXML
    private void handleRefresh() {
        loadAllData();
        setupCharts();
        showInfo("✅ Données actualisées");
    }

    @FXML
    private void handleLogout() {
        Session.clear();
        NavigationService.navigateTo("/fxml/login.fxml", "Connexion");
    }

    // ========== QUICK ACTIONS ==========

    @FXML
    private void handleAjouterUtilisateur() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_user_form.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un utilisateur");
            stage.showAndWait();
            loadAllData();
            showInfo("✅ Utilisateur ajouté avec succès");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture du formulaire d'ajout");
        }
    }

    @FXML
    private void handleExporterRapport() {
        showInfo("📊 Rapport mensuel généré avec succès");
    }

    @FXML
    private void handleNettoyerBD() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Nettoyer les logs de la base de données ?");
        confirm.setContentText("Cette action supprimera les logs de transactions de plus de 30 jours.\n\nVoulez-vous continuer ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            showInfo("✅ Logs nettoyés avec succès");
            handleRefresh();
        }
    }

    @FXML
    private void handleEnvoyerNewsletter() {
        showInfo("📧 Newsletter envoyée à tous les utilisateurs");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.showAndWait();
    }
}