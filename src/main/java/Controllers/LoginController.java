package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import utils.Db;
import Models.User;
import Services.BiometricAuthService;
import Services.JdbcUserDao;

import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.Node;
import javafx.stage.Stage;

public class LoginController implements Initializable {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordVisibleField;
    @FXML
    private CheckBox showPasswordCheck;
    @FXML
    private Button togglePasswordBtn;

    @FXML
    private Button fingerprintLoginBtn;
    @FXML
    private Label fingerprintStatusLabel;

    @FXML
    private Button faceLoginBtn;
    @FXML
    private Label faceStatusLabel;

    @FXML
    private Label statusLabel;

    // Code restauré à l'état d'origine, sans aucune modification responsive ni
    // navigation
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        showPasswordCheck.selectedProperty().addListener((obs, oldV, show) -> {
            passwordVisibleField.setVisible(show);
            passwordVisibleField.setManaged(show);
            passwordField.setVisible(!show);
            passwordField.setManaged(!show);
            if (togglePasswordBtn != null) {
                togglePasswordBtn.setText(show ? "🙈" : "👁");
            }
        });
        if (togglePasswordBtn != null) {
            togglePasswordBtn.setText("👁");
        }
        BiometricAuthService bioService = BiometricAuthService.getInstance();
        if (fingerprintLoginBtn != null) {
            fingerprintLoginBtn.setDisable(!bioService.isAvailable());
            if (!bioService.isAvailable() && fingerprintStatusLabel != null) {
                fingerprintStatusLabel.setText("Lecteur d'empreintes non disponible");
                fingerprintStatusLabel.setStyle("-fx-text-fill: #999;");
            }
        }
        if (faceLoginBtn != null) {
            faceLoginBtn.setVisible(true);
            faceLoginBtn.setManaged(true);
            faceLoginBtn.setDisable(false);
            if (faceStatusLabel != null)
                faceStatusLabel.setText("");
            faceLoginBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-weight: 700;");
            System.out.println("[LoginController] faceLoginBtn injected and forced visible");
        } else {
            System.out.println("[LoginController] faceLoginBtn NOT injected (null)");
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
                statusLabel.setText("Votre email n'est pas vérifié. Veuillez vérifier votre boîte mail.");
                return;
            }
            if (u.isBanned()) {
                statusLabel.setText("Votre compte a été banni pour non-respect des règles.");
                return;
            }

