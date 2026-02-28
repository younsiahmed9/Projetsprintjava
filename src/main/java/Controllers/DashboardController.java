package Controllers;

import Models.*;
import Services.CarteVirtuelleService;
import Services.PortefeuilleService;
import Services.TransactionService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    // ============ FXML INJECTIONS ============
    @FXML private Label userGreeting;
    @FXML private Label headerSubtitle;
    @FXML private VBox cartesContainer;

    @FXML private TableView<Portefeuille> portefeuilleTable;
    @FXML private TableColumn<Portefeuille, Number> idColumn;
    @FXML private TableColumn<Portefeuille, String> nomColumn;
    @FXML private TableColumn<Portefeuille, Number> soldeColumn;
    @FXML private TableColumn<Portefeuille, String> deviseColumn;
    @FXML private TableColumn<Portefeuille, String> dateColumn;

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> txDateColumn;
    @FXML private TableColumn<Transaction, String> txTypeColumn;
    @FXML private TableColumn<Transaction, Number> txMontantColumn;
    @FXML private TableColumn<Transaction, String> txDeviseColumn;
    @FXML private TableColumn<Transaction, String> txStatutColumn;
    @FXML private TableColumn<Transaction, String> txDescriptionColumn;

    @FXML private Label statPortefeuilles;
    @FXML private Label statCartes;
    @FXML private Label statSolde;
    @FXML private Label statNormal;
    @FXML private Label statGold;
    @FXML private Label statSilver;

    @FXML private Label messageLabel;

    // ============ SERVICES ============
    private final PortefeuilleService portefeuilleService = new PortefeuilleService();
    private final CarteVirtuelleService carteService = new CarteVirtuelleService();
    private final TransactionService transactionService = new TransactionService();

    // ============ DATA ============
    private final ObservableList<Portefeuille> portefeuilles = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupUserInfo();
        setupTableColumns();
        loadData();
    }

    // ============ SETUP ============
    private void setupUserInfo() {
        Utilisateur user = Session.getUtilisateur();
        if (user != null) {
            String role = (user.getPassword() != null && user.getPassword().contains("admin")) ? "Admin" : "User";
            userGreeting.setText(String.format("Bienvenue, %s %s", user.getPrenom(), user.getNom()));
            headerSubtitle.setText(String.format("(Rôle: %s) | Solde: %.2f DT", role, user.getSolde()));
        }
    }

    private void setupTableColumns() {
        // Portefeuille Table Columns
        idColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        nomColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        soldeColumn.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSoldeTotal()));
        deviseColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDevisePrincipale()));
        dateColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedAt() != null ?
                c.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) :
                "N/A"
        ));

        portefeuilleTable.setItems(portefeuilles);

        // Transaction Table Columns
        txDateColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDate() != null ?
                c.getValue().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) :
                "N/A"
        ));
        txTypeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType().name()));
        txMontantColumn.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getMontant()));
        txDeviseColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDevise()));
        txStatutColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatut().name()));
        txDescriptionColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));

        transactionTable.setItems(transactions);
    }

    // ============ DATA LOADING ============
    private void loadData() {
        loadPortefeuilles();
        loadCartes();
        loadTransactions();
        updateStatistics();
    }

    private void loadPortefeuilles() {
        try {
            List<Portefeuille> all = portefeuilleService.afficherTous();
            portefeuilles.setAll(all);
            messageLabel.setText("");
        } catch (Exception e) {
            showError("Erreur chargement portefeuilles: " + e.getMessage());
        }
    }

    private void loadCartes() {
        try {
            cartesContainer.getChildren().clear();
            List<CarteVirtuelle> cartes = carteService.afficherTous();

            for (CarteVirtuelle carte : cartes) {
                cartesContainer.getChildren().add(createCarteCard(carte));
            }
        } catch (Exception e) {
            showError("Erreur chargement cartes: " + e.getMessage());
        }
    }

    private VBox createCarteCard(CarteVirtuelle carte) {
        VBox card = new VBox();
        card.setSpacing(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-border-radius: 10; -fx-background-radius: 10;");

        // Déterminer le style selon le type
        if (carte.getType() == TypeCarte.GOLD) {
            card.getStyleClass().add("carte-card-gold");
        } else if (carte.getType() == TypeCarte.SILVER) {
            card.getStyleClass().add("carte-card-silver");
        } else {
            card.getStyleClass().add("carte-card-normal");
        }

        // Type de carte
        Label typeLabel = new Label(carte.getType().name());
        typeLabel.getStyleClass().add("carte-type");

        // Numéro masqué
        String numerMasque = maskCardNumber(carte.getNumeroCarte());
        Label numeroLabel = new Label(numerMasque);
        numeroLabel.getStyleClass().add("carte-numero");

        // Solde et devise
        HBox soldeBox = new HBox(10);
        Label soldeLabel = new Label(String.format("%.2f", carte.getSolde()));
        soldeLabel.getStyleClass().add("carte-solde");
        Label deviseLabel = new Label(carte.getDevise().name());
        deviseLabel.getStyleClass().add("carte-devise");
        soldeBox.getChildren().addAll(soldeLabel, deviseLabel);

        // Plafond
        Label plafondLabel = new Label(String.format("Plafond: %.2f", carte.getPlafond()));
        plafondLabel.setStyle("-fx-font-size: 10; -fx-opacity: 0.8;");

        card.getChildren().addAll(typeLabel, numeroLabel, soldeBox, plafondLabel);
        return card;
    }

    private String maskCardNumber(String numero) {
        if (numero == null || numero.length() < 4) return numero;
        int len = numero.length();
        return "**** **** **** " + numero.substring(len - 4);
    }

    private void loadTransactions() {
        try {
            List<Transaction> all = transactionService.afficherTous();
            // Limiter aux 10 dernières
            if (all.size() > 10) {
                all = all.subList(0, 10);
            }
            transactions.setAll(all);
        } catch (Exception e) {
            showError("Erreur chargement transactions: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        try {
            // Compter portefeuilles
            List<Portefeuille> allPortefeuilles = portefeuilleService.afficherTous();
            statPortefeuilles.setText(String.valueOf(allPortefeuilles.size()));

            // Compter cartes
            List<CarteVirtuelle> allCartes = carteService.afficherTous();
            statCartes.setText(String.valueOf(allCartes.size()));

            // Calculer solde total (simplifié - tous les soldes en DT)
            double totalSolde = allPortefeuilles.stream()
                    .mapToDouble(Portefeuille::getSoldeTotal)
                    .sum();
            statSolde.setText(String.format("%.2f", totalSolde));

            // Compter par type de carte
            long countNormal = allCartes.stream().filter(c -> c.getType() == TypeCarte.NORMAL).count();
            long countGold = allCartes.stream().filter(c -> c.getType() == TypeCarte.GOLD).count();
            long countSilver = allCartes.stream().filter(c -> c.getType() == TypeCarte.SILVER).count();

            statNormal.setText(String.valueOf(countNormal));
            statGold.setText(String.valueOf(countGold));
            statSilver.setText(String.valueOf(countSilver));
        } catch (Exception e) {
            showError("Erreur calcul statistiques: " + e.getMessage());
        }
    }

    // ============ ACTION HANDLERS ============
    @FXML
    private void handleRefresh() {
        loadData();
        showSuccess("Données actualisées");
    }

    @FXML
    private void handleAjouterPortefeuille() {
        showInfo("Fonctionnalité 'Ajouter Portefeuille' à implémenter");
        // TODO: Ouvrir un dialog pour ajouter un portefeuille
    }

    @FXML
    private void handleModifierPortefeuille() {
        Portefeuille selected = portefeuilleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez sélectionner un portefeuille");
            return;
        }
        showInfo("Fonctionnalité 'Modifier Portefeuille' à implémenter");
    }

    @FXML
    private void handleSupprimerPortefeuille() {
        Portefeuille selected = portefeuilleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez sélectionner un portefeuille");
            return;
        }
        showInfo("Fonctionnalité 'Supprimer Portefeuille' à implémenter");
    }

    @FXML
    private void handleAjouterCarte() {
        showInfo("Fonctionnalité 'Ajouter Carte' à implémenter");
        // TODO: Ouvrir un dialog pour ajouter une carte
    }

    @FXML
    private void handleAjouterTransaction() {
        showInfo("Fonctionnalité 'Ajouter Transaction' à implémenter");
        // TODO: Ouvrir un dialog pour ajouter une transaction
    }

    @FXML
    private void handleDeconnexion() {
        Session.clear();
        try {
            var resource = getClass().getResource("/fxml/login.fxml");
            if (resource != null) {
                Parent root = FXMLLoader.load(resource);
                Stage stage = (Stage) userGreeting.getScene().getWindow();
                stage.setScene(new Scene(root, 600, 400));
                stage.setTitle("FinTrack - Connexion");
            } else {
                showError("Ressource login.fxml non trouvée");
            }
        } catch (IOException e) {
            showError("Erreur navigation: " + e.getMessage());
        }
    }

    // ============ UTILITIES ============
    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        messageLabel.setText("✅ " + message);
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        messageLabel.setText("❌ " + message);
    }

    private void showInfo(String message) {
        messageLabel.setStyle("-fx-text-fill: #3498db;");
        messageLabel.setText("ℹ️ " + message);
    }
}

