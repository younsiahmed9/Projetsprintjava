package Controllers;

import Models.Session;
import Models.Utilisateur;
import Services.UtilisateurService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Contrôleur pour l'écran de connexion.
 * Gère l'authentification et la redirection selon le rôle (admin/user).
 */
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        messageLabel.setText("");
    }

    /**
     * Gère le clic sur le bouton "Se connecter".
     */
    @FXML
    private void handleLogin() {
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (email.isEmpty()) {
            showError("Veuillez entrer votre email.");
            return;
        }
        if (password.isEmpty()) {
            showError("Veuillez entrer votre mot de passe.");
            return;
        }

        try {
            // Try normal login (email + password)
            Utilisateur utilisateur = utilisateurService.login(email, password);

            if (utilisateur == null) {
                // Try email-only to see if the account exists -> differentiate messages
                Utilisateur byEmail = utilisateurService.login(email);
                if (byEmail != null) {
                    showError("Mot de passe incorrect.");
                } else {
                    showError("Email non trouvé.");
                }
                return;
            }

            Session.setUtilisateur(utilisateur);
            System.out.println("[Login] Utilisateur connecté: " + utilisateur);
            showSuccess("Connexion réussie! Bienvenue " + utilisateur.getPrenom());

            // Redirection selon le rôle
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
            var resource = getClass().getResource("/views/admin_dashboard.fxml");
            if (resource == null) {
                showError("Fichier admin_dashboard.fxml non trouvé");
                return;
            }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/styles/colors.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
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
            // Prefer user_dashboard.fxml if exists (some views name variant)
            String[] candidates = {"/views/user_dashboard.fxml", "/views/portefeuille_dashboard.fxml"};
            java.net.URL resource = null;
            for (String path : candidates) {
                resource = getClass().getResource(path);
                if (resource != null) {
                    System.out.println("[Login] Using user view: " + path);
                    break;
                }
            }
            if (resource == null) {
                showError("Aucune vue utilisateur trouvée (user_dashboard.fxml ou portefeuille_dashboard.fxml manquante)");
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 800);
            var css1 = getClass().getResource("/styles/colors.css");
            var css2 = getClass().getResource("/styles/dashboard.css");
            if (css1 != null) scene.getStylesheets().add(css1.toExternalForm());
            if (css2 != null) scene.getStylesheets().add(css2.toExternalForm());
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
