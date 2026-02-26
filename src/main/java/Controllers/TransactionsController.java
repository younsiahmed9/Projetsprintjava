package Controllers;

import Models.Session;
import Models.Transaction;
import Models.Utilisateur;
import Services.TransactionService;
import Services.CarteVirtuelleService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionsController {

    @FXML private Label welcomeLabel;
    @FXML private Label lblTotalTransactions, lblMontantTotal, lblReussies;
    @FXML private VBox containerTransactions;
    @FXML private Label messageLabel;

    private TransactionService transactionService = new TransactionService();
    private CarteVirtuelleService carteService = new CarteVirtuelleService();
    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private String currentFilter = "ALL";

    @FXML
    public void initialize() {
        Utilisateur user = Session.getUtilisateur();
        if (user != null) {
            welcomeLabel.setText("Bienvenue, " + user.getPrenom() + " " + user.getNom());
        }
        loadTransactions();
    }

    private void loadTransactions() {
        // Récupérer toutes les transactions (pour l'instant)
        // Idéalement, on pourrait filtrer par utilisateur via les cartes
        List<Transaction> allTransactions = transactionService.afficherTous();
        transactions.setAll(allTransactions);
        applyFilter();
    }

    private void applyFilter() {
        List<Transaction> filtered = transactions.stream()
                .filter(t -> currentFilter.equals("ALL") || t.getType().toString().equals(currentFilter))
                .collect(Collectors.toList());
        displayTransactions(filtered);
        updateStats(filtered);
    }

    private void updateStats(List<Transaction> filtered) {
        lblTotalTransactions.setText(String.valueOf(filtered.size()));

        double montantTotal = filtered.stream()
                .mapToDouble(Transaction::getMontant)
                .sum();
        lblMontantTotal.setText(String.format("%.2f DT", montantTotal));

        long reussies = filtered.stream()
                .filter(t -> "SUCCESS".equals(t.getStatut().toString()))
                .count();
        lblReussies.setText(String.valueOf(reussies));
    }

    private void displayTransactions(List<Transaction> list) {
        containerTransactions.getChildren().clear();

        if (list.isEmpty()) {
            messageLabel.setText("Aucune transaction trouvée.");
            return;
        } else {
            messageLabel.setText("");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Transaction t : list) {
            HBox row = createTransactionRow(t, formatter);
            containerTransactions.getChildren().add(row);
        }
    }

    private HBox createTransactionRow(Transaction t, DateTimeFormatter formatter) {
        HBox row = new HBox(0);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPrefHeight(50);
        row.getStyleClass().add("custom-row-credit");
        row.setStyle("-fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");

        Label idLabel = new Label(String.valueOf(t.getId()));
        idLabel.setMinWidth(60);
        idLabel.setPrefWidth(60);
        idLabel.setMaxWidth(60);
        idLabel.getStyleClass().add("transaction-item");

        Label dateLabel = new Label(t.getDate().format(formatter));
        dateLabel.setMinWidth(150);
        dateLabel.setPrefWidth(150);
        dateLabel.setMaxWidth(150);
        dateLabel.getStyleClass().add("transaction-item");

        Label typeLabel = new Label(t.getType().toString());
        typeLabel.setMinWidth(100);
        typeLabel.setPrefWidth(100);
        typeLabel.setMaxWidth(100);
        typeLabel.getStyleClass().add("transaction-item");

        Label montantLabel = new Label(String.format("%.2f", t.getMontant()));
        montantLabel.setMinWidth(120);
        montantLabel.setPrefWidth(120);
        montantLabel.setMaxWidth(120);
        montantLabel.getStyleClass().add("transaction-item");

        Label deviseLabel = new Label(t.getDevise());
        deviseLabel.setMinWidth(80);
        deviseLabel.setPrefWidth(80);
        deviseLabel.setMaxWidth(80);
        deviseLabel.getStyleClass().add("transaction-item");

        Label statutLabel = new Label(t.getStatut().toString());
        statutLabel.setMinWidth(100);
        statutLabel.setPrefWidth(100);
        statutLabel.setMaxWidth(100);
        statutLabel.getStyleClass().addAll("status-badge",
                "SUCCESS".equals(t.getStatut().toString()) ? "status-active" : "status-pending");

        Label sourceLabel = new Label(getCardNumber(t.getCarteSourceId()));
        sourceLabel.setMinWidth(100);
        sourceLabel.setPrefWidth(100);
        sourceLabel.setMaxWidth(100);
        sourceLabel.getStyleClass().add("transaction-item");

        Label destLabel = new Label(getCardNumber(t.getCarteDestId()));
        destLabel.setMinWidth(100);
        destLabel.setPrefWidth(100);
        destLabel.setMaxWidth(100);
        destLabel.getStyleClass().add("transaction-item");

        Label descLabel = new Label(t.getDescription() != null ? t.getDescription() : "");
        descLabel.setMinWidth(200);
        descLabel.setPrefWidth(200);
        descLabel.setMaxWidth(200);
        descLabel.getStyleClass().add("transaction-item");

        row.getChildren().addAll(idLabel, dateLabel, typeLabel, montantLabel, deviseLabel,
                statutLabel, sourceLabel, destLabel, descLabel);
        return row;
    }

    private String getCardNumber(Integer cardId) {
        if (cardId == null) return "-";
        var carte = carteService.afficherParId(cardId);
        if (carte == null) return "Carte " + cardId;
        String num = carte.getNumeroCarte();
        if (num.length() >= 4) {
            return "****" + num.substring(num.length() - 4);
        }
        return "Carte " + cardId;
    }

    @FXML
    private void handleAllTransactions() {
        currentFilter = "ALL";
        applyFilter();
    }

    @FXML
    private void handleDepotTransactions() {
        currentFilter = "DEPOT";
        applyFilter();
    }

    @FXML
    private void handleRetraitTransactions() {
        currentFilter = "RETRAIT";
        applyFilter();
    }

    @FXML
    private void handleTransfertTransactions() {
        currentFilter = "TRANSFERT";
        applyFilter();
    }

    @FXML
    private void handlePortefeuilles() {
        navigateTo("/views/client_dashboard.fxml", "FinTrack - Mes Portefeuilles");
    }

    @FXML
    private void handleCartes() {
        Integer selectedPortefeuille = Session.getSelectedPortefeuilleId();
        if (selectedPortefeuille != null) {
            navigateTo("/views/carte_list.fxml", "FinTrack - Mes Cartes");
        } else {
            messageLabel.setText("Veuillez d'abord sélectionner un portefeuille.");
        }
    }

    @FXML
    private void handleTransferts() {
        navigateTo("/views/transfer_form.fxml", "FinTrack - Transfert");
    }

    @FXML
    private void handleScheduledTransfer() {
        navigateTo("/views/scheduled_transfer_form.fxml", "FinTrack - Transfert programmé");
    }

    @FXML
    private void handleLogout() {
        Session.clear();
        navigateTo("/views/login.fxml", "FinTrack - Connexion");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 800);
            try {
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            } catch (Exception e) {
                // CSS may not exist, ignore
            }
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (IOException e) {
            if (messageLabel != null) {
                messageLabel.setText("Erreur navigation: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }
}