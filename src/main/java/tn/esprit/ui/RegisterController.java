package tn.esprit.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import tn.esprit.db.Db;
import tn.esprit.domain.Admin;
import tn.esprit.domain.Client;
import tn.esprit.domain.Role;
import tn.esprit.domain.User;
import tn.esprit.repository.JdbcAdminDao;
import tn.esprit.repository.JdbcClientDao;
import tn.esprit.repository.JdbcUserDao;

import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class RegisterController implements Initializable {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    // CIN: uniquement chiffres, 6 à 20 (adaptable)
    private static final Pattern CIN_PATTERN = Pattern.compile("^[0-9]{6,20}$");
    // Tel: autorise +, chiffres, espaces, -, (), longueur 7..25
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9][0-9()\\s-]{5,23}[0-9]$");
    // Admin code: ex ADM-001 (lettres/chiffres/ - _), 3..40
    private static final Pattern ADMIN_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{3,40}$");

    @FXML private ComboBox<Role> roleCombo;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField fullNameField;

    @FXML private Label adminCodeLabel;
    @FXML private TextField adminCodeField;

    @FXML private Label cinLabel;
    @FXML private TextField cinField;

    @FXML private Label phoneLabel;
    @FXML private TextField phoneField;

    @FXML private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleCombo.getItems().setAll(Role.ADMIN, Role.CLIENT);
        roleCombo.setValue(Role.CLIENT);

        roleCombo.valueProperty().addListener((obs, oldV, newV) -> updateRoleFields(newV));
        updateRoleFields(Role.CLIENT);

        // Nettoyer le style d'erreur quand l'utilisateur retape
        installClearErrorOnType(emailField);
        installClearErrorOnType(passwordField);
        installClearErrorOnType(fullNameField);
        installClearErrorOnType(adminCodeField);
        installClearErrorOnType(cinField);
        installClearErrorOnType(phoneField);
    }

    private void updateRoleFields(Role role) {
        Role r = role == null ? Role.CLIENT : role;
        boolean isAdmin = r == Role.ADMIN;

        setVisibleManaged(adminCodeLabel, isAdmin);
        setVisibleManaged(adminCodeField, isAdmin);

        setVisibleManaged(cinLabel, !isAdmin);
        setVisibleManaged(cinField, !isAdmin);
        setVisibleManaged(phoneLabel, !isAdmin);
        setVisibleManaged(phoneField, !isAdmin);

        // Nettoyage des champs non pertinents
        if (isAdmin) {
            cinField.clear();
            phoneField.clear();
        } else {
            adminCodeField.clear();
        }

        clearErrors();
        statusLabel.setText("");
    }

    private static void setVisibleManaged(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private static void installClearErrorOnType(TextInputControl ctrl) {
        ctrl.textProperty().addListener((obs, o, n) -> ctrl.setStyle(""));
    }

    private void markError(Control c) {
        c.setStyle("-fx-border-color: #d32f2f; -fx-border-width: 1; -fx-background-insets: 0;");
    }

    private void clearErrors() {
        emailField.setStyle("");
        passwordField.setStyle("");
        fullNameField.setStyle("");
        adminCodeField.setStyle("");
        cinField.setStyle("");
        phoneField.setStyle("");
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    @FXML
    public void onCreateAccount(ActionEvent e) {
        statusLabel.setText("");
        clearErrors();

        Role role = roleCombo.getValue() == null ? Role.CLIENT : roleCombo.getValue();

        String email = safeTrim(emailField.getText());
        String pwd = passwordField.getText() == null ? "" : passwordField.getText();
        String fullName = safeTrim(fullNameField.getText());

        String adminCode = safeTrim(adminCodeField.getText());
        String cin = safeTrim(cinField.getText());
        String phone = safeTrim(phoneField.getText());

        // --- Validation ---
        boolean ok = true;

        if (email.isBlank()) {
            ok = false;
            markError(emailField);
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            ok = false;
            markError(emailField);
            statusLabel.setText("Email invalide (ex: nom@domaine.com)");
        }

        if (pwd.isBlank()) {
            ok = false;
            markError(passwordField);
        } else if (pwd.length() < 6) {
            ok = false;
            markError(passwordField);
            if (statusLabel.getText().isBlank()) statusLabel.setText("Mot de passe: minimum 6 caractères.");
        }

        if (fullName.isBlank()) {
            ok = false;
            markError(fullNameField);
        }

        if (role == Role.ADMIN) {
            // Admin code obligatoire + format
            if (adminCode.isBlank()) {
                ok = false;
                markError(adminCodeField);
                if (statusLabel.getText().isBlank()) statusLabel.setText("Admin code est obligatoire pour un admin.");
            } else if (!ADMIN_CODE_PATTERN.matcher(adminCode).matches()) {
                ok = false;
                markError(adminCodeField);
                if (statusLabel.getText().isBlank()) statusLabel.setText("Admin code invalide (3-40 caractères: lettres/chiffres/_/-)");
            }
        } else {
            // CIN obligatoire + format
            if (cin.isBlank()) {
                ok = false;
                markError(cinField);
                if (statusLabel.getText().isBlank()) statusLabel.setText("CIN est obligatoire pour un client.");
            } else if (!CIN_PATTERN.matcher(cin).matches()) {
                ok = false;
                markError(cinField);
                if (statusLabel.getText().isBlank()) statusLabel.setText("CIN invalide (chiffres uniquement, 6 à 20).");
            }

            // Téléphone optionnel, mais si rempli on valide le format
            if (!phone.isBlank() && !PHONE_PATTERN.matcher(phone).matches()) {
                ok = false;
                markError(phoneField);
                if (statusLabel.getText().isBlank()) statusLabel.setText("Téléphone invalide (ex: +216 20 000 000)");
            }
        }

        if (!ok) {
            if (statusLabel.getText().isBlank()) {
                statusLabel.setText("Vérifie les champs en rouge.");
            }
            return;
        }

        // NOTE: pour le moment on stocke un "hash" simple.
        // À améliorer plus tard avec BCrypt.
        String passwordHash = "hash:" + pwd;

        try (Connection cn = Db.getConnection()) {
            JdbcUserDao userDao = new JdbcUserDao(cn);
            JdbcClientDao clientDao = new JdbcClientDao(cn);
            JdbcAdminDao adminDao = new JdbcAdminDao(cn);

            if (userDao.findByEmail(email).isPresent()) {
                markError(emailField);
                statusLabel.setText("Cet email existe déjà.");
                return;
            }

            User u = new User(null, email, passwordHash, fullName, role, true);
            long userId = userDao.insert(u);
            u.setId(userId);

            if (role == Role.ADMIN) {
                adminDao.insert(new Admin(userId, adminCode));
            } else {
                clientDao.insert(new Client(userId, cin, phone.isBlank() ? null : phone));
            }

            // Auto-login + redirection accueil
            AppState.setCurrentUser(u);
            if (role == Role.ADMIN) {
                SceneNavigator.goHomeAdmin();
            } else {
                SceneNavigator.goHome();
            }
        } catch (Exception ex) {
            statusLabel.setText("Erreur: " + ex.getMessage());
        }
    }

    @FXML
    public void onGoLogin(ActionEvent e) {
        SceneNavigator.goLogin();
    }

    @FXML
    public void onClear(ActionEvent e) {
        clearForm();
        statusLabel.setText("");
    }

    private void clearForm() {
        roleCombo.setValue(Role.CLIENT);
        emailField.clear();
        passwordField.clear();
        fullNameField.clear();
        adminCodeField.clear();
        cinField.clear();
        phoneField.clear();
        clearErrors();
        updateRoleFields(Role.CLIENT);
    }
}