            AppState.setCurrentUser(u);
            if (u.getRole() != null && u.getRole() == Models.Role.ADMIN) {
                SceneNavigator.goAdminDashboard();
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
    public void onFingerprintLogin(ActionEvent e) {
        statusLabel.setText("");

        BiometricAuthService bioService = BiometricAuthService.getInstance();

        // Vérifier d'abord si le lecteur est disponible
        if (!bioService.isAvailable()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lecteur d'empreintes non disponible");
            alert.setHeaderText("Impossible de se connecter par empreinte");
            alert.setContentText("Le lecteur d'empreintes n'est pas disponible.\n\n" +
                    "Veuillez utiliser la connexion classique avec email et mot de passe,\n" +
                    "ou vérifier que Windows Hello est correctement configuré.");
            alert.showAndWait();

            if (fingerprintStatusLabel != null) {
                fingerprintStatusLabel.setText("❌ Lecteur non disponible");
                fingerprintStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
            }
            return;
        }

        try (Connection cn = Db.getConnection()) {
            JdbcUserDao userDao = new JdbcUserDao(cn);

            // Récupérer tous les utilisateurs avec empreinte enregistrée
            List<User> allUsers = userDao.findAll();
            List<User> usersWithFingerprint = allUsers.stream()
                    .filter(u -> u.getFingerprintTemplate() != null && u.getFingerprintTemplate().length > 0)
                    .toList();

            if (usersWithFingerprint.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Aucune empreinte enregistrée");
                alert.setHeaderText("Aucun compte avec empreinte trouvé");
                alert.setContentText("Il n'y a aucun compte avec empreinte digitale enregistrée.\n\n" +
                        "Veuillez créer un compte et enregistrer votre empreinte\n" +
                        "lors de l'inscription pour pouvoir utiliser cette fonctionnalité.");
                alert.showAndWait();

                if (fingerprintStatusLabel != null) {
                    fingerprintStatusLabel.setText("❌ Aucune empreinte enregistrée");
                    fingerprintStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
                }
                statusLabel.setText("Veuillez créer un compte avec empreinte d'abord.");
                return;
            }

            // Afficher une fenêtre pour demander le scan
            Alert scanAlert = new Alert(Alert.AlertType.INFORMATION);
            scanAlert.setTitle("Authentification biométrique");
            scanAlert.setHeaderText("🔐 Préparation de l'authentification");
            scanAlert.setContentText("Dans quelques secondes, vous devrez poser votre doigt\n" +
                    "sur le capteur d'empreintes pour vous authentifier.\n\n" +
                    "Préparez votre doigt et cliquez sur OK...");

            var result = scanAlert.showAndWait();

            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return; // L'utilisateur a annulé
            }

            // Compte à rebours et scan dans un thread séparé
            new Thread(() -> {
                try {
                    // Compte à rebours
                    for (int i = 3; i > 0; i--) {
                        final int count = i;
                        javafx.application.Platform.runLater(() -> {
                            if (fingerprintStatusLabel != null) {
                                fingerprintStatusLabel.setText("⏳ Posez votre doigt maintenant... " + count);
                                fingerprintStatusLabel.setStyle("-fx-text-fill: #FF6B35; -fx-font-weight: bold;");
                            }
                        });
                        Thread.sleep(1000);
                    }

                    // Message de scan en cours
                    javafx.application.Platform.runLater(() -> {
                        if (fingerprintStatusLabel != null) {
                            fingerprintStatusLabel.setText("🔍 AUTHENTIFICATION EN COURS...");
                            fingerprintStatusLabel
                                    .setStyle("-fx-text-fill: #FF6B35; -fx-font-weight: bold; -fx-font-size: 13px;");
                        }
                    });

                    Thread.sleep(1000);

                    // Vérifier l'empreinte
                    boolean authenticated = false;
                    User authenticatedUser = null;

                    for (User user : usersWithFingerprint) {
                        try {
                            if (bioService.verifyFingerprint(user.getFingerprintTemplate())) {
                                authenticated = true;
                                authenticatedUser = user;
                                break;
                            }
                        } catch (BiometricAuthService.BiometricException ex) {
                            // Continuer avec le prochain utilisateur
                            continue;
                        }
                    }

                    // Traiter le résultat dans le thread JavaFX
                    final boolean finalAuthenticated = authenticated;
                    final User finalAuthenticatedUser = authenticatedUser;

                    javafx.application.Platform.runLater(() -> {
                        if (finalAuthenticated && finalAuthenticatedUser != null) {
                            if (!finalAuthenticatedUser.isActive()) {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Compte désactivé");
                                alert.setHeaderText("Accès refusé");
                                alert.setContentText("Votre compte a été désactivé.\n\n" +
                                        "Veuillez contacter l'administrateur pour plus d'informations.");
                                alert.showAndWait();

                                statusLabel.setText("Compte désactivé.");
                                if (fingerprintStatusLabel != null) {
                                    fingerprintStatusLabel.setText("");
                                }
                                return;
                            }

                            // Succès
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Authentification réussie");
                            successAlert.setHeaderText("✅ Bienvenue " + finalAuthenticatedUser.getFullName() + " !");
                            successAlert.setContentText("Vous êtes maintenant connecté.\n\nRedirection en cours...");
                            successAlert.show();

                            // Fermer automatiquement après 1.5 secondes
                            new Thread(() -> {
                                try {
                                    Thread.sleep(1500);
                                    javafx.application.Platform.runLater(() -> {
                                        successAlert.close();
                                    });
                                } catch (InterruptedException ex) {
                                    // Ignorer
                                }
                            }).start();

                            if (fingerprintStatusLabel != null) {
                                fingerprintStatusLabel.setText("✓ Authentification réussie");
                                fingerprintStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                            }

                            AppState.setCurrentUser(finalAuthenticatedUser);
                            if (finalAuthenticatedUser.getRole() != null
                                    && finalAuthenticatedUser.getRole() == Models.Role.ADMIN) {
                                SceneNavigator.goAdminDashboard();
                            } else {
                                SceneNavigator.goHome();
                            }
                        } else {
                            // Échec
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Empreinte non reconnue");
                            errorAlert.setHeaderText("❌ Authentification échouée");
                            errorAlert.setContentText("Votre empreinte digitale n'a pas été reconnue.\n\n" +
                                    "Suggestions :\n" +
                                    "• Assurez-vous d'utiliser le doigt enregistré\n" +
                                    "• Nettoyez le lecteur d'empreintes\n" +
                                    "• Utilisez la connexion classique avec email/mot de passe\n" +
                                    "• Réenregistrez votre empreinte si le problème persiste");
                            errorAlert.showAndWait();

                            if (fingerprintStatusLabel != null) {
                                fingerprintStatusLabel.setText("❌ Empreinte non reconnue");
                                fingerprintStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
                            }
                            statusLabel.setText("Empreinte non reconnue. Veuillez réessayer.");
                        }
                    });

                } catch (InterruptedException ex) {
                    javafx.application.Platform.runLater(() -> {
                        if (fingerprintStatusLabel != null) {
                            fingerprintStatusLabel.setText("❌ Scan annulé");
                            fingerprintStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
                        }
                    });
                }
            }).start();

        } catch (Exception ex) {
            ex.printStackTrace();

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText("Une erreur s'est produite");
            errorAlert.setContentText("Erreur : " + rootCauseMessage(ex) + "\n\n" +
                    "Veuillez réessayer ou utiliser la connexion classique.");
            errorAlert.showAndWait();

            statusLabel.setText("Erreur: " + rootCauseMessage(ex));
            if (fingerprintStatusLabel != null) {
                fingerprintStatusLabel.setText("");
            }
        }
    }

