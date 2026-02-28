package Controllers;

import Models.Session;
import Models.Utilisateur;
import Services.NavigationService;
import Services.UtilisateurService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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

            // ✅ UTILISER NavigationService ICI
            if (utilisateur.isAdmin()) {
                NavigationService.navigateTo("/fxml/admin_dashboard.fxml", "Administration");
            } else {
                NavigationService.navigateTo("/fxml/client_dashboard.fxml", "Tableau de bord");
            }

        } catch (Exception e) {
            showError("Erreur de connexion: " + e.getMessage());
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