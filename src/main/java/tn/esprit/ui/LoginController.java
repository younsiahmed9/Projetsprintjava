package tn.esprit.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tn.esprit.db.Db;
import tn.esprit.domain.User;
import tn.esprit.repository.JdbcUserDao;

import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField emailField;

    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private CheckBox showPasswordCheck;
    @FXML private Button togglePasswordBtn;

    @FXML private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Synchronisation bidirectionnelle
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        // Toggle show/hide
        showPasswordCheck.selectedProperty().addListener((obs, oldV, show) -> {
            passwordVisibleField.setVisible(show);
            passwordVisibleField.setManaged(show);
            passwordField.setVisible(!show);
            passwordField.setManaged(!show);
            if (togglePasswordBtn != null) {
                togglePasswordBtn.setText(show ? "🙈" : "👁");
            }
        });

        // état initial
        if (togglePasswordBtn != null) {
            togglePasswordBtn.setText("👁");
        }
    }

    @FXML
    public void onLogin(ActionEvent e) {
        statusLabel.setText("");

        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        // On récupère depuis passwordField (les deux champs sont synchronisés)
        String pwd = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isBlank() || pwd.isBlank()) {
            statusLabel.setText("Email et mot de passe sont obligatoires.");
            return;
        }

        String expectedHash = "hash:" + pwd;

        try (Connection cn = Db.getConnection()) {
            JdbcUserDao userDao = new JdbcUserDao(cn);
            User u = userDao.findByEmail(email).orElse(null);
            if (u == null) {
                statusLabel.setText("Compte introuvable.");
                return;
            }
            if (!expectedHash.equals(u.getPasswordHash())) {
                statusLabel.setText("Mot de passe incorrect.");
                return;
            }
            if (!u.isActive()) {
                statusLabel.setText("Compte désactivé.");
                return;
            }

            AppState.setCurrentUser(u);
            if (u.getRole() != null && u.getRole() == tn.esprit.domain.Role.ADMIN) {
                SceneNavigator.goHomeAdmin();
            } else {
                SceneNavigator.goHome();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Erreur: " + rootCauseMessage(ex));
        }
    }

    private static String rootCauseMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage() == null ? ex.getMessage() : root.getMessage();
    }

    @FXML
    public void onGoRegister(ActionEvent e) {
        SceneNavigator.goRegister();
    }

    @FXML
    public void onTogglePassword(ActionEvent e) {
        showPasswordCheck.setSelected(!showPasswordCheck.isSelected());
    }
}