    @FXML
    public void onFaceLogin(ActionEvent e) {
        statusLabel.setText("");
        faceStatusLabel.setText("");
        try {
            if (!Services.FaceRecognitionService.isOpenCvAvailable()) {
                faceStatusLabel.setText("❌ OpenCV non disponible. Impossible d'utiliser la caméra.");
                faceStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
                return;
            }
            faceStatusLabel.setText("⏳ Ouvrir la caméra et capturer...");
            faceStatusLabel.setStyle("-fx-text-fill: #FF6B35;");
            Services.FaceRecognitionService frs = new Services.FaceRecognitionService();
            List<org.bytedeco.opencv.opencv_core.Mat> mats;
            try {
                mats = frs.captureFacesFromCamera(3, 10000, true);
            } catch (Exception ex) {
                faceStatusLabel.setText("❌ Erreur capture caméra: " + ex.getMessage());
                faceStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
                return;
            }
            if (mats == null || mats.isEmpty()) {
                faceStatusLabel.setText("❌ Aucun visage détecté. Réessayez.");
                faceStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
                return;
            }
            Path modelsDir = Path.of("data/face_models");
            if (!Files.exists(modelsDir)) {
                faceStatusLabel.setText("❌ Aucun modèle face enregistré.");
                faceStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
                return;
            }
            double bestConfidence = Double.MAX_VALUE;
            Long bestUserId = null;
            try (Connection cn = Db.getConnection()) {
                JdbcUserDao userDao = new JdbcUserDao(cn);
                try (var stream = Files.list(modelsDir)) {
                    for (Path p : (Iterable<Path>) stream::iterator) {
                        String name = p.getFileName().toString();
                        if (!name.startsWith("face_") || !name.endsWith(".yml"))
                            continue;
                        String idStr = name.substring(5, name.length() - 4);
                        Long uid;
                        try {
                            uid = Long.parseLong(idStr);
                        } catch (NumberFormatException ex) {
                            continue;
                        }
                        double minConf = Double.MAX_VALUE;
                        for (org.bytedeco.opencv.opencv_core.Mat m : mats) {
                            double conf = frs.verifyWithModel(p, m);
                            if (conf < minConf)
                                minConf = conf;
                        }
                        if (minConf < bestConfidence) {
                            bestConfidence = minConf;
                            bestUserId = uid;
                        }
                    }
                }
                double threshold = 60.0;
                if (bestUserId != null && bestConfidence <= threshold) {
                    var opt = userDao.findById(bestUserId);
                    if (opt.isPresent()) {
                        User matched = opt.get();
                        if (!matched.isActive()) {
                            faceStatusLabel.setText("❌ Compte désactivé.");
                            faceStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
                            return;
                        }
                        AppState.setCurrentUser(matched);
                        faceStatusLabel.setText(
                                "✓ Connexion Face ID réussie (score=" + String.format("%.2f", bestConfidence) + ")");
                        faceStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                        if (matched.getRole() != null && matched.getRole() == Models.Role.ADMIN) {
                            SceneNavigator.goAdminDashboard();
                        } else {
                            SceneNavigator.goHome();
                        }
                        return;
                    }
                }
                faceStatusLabel.setText("❌ Aucun modèle correspondant (best="
                        + (bestConfidence == Double.MAX_VALUE ? "n/a" : String.format("%.2f", bestConfidence)) + ")");
                faceStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
            } catch (Exception ex) {
                faceStatusLabel.setText("❌ Erreur Face ID: " + ex.getMessage());
                faceStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
            }
        } catch (Exception ex) {
            faceStatusLabel.setText("❌ Erreur Face ID: " + ex.getMessage());
            faceStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
        }
    }

    // Ajout des handlers pour les boutons de fenêtre
    @FXML
    private void onMinimize(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).setIconified(true);
    }

    @FXML
    private void onMaximize(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML
    private void onClose(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
}
