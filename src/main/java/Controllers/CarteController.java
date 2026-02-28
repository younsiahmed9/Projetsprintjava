package Controllers;

import Models.Session;
import Models.Utilisateur;
import Models.CarteVirtuelle;
import Services.CarteVirtuelleService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * Contrôleur UI pour la page carte_dashboard.fxml.
 * Objectif: navigation + actions, avec implémentation métier basique.
 */
public class CarteController {

    @FXML private Label headerTitleLabel;
    @FXML private Label headerUserLabel;
    @FXML private Label pageTitleLabel;
    @FXML private Label messageLabel;

    @FXML private FlowPane cartesFlow;

    private final CarteVirtuelleService carteService = new CarteVirtuelleService();

    @FXML
    public void initialize() {
        Utilisateur u = Session.getUtilisateur();
        headerUserLabel.setText(u != null ? (u.getPrenom() + " " + u.getNom()) : "Invité");
        headerTitleLabel.setText("Cartes");
        pageTitleLabel.setText("Cartes du portefeuille");

        messageLabel.setText("");

        Integer pfId = Session.getSelectedPortefeuilleId();
        if (pfId == null) {
            messageLabel.setText("Aucun portefeuille sélectionné.");
            return;
        }

        loadCartesForPortefeuille(pfId);
    }

    private void loadCartesForPortefeuille(int portefeuilleId) {
        cartesFlow.getChildren().clear();
        try {
            List<CarteVirtuelle> list = carteService.getCartesByPortefeuille(portefeuilleId);
            for (CarteVirtuelle c : list) {
                // Rendu minimal de la carte
                javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(6);
                Label num = new Label(c.getNumeroCarte() != null && c.getNumeroCarte().length() >= 4 ? "**** **** **** " + c.getNumeroCarte().substring(c.getNumeroCarte().length()-4) : "****");
                Label solde = new Label(String.format("%.2f %s", c.getSolde(), c.getDevise()));
                card.getChildren().addAll(num, solde);
                card.getStyleClass().addAll("card", "carte-card");
                cartesFlow.getChildren().add(card);
            }
            messageLabel.setText(list.isEmpty() ? "Aucune carte." : "");
        } catch (Exception e) {
            messageLabel.setText("Erreur chargement cartes: " + e.getMessage());
        }
    }

    // Navigation
    @FXML
    private void goBackPortefeuilles() {
        try {
            var resource = getClass().getResource("/fxml/portefeuille_dashboard.fxml");
            if (resource == null) { messageLabel.setText("portefeuille_dashboard.fxml introuvable"); return; }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) cartesFlow.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 800);
            scene.getStylesheets().add(getClass().getResource("/styles/colors.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("FinTrack - Portefeuilles");
        } catch (IOException e) {
            messageLabel.setText("Erreur navigation: " + e.getMessage());
        }
    }

    // Actions (stubs)
    @FXML
    private void handleAjouterCarte() {
        messageLabel.setText("Action: ajouter carte (stub)");
    }

    @FXML
    private void handleRecharger() {
        messageLabel.setText("Action: recharger carte (stub)");
    }

    @FXML
    private void handleTransfert() {
        messageLabel.setText("Action: transfert entre cartes (stub)");
    }

    @FXML
    private void handleToggle() {
        messageLabel.setText("Action: activer/désactiver (stub)");
    }

    @FXML
    private void handleSupprimer() {
        messageLabel.setText("Action: supprimer carte (stub)");
    }

    @FXML
    private void handleRefresh() {
        Integer pfId = Session.getSelectedPortefeuilleId();
        if (pfId != null) loadCartesForPortefeuille(pfId);
    }

    @FXML
    private void handleDeconnexion() {
        Session.clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) cartesFlow.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 450));
            stage.setTitle("FinTrack - Connexion");
        } catch (IOException e) {
            messageLabel.setText("Erreur déconnexion: " + e.getMessage());
        }
    }
}
