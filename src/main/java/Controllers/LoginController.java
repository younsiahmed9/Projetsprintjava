package Controllers;

import Models.Session;
import Models.Utilisateur;
import Services.UtilisateurService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        messageLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty()) {
            showError("Veuillez entrer votre email.");
            return;
        }
        if (password.isEmpty()) {
            showError("Veuillez entrer votre mot de passe.");
            return;
        }

        try {
            Utilisateur utilisateur = utilisateurService.login(email, password);

            if (utilisateur == null) {
                showError("Email ou mot de passe incorrect.");
                return;
            }

            Session.setUtilisateur(utilisateur);
            System.out.println("[Login] Utilisateur connecté: " + utilisateur);
            showSuccess("Connexion réussie! Bienvenue " + utilisateur.getPrenom());

            if (utilisateur.isAdmin()) {
                ouvrirAdminDashboard();
            } else {
                ouvrirUserDashboard();
            }

        } catch (Exception e) {
            showError("Erreur de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ouvrirAdminDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/admin_dashboard.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            var css = getClass().getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene);
            stage.setTitle("FinTrack - Administration");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Erreur navigation admin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ouvrirUserDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/client_dashboard.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 800);
            var css = getClass().getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene);
            stage.setTitle("FinTrack - Mes Portefeuilles");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Erreur navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        messageLabel.setText("❌ " + message);
    }

    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        messageLabel.setText("✅ " + message);
    }
}