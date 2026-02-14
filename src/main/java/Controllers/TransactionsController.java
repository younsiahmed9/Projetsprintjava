package Controllers;

import Models.Session;
import Models.Transaction;
import Services.TransactionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class TransactionsController {

    @FXML private VBox transactionsContainer;
    @FXML private Label headerUserLabel;
    @FXML private Label messageLabel;

    private final TransactionService transactionService = new TransactionService();

    @FXML
    public void initialize() {
        var user = Session.getUtilisateur();
        headerUserLabel.setText(user != null ? (user.getPrenom() + " " + user.getNom()) : "Invité");
        loadTransactions();
    }

    private void loadTransactions() {
        transactionsContainer.getChildren().clear();
        try {
            List<Transaction> list = transactionService.afficherTous();
            for (Transaction t : list) {
                Label row = new Label(String.format("%s | %s | %.2f %s | %s", t.getDate(), t.getType(), t.getMontant(), t.getDevise(), t.getStatut()));
                transactionsContainer.getChildren().add(row);
            }
        } catch (Exception e) {
            messageLabel.setText("Erreur chargement transactions: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        try {
            var resource = getClass().getResource("/views/portefeuille_dashboard.fxml");
            if (resource == null) { messageLabel.setText("portefeuille_dashboard.fxml introuvable"); return; }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) transactionsContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
            stage.setTitle("FinTrack - Portefeuilles");
        } catch (IOException e) {
            messageLabel.setText("Erreur navigation: " + e.getMessage());
        }
    }
}
