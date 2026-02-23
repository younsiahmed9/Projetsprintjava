package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import utils.Db;
import Models.Admin;
import Models.Client;
import Models.Role;
import Models.User;
import Services.BiometricAuthService;
import Services.JdbcAdminDao;
import Services.JdbcClientDao;
import Services.JdbcUserDao;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
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

    @FXML private Button fingerprintBtn;
    @FXML private Label fingerprintStatusLabel;

    @FXML private Button faceBtn;
    @FXML private Label faceStatusLabel;
    @FXML private Button faceTopBtn;

    @FXML private Label statusLabel;

    private byte[] capturedFingerprint = null;
    private byte[] capturedFace = null;
    private List<org.bytedeco.opencv.opencv_core.Mat> capturedFaceMats = null; // in-memory mats captured from camera

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

        // Vérifier la disponibilité du lecteur d'empreintes
        BiometricAuthService bioService = BiometricAuthService.getInstance();
        if (fingerprintBtn != null) {
            fingerprintBtn.setDisable(!bioService.isAvailable());
            if (!bioService.isAvailable()) {
                if (fingerprintStatusLabel != null) {
                    fingerprintStatusLabel.setText("Lecteur d'empreintes non disponible");
                    fingerprintStatusLabel.setStyle("-fx-text-fill: #999;");
                }
            }
        }

        // Forcer visibilité et style du bouton Face ID pour debug / garantir l'affichage
        if (faceBtn != null) {
            faceBtn.setVisible(true);
            faceBtn.setManaged(true);
            faceBtn.setDisable(false);
            // donner un style visible pour repérer le bouton (à retirer en prod)
            faceBtn.setStyle("-fx-border-color: #f78f34; -fx-border-width: 2; -fx-background-color: transparent; -fx-text-fill: #f78f34; -fx-font-weight: 700;");
            if (faceStatusLabel != null && (faceStatusLabel.getText() == null || faceStatusLabel.getText().isBlank())) {
                faceStatusLabel.setText("");
            }
            System.out.println("[RegisterController] faceBtn injected and forced visible");
        } else {
            System.out.println("[RegisterController] faceBtn NOT injected (null)");
        }

        // Forcer style/visibilité du bouton top (nouveau)
        if (faceTopBtn != null) {
            faceTopBtn.setVisible(true);
            faceTopBtn.setManaged(true);
            faceTopBtn.setDisable(false);
            faceTopBtn.setStyle("-fx-background-color: #f78f34; -fx-text-fill: white; -fx-font-weight: 700;");
            System.out.println("[RegisterController] faceTopBtn injected and styled");
        } else {
            System.out.println("[RegisterController] faceTopBtn NOT injected (null)");
        }
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

            // Ajouter l'empreinte si elle a été capturée
            if (capturedFingerprint != null) {
                u.setFingerprintTemplate(capturedFingerprint);
            }
            // Ajout du modèle facial (thumbnail bytes) sera fait après entraînement

            long userId = userDao.insert(u);
            u.setId(userId);

            // Si on a des mats capturés depuis la caméra, entraîner et sauvegarder le modèle LBPH
            if (capturedFaceMats != null && !capturedFaceMats.isEmpty()) {
                try {
                    Path modelDir = Path.of("data/face_models");
                    Files.createDirectories(modelDir);
                    Path modelPath = modelDir.resolve("face_" + userId + ".yml");

                    Services.FaceRecognitionService frs = new Services.FaceRecognitionService();
                    frs.trainAndSave(capturedFaceMats, modelPath);

                    // aussi générer un thumbnail (png bytes) à partir de la première image
                    byte[] thumb = frs.matToPngBytes(capturedFaceMats.get(0));
                    u.setFaceTemplate(thumb);

                    // mettre à jour l'utilisateur avec le thumbnail
                    userDao.update(u);
                } catch (Exception ex) {
                    System.err.println("Erreur lors de l'entraînement Face ID: " + ex.getMessage());
                }
            } else if (capturedFace != null) {
                // fallback: user selected an image via FileChooser previously
                u.setFaceTemplate(capturedFace);
                userDao.update(u);
            }

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
    public void onCaptureFingerprint(ActionEvent e) {
        BiometricAuthService bioService = BiometricAuthService.getInstance();

        // Vérifier d'abord si le lecteur est disponible
        if (!bioService.isAvailable()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lecteur d'empreintes non disponible");
            alert.setHeaderText("Impossible de capturer l'empreinte");
            alert.setContentText("Le lecteur d'empreintes n'est pas disponible.\n\n" +
                    "Veuillez vérifier :\n" +
                    "• Windows Hello est configuré\n" +
                    "• Le service Windows Biometric est démarré\n" +
                    "• Les drivers du lecteur HP ProBook sont installés");
            alert.showAndWait();

            if (fingerprintStatusLabel != null) {
                fingerprintStatusLabel.setText("❌ Lecteur non disponible");
                fingerprintStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
            }
            return;
        }

        // Afficher une fenêtre de dialogue pour guider l'utilisateur
        Alert scanAlert = new Alert(Alert.AlertType.INFORMATION);
        scanAlert.setTitle("Scan de l'empreinte digitale");
        scanAlert.setHeaderText("📱 Préparation du scan");
        scanAlert.setContentText("Dans quelques secondes, vous devrez poser votre doigt\n" +
                "sur le capteur d'empreintes.\n\n" +
                "Préparez votre doigt et cliquez sur OK...");

        // Afficher la fenêtre et attendre que l'utilisateur clique sur OK
        var result = scanAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Démarrer un compte à rebours avant de scanner
            javafx.application.Platform.runLater(() -> {
                if (fingerprintStatusLabel != null) {
                    fingerprintStatusLabel.setText("⏳ Préparation... Posez votre doigt dans 3 secondes");
                    fingerprintStatusLabel.setStyle("-fx-text-fill: #FF6B35;");
                }
            });

            // Thread pour le compte à rebours et le scan
            new Thread(() -> {
                try {
                    // Compte à rebours 3, 2, 1...
                    for (int i = 3; i > 0; i--) {
                        final int count = i;
                        javafx.application.Platform.runLater(() -> {
                            if (fingerprintStatusLabel != null) {
                                fingerprintStatusLabel.setText("⏳ Posez votre doigt maintenant... " + count);
                                fingerprintStatusLabel.setStyle("-fx-text-fill: #FF6B35; -fx-font-weight: bold;");
                            }
                        });
                        Thread.sleep(1000); // 1 seconde
                    }

                    // Message final avant le scan
                    javafx.application.Platform.runLater(() -> {
                        if (fingerprintStatusLabel != null) {
                            fingerprintStatusLabel.setText("🔍 SCAN EN COURS... Maintenez votre doigt !");
                            fingerprintStatusLabel.setStyle("-fx-text-fill: #FF6B35; -fx-font-weight: bold; -fx-font-size: 13px;");
                        }
                    });

                    // Attendre 1 seconde de plus pour que l'utilisateur positionne bien son doigt
                    Thread.sleep(1000);

                    // Maintenant capturer l'empreinte
                    byte[] fingerprint = bioService.captureFingerprint();

                    // Succès - Afficher dans le thread JavaFX
                    javafx.application.Platform.runLater(() -> {
                        capturedFingerprint = fingerprint;

                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Succès !");
                        successAlert.setHeaderText("✅ Empreinte enregistrée avec succès");
                        successAlert.setContentText("Votre empreinte digitale a été capturée et enregistrée.\n\n" +
                                "Vous pourrez l'utiliser pour vous connecter rapidement\n" +
                                "après la création de votre compte.");
                        successAlert.showAndWait();

                        if (fingerprintStatusLabel != null) {
                            fingerprintStatusLabel.setText("✓ Empreinte enregistrée avec succès");
                            fingerprintStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                        }
                        if (fingerprintBtn != null) {
                            fingerprintBtn.setText("🔄 Réenregistrer empreinte");
                        }
                    });

                } catch (BiometricAuthService.BiometricException ex) {
                    // Erreur biométrique
                    javafx.application.Platform.runLater(() -> {
                        capturedFingerprint = null;

                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Erreur de scan");
                        errorAlert.setHeaderText("❌ Impossible de capturer l'empreinte");
                        errorAlert.setContentText("Erreur : " + ex.getMessage() + "\n\n" +
                                "Suggestions :\n" +
                                "• Assurez-vous que votre doigt est bien positionné\n" +
                                "• Nettoyez le lecteur d'empreintes\n" +
                                "• Essayez avec un autre doigt\n" +
                                "• Vérifiez que Windows Hello est configuré");
                        errorAlert.showAndWait();

                        if (fingerprintStatusLabel != null) {
                            fingerprintStatusLabel.setText("❌ " + ex.getMessage());
                            fingerprintStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
                        }
                    });
                } catch (InterruptedException ex) {
                    // Interruption du thread
                    javafx.application.Platform.runLater(() -> {
                        if (fingerprintStatusLabel != null) {
                            fingerprintStatusLabel.setText("❌ Scan annulé");
                            fingerprintStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
                        }
                    });
                }
            }).start();
        }
    }

    @FXML
    public void onCaptureFace(ActionEvent e) {
        // If OpenCV available, open camera preview and capture multiple face images
        try {
            if (Services.FaceRecognitionService.isOpenCvAvailable()) {
                faceStatusLabel.setText("⏳ Ouverture caméra pour capture du visage...");
                faceStatusLabel.setStyle("-fx-text-fill: #FF6B35;");

                Services.FaceRecognitionService frs = new Services.FaceRecognitionService();
                // capture 8 images within 15 seconds with preview
                List<org.bytedeco.opencv.opencv_core.Mat> mats = frs.captureFacesFromCamera(8, 15000, true);
                if (mats == null || mats.isEmpty()) {
                    faceStatusLabel.setText("❌ Aucun visage détecté. Réessayez.");
                    faceStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
                    return;
                }
                capturedFaceMats = mats;
                faceStatusLabel.setText("✓ Visages capturés: " + mats.size());
                faceStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                if (faceBtn != null) faceBtn.setText("🔄 Réenregistrer visage");
            } else {
                // fallback to file chooser (existing behavior)
                javafx.application.Platform.runLater(() -> {
                    if (faceStatusLabel != null) {
                        faceStatusLabel.setText("📷 Choisissez une image pour enregistrer votre visage");
                        faceStatusLabel.setStyle("-fx-text-fill: #FF6B35;");
                    }
                });

                javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
                chooser.setTitle("Sélectionnez une image de visage");
                chooser.getExtensionFilters().addAll(
                        new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
                );

                java.io.File file = chooser.showOpenDialog(faceBtn.getScene() == null ? null : faceBtn.getScene().getWindow());
                if (file == null) {
                    // Annulé
                    return;
                }

                byte[] imageBytes = java.nio.file.Files.readAllBytes(file.toPath());
                // store raw image bytes as fallback
                capturedFace = imageBytes;

                javafx.application.Platform.runLater(() -> {
                    if (faceStatusLabel != null) {
                        faceStatusLabel.setText("✓ Visage enregistré (image chargée: " + file.getName() + ")");
                        faceStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    }
                    if (faceBtn != null) {
                        faceBtn.setText("🔄 Réenregistrer visage");
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                faceStatusLabel.setText("❌ Erreur capture caméra: " + ex.getMessage());
                faceStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
            });
        }
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

        // Réinitialiser l'empreinte
        capturedFingerprint = null;
        if (fingerprintStatusLabel != null) {
            fingerprintStatusLabel.setText("");
        }
        if (fingerprintBtn != null) {
            fingerprintBtn.setText("📱 Enregistrer empreinte");
        }

        // Réinitialiser le visage
        capturedFace = null;
        if (faceStatusLabel != null) {
            faceStatusLabel.setText("");
        }
        if (faceBtn != null) {
            faceBtn.setText("📷 Enregistrer visage");
        }
    }
}
