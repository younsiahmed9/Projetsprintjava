package Controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import utils.Db;
import Models.User;
import Services.JdbcUserDao;
import Models.Role;
import javafx.scene.control.ComboBox;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import javafx.stage.FileChooser;
import javafx.scene.Parent;

import java.util.Optional;
import java.util.prefs.Preferences;

public class HomeController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private TextField profileFullNameField;
    @FXML
    private TextField profileEmailField;
    @FXML
    private PasswordField profileNewPasswordField;
    @FXML
    private PasswordField profileConfirmPasswordField;
    @FXML
    private Label profileStatusLabel;

    @FXML
    private ImageView profileAvatarView;
    @FXML
    private Label profilePhotoHintLabel;

    @FXML
    private javafx.scene.control.Button profileFingerprintBtn;
    @FXML
    private Label profileFingerprintStatusLabel;

    @FXML
    private ImageView headerAvatarView;
    @FXML
    private VBox editProfileCard;
    @FXML
    private ScrollPane editProfileScroll;
    @FXML
    private javafx.scene.layout.StackPane editProfileBackdrop;

    @FXML
    private VBox adminMenuSection;
    @FXML
    private VBox transactionsSection;
    @FXML
    private VBox transactionsList;

    private String profilePhotoPath; // chemin local ou uri (persisté en DB)

    // Désactivation de la logique de thèmes dynamiques : on n'utilisera qu'un seul
    // fichier CSS fourni par le repo
    private static final String THEME_ORANGE = "/css/theme-orange.css";
    private static final String THEME_LOGO = "/css/theme-logo.css";
    private static final String PREF_THEME_KEY = "fintrack.theme";

    private final Preferences prefs = Preferences.userNodeForPackage(HomeController.class);

    @FXML
    public void initialize() {

        refreshHeader();

        // Appliquer le thème sauvegardé
        Platform.runLater(() -> {
            if (welcomeLabel != null && welcomeLabel.getScene() != null) {
                String savedTheme = prefs.get(PREF_THEME_KEY, THEME_ORANGE);
                // Toujours s'assurer que app.css est présent
                if (!welcomeLabel.getScene().getStylesheets()
                        .contains(getClass().getResource("/css/app.css").toExternalForm())) {
                    welcomeLabel.getScene().getStylesheets()
                            .add(getClass().getResource("/css/app.css").toExternalForm());
                }

                // Retirer les deux thèmes possibles d'abord
                welcomeLabel.getScene().getStylesheets().remove(getClass().getResource(THEME_ORANGE).toExternalForm());
                welcomeLabel.getScene().getStylesheets().remove(getClass().getResource(THEME_LOGO).toExternalForm());

                // Ajouter le thème sauvegardé
                welcomeLabel.getScene().getStylesheets().add(getClass().getResource(savedTheme).toExternalForm());
            }
        });

        // caché par défaut (au cas où)
        showEditProfileOverlay(false);

        loadProfileFormFromState();
        loadProfilePhotoFromState();
    }

    private void refreshHeader() {
        User u = AppState.getCurrentUser();
        if (u == null) {
            welcomeLabel.setText("Bienvenue");
            if (adminMenuSection != null) {
                adminMenuSection.setVisible(false);
                adminMenuSection.setManaged(false);
            }
        } else {
            welcomeLabel.setText(
                    "Bienvenue " + (u.getFullName() == null ? "" : u.getFullName()) + " (" + u.getRole() + ")");

            // Afficher le menu admin si l'utilisateur est admin
            if (adminMenuSection != null) {
                boolean isAdmin = u.getRole() == Role.ADMIN;
                adminMenuSection.setVisible(isAdmin);
                adminMenuSection.setManaged(isAdmin);
            }
        }
    }

    private void loadProfileFormFromState() {
        User u = AppState.getCurrentUser();
        if (u == null) {
            if (profileStatusLabel != null) {
                profileStatusLabel.setText("Connecte-toi pour modifier ton profil.");
            }
            return;
        }
        if (profileFullNameField != null)
            profileFullNameField.setText(u.getFullName() == null ? "" : u.getFullName());
        if (profileEmailField != null)
            profileEmailField.setText(u.getEmail() == null ? "" : u.getEmail());
        if (profileNewPasswordField != null)
            profileNewPasswordField.clear();
        if (profileConfirmPasswordField != null)
            profileConfirmPasswordField.clear();
        if (profileStatusLabel != null)
            profileStatusLabel.setText("");
    }

    private void loadProfilePhotoFromState() {
        User u = AppState.getCurrentUser();
        if (u == null) {
            setAvatarDefault();
            setHeaderAvatarDefault();
            return;
        }
        // priorité: valeur DB (u.getProfilePhoto())
        profilePhotoPath = u.getProfilePhoto();
        if (profilePhotoPath == null || profilePhotoPath.isBlank()) {
            setAvatarDefault();
            setHeaderAvatarDefault();
        } else {
            setAvatarImage(profilePhotoPath);
            setHeaderAvatarImage(profilePhotoPath);
        }
    }

    private void setHeaderAvatarDefault() {
        if (headerAvatarView == null)
            return;
        URL res = getClass().getResource("/assets/logo.png");
        if (res == null) {
            headerAvatarView.setImage(null);
            return;
        }
        headerAvatarView.setImage(new Image(res.toExternalForm(), true));
    }

    private void setHeaderAvatarImage(String pathOrUri) {
        if (headerAvatarView == null)
            return;
        try {
            String uri = pathOrUri;
            if (!uri.startsWith("file:") && !uri.startsWith("jar:") && !uri.startsWith("http")) {
                uri = new File(uri).toURI().toString();
            }
            headerAvatarView.setImage(new Image(uri, true));
        } catch (Exception ex) {
            setHeaderAvatarDefault();
        }
    }

    @FXML
    public void onHeaderAvatarClick(MouseEvent e) {
        // Afficher le formulaire uniquement quand on clique sur l'avatar
        showEditProfileOverlay(true);

        if (profileFullNameField != null) {
            Platform.runLater(profileFullNameField::requestFocus);
        }
    }

    @FXML
    public void onCloseEditProfileBackdrop(MouseEvent e) {
        // Clic en dehors => ferme
        showEditProfileOverlay(false);
    }

    /**
     * Compatibilité: ancien handler référencé par erreur dans le FXML.
     */
    @FXML
    public void onEditProfileBackdropClick(MouseEvent e) {
        onCloseEditProfileBackdrop(e);
    }

    @FXML
    public void onEditProfileCardClick(MouseEvent e) {
        // Empêche le clic dans la carte de fermer via le backdrop
        e.consume();
    }

    @FXML
    public void onCloseEditProfile(ActionEvent e) {
        showEditProfileOverlay(false);
    }

    @FXML
    public void onProfileCaptureFingerprint(ActionEvent e) {
        Services.BiometricAuthService bioService = Services.BiometricAuthService.getInstance();

        // Vérifier si le lecteur est disponible
        if (!bioService.isAvailable()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Lecteur d'empreintes non disponible");
            alert.setHeaderText("Impossible de capturer l'empreinte");
            alert.setContentText("Le lecteur d'empreintes n'est pas disponible.\n\n" +
                    "Veuillez vérifier :\n" +
                    "• Windows Hello est configuré\n" +
                    "• Le service Windows Biometric est démarré\n" +
                    "• Les drivers du lecteur HP ProBook sont installés");
            alert.showAndWait();

            if (profileFingerprintStatusLabel != null) {
                profileFingerprintStatusLabel.setText("❌ Lecteur non disponible");
                profileFingerprintStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
            }
            return;
        }

        // Afficher la popup de préparation
        javafx.scene.control.Alert scanAlert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        scanAlert.setTitle("Scan de l'empreinte digitale");
        scanAlert.setHeaderText("📱 Préparation du scan");
        scanAlert.setContentText("Dans quelques secondes, vous devrez poser votre doigt\n" +
                "sur le capteur d'empreintes.\n\n" +
                "Préparez votre doigt et cliquez sur OK...");

        var result = scanAlert.showAndWait();

        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            // Démarrer le compte à rebours dans un thread séparé
            javafx.application.Platform.runLater(() -> {
                if (profileFingerprintStatusLabel != null) {
                    profileFingerprintStatusLabel.setText("⏳ Préparation... Posez votre doigt dans 3 secondes");
                    profileFingerprintStatusLabel.setStyle("-fx-text-fill: #FF6B35;");
                }
            });

            // Thread pour le compte à rebours et le scan
            new Thread(() -> {
                try {
                    // Compte à rebours 3, 2, 1...
                    for (int i = 3; i > 0; i--) {
                        final int count = i;
                        javafx.application.Platform.runLater(() -> {
                            if (profileFingerprintStatusLabel != null) {
                                profileFingerprintStatusLabel.setText("⏳ Posez votre doigt maintenant... " + count);
                                profileFingerprintStatusLabel
                                        .setStyle("-fx-text-fill: #FF6B35; -fx-font-weight: bold;");
                            }
                        });
                        Thread.sleep(1000);
                    }

                    // Message de scan en cours
                    javafx.application.Platform.runLater(() -> {
                        if (profileFingerprintStatusLabel != null) {
                            profileFingerprintStatusLabel.setText("🔍 SCAN EN COURS... Maintenez votre doigt !");
                            profileFingerprintStatusLabel
                                    .setStyle("-fx-text-fill: #FF6B35; -fx-font-weight: bold; -fx-font-size: 13px;");
                        }
                    });

                    Thread.sleep(1000);

                    // Capturer l'empreinte
                    byte[] fingerprint = bioService.captureFingerprint();

                    // Sauvegarder dans la base de données
                    User currentUser = AppState.getCurrentUser();
                    if (currentUser != null) {
                        try (Connection conn = Db.getConnection()) {
                            JdbcUserDao userDao = new JdbcUserDao(conn);

                            // ✅ VÉRIFICATION : L'empreinte est-elle déjà utilisée ?
                            Optional<User> existingUser = userDao.findByFingerprintHash(fingerprint,
                                    currentUser.getId());
                            if (existingUser.isPresent()) {
                                // L'empreinte est déjà associée à un autre compte !
                                User other = existingUser.get();
                                javafx.application.Platform.runLater(() -> {
                                    javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(
                                            javafx.scene.control.Alert.AlertType.WARNING);
                                    errorAlert.setTitle("Empreinte déjà utilisée");
                                    errorAlert.setHeaderText("⚠️ Cette empreinte est déjà associée à un autre compte");
                                    errorAlert
                                            .setContentText("Cette empreinte digitale est déjà enregistrée pour :\n\n" +
                                                    "• Email : " + other.getEmail() + "\n" +
                                                    "• Nom : " + other.getFullName() + "\n\n" +
                                                    "Veuillez utiliser un AUTRE DOIGT pour votre empreinte.");
                                    errorAlert.showAndWait();

                                    if (profileFingerprintStatusLabel != null) {
                                        profileFingerprintStatusLabel
                                                .setText("❌ Empreinte déjà associée. Changez de doigt.");
                                        profileFingerprintStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
                                    }
                                });
                                return; // ⛔ STOP - Ne pas enregistrer
                            }

                            // ✅ L'empreinte est unique, on peut l'enregistrer
                            currentUser.setFingerprintTemplate(fingerprint);
                            userDao.update(currentUser);

                            // Mettre à jour l'état global
                            AppState.setCurrentUser(currentUser);

                            // Succès !
                            javafx.application.Platform.runLater(() -> {
                                javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(
                                        javafx.scene.control.Alert.AlertType.INFORMATION);
                                successAlert.setTitle("Succès !");
                                successAlert.setHeaderText("✅ Empreinte enregistrée avec succès");
                                successAlert.setContentText("Votre empreinte digitale a été enregistrée.\n\n" +
                                        "Vous pouvez maintenant utiliser la connexion par empreinte !");
                                successAlert.showAndWait();

                                if (profileFingerprintStatusLabel != null) {
                                    profileFingerprintStatusLabel.setText("✓ Empreinte enregistrée avec succès");
                                    profileFingerprintStatusLabel
                                            .setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                                }
                                if (profileFingerprintBtn != null) {
                                    profileFingerprintBtn.setText("🔄 Réenregistrer empreinte");
                                }
                            });
                        } catch (SQLException ex) {
                            javafx.application.Platform.runLater(() -> {
                                javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(
                                        javafx.scene.control.Alert.AlertType.ERROR);
                                errorAlert.setTitle("Erreur de sauvegarde");
                                errorAlert.setHeaderText("❌ Impossible de sauvegarder l'empreinte");
                                errorAlert.setContentText("Erreur de base de données : " + ex.getMessage());
                                errorAlert.showAndWait();

                                if (profileFingerprintStatusLabel != null) {
                                    profileFingerprintStatusLabel.setText("❌ Erreur de sauvegarde");
                                    profileFingerprintStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
                                }
                            });
                        }
                    }

                } catch (Services.BiometricAuthService.BiometricException ex) {
                    // Erreur biométrique
                    javafx.application.Platform.runLater(() -> {
                        javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR);
                        errorAlert.setTitle("Erreur de scan");
                        errorAlert.setHeaderText("❌ Impossible de capturer l'empreinte");
                        errorAlert.setContentText("Erreur : " + ex.getMessage() + "\n\n" +
                                "Suggestions :\n" +
                                "• Assurez-vous que votre doigt est bien positionné\n" +
                                "• Nettoyez le lecteur d'empreintes\n" +
                                "• Essayez avec un autre doigt\n" +
                                "• Vérifiez que Windows Hello est configuré");
                        errorAlert.showAndWait();

                        if (profileFingerprintStatusLabel != null) {
                            profileFingerprintStatusLabel.setText("❌ " + ex.getMessage());
                            profileFingerprintStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
                        }
                    });
                } catch (InterruptedException ex) {
                    // Interruption du thread
                    javafx.application.Platform.runLater(() -> {
                        if (profileFingerprintStatusLabel != null) {
                            profileFingerprintStatusLabel.setText("❌ Scan annulé");
                            profileFingerprintStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
                        }
                    });
                }
            }).start();
        }
    }

    @FXML
    public void consumeEvent(MouseEvent e) {
        // Empêche le clic dans les overlays de fermer le backdrop
        e.consume();
    }

    private void showEditProfileOverlay(boolean show) {
        System.out.println("=== DEBUG: showEditProfileOverlay(" + show + ") ===");

        if (editProfileBackdrop != null) {
            editProfileBackdrop.setVisible(show);
            // Important: si visible=false mais managed=true, JavaFX peut laisser des effets
            // de layout/evt.
            editProfileBackdrop.setManaged(show);

            if (show) {
                editProfileBackdrop.toFront();
                System.out.println("✓ Backdrop Edit Profile affiché");
            }
        } else {
            System.out.println("✗ editProfileBackdrop est NULL");
        }

        if (editProfileCard != null) {
            editProfileCard.setVisible(show);
            editProfileCard.setManaged(show);

            if (show) {
                editProfileCard.toFront();
            }
            System.out.println("editProfileCard: " + editProfileCard);
            System.out.println("? editProfileCard.setVisible(" + show + ")");
        } else {
            System.out.println("✗ editProfileCard est NULL");
        }

        if (editProfileScroll != null) {
            // Le ScrollPane doit aussi suivre pour capter les events et afficher le
            // contenu.
            editProfileScroll.setVisible(show);
            editProfileScroll.setManaged(show);
            if (show) {
                editProfileScroll.toFront();
            }
        }
    }

    private void setAvatarDefault() {
        if (profileAvatarView == null)
            return;
        URL res = getClass().getResource("/assets/logo.png");
        if (res == null) {
            profileAvatarView.setImage(null);
            return;
        }
        Image img = new Image(res.toExternalForm(), true);
        profileAvatarView.setImage(img);
    }

    private void setAvatarImage(String pathOrUri) {
        if (profileAvatarView == null)
            return;
        try {
            String uri = pathOrUri;
            if (!uri.startsWith("file:") && !uri.startsWith("jar:") && !uri.startsWith("http")) {
                uri = new File(uri).toURI().toString();
            }
            profileAvatarView.setImage(new Image(uri, true));
        } catch (Exception ex) {
            setAvatarDefault();
            if (profilePhotoHintLabel != null) {
                profilePhotoHintLabel.setText("Impossible de charger l'image.");
            }
        }
    }

    @FXML
    public void onChooseProfilePhoto(ActionEvent e) {
        chooseProfilePhoto();
    }

    /**
     * Alias de compatibilité pour les anciennes versions du FXML qui appellent
     * `#onPickProfilePhoto`.
     */
    @FXML
    public void onPickProfilePhoto(ActionEvent e) {
        onChooseProfilePhoto(e);
    }

    @FXML
    public void onChooseProfilePhotoClick(MouseEvent e) {
        chooseProfilePhoto();
    }

    private void chooseProfilePhoto() {
        User u = AppState.getCurrentUser();
        if (u == null) {
            if (profileStatusLabel != null)
                profileStatusLabel.setText("Connecte-toi d'abord.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo de profil");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));

        File selected = chooser
                .showOpenDialog(profileAvatarView == null ? null : profileAvatarView.getScene().getWindow());
        if (selected == null)
            return;

        try {
            Path destDir = Path.of(System.getProperty("user.home"), ".fintrack", "profile-photos");
            Files.createDirectories(destDir);

            String ext = getExtensionLower(selected.getName());
            String fileName = "user-" + u.getId() + (ext.isEmpty() ? ".png" : ("." + ext));
            Path dest = destDir.resolve(fileName);

            Files.copy(selected.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            profilePhotoPath = dest.toString();
            // on met aussi à jour l'objet en mémoire (sera sauvegardé en DB lors de
            // Enregistrer)
            u.setProfilePhoto(profilePhotoPath);
            AppState.setCurrentUser(u);

            setAvatarImage(profilePhotoPath);
            setHeaderAvatarImage(profilePhotoPath);

            if (profilePhotoHintLabel != null) {
                profilePhotoHintLabel.setText("Photo sélectionnée. Clique sur 'Enregistrer' pour sauvegarder.");
            }
        } catch (IOException ex) {
            if (profileStatusLabel != null) {
                profileStatusLabel.setText("Erreur fichier: " + ex.getMessage());
            }
        }
    }

    @FXML
    public void onSaveProfile(ActionEvent e) {
        User current = AppState.getCurrentUser();
        if (current == null) {
            profileStatusLabel.setText("Aucun utilisateur connecté.");
            return;
        }

        String fullName = safeTrim(profileFullNameField.getText());
        String email = safeTrim(profileEmailField.getText());
        String newPwd = profileNewPasswordField.getText() == null ? "" : profileNewPasswordField.getText();
        String confirm = profileConfirmPasswordField.getText() == null ? "" : profileConfirmPasswordField.getText();

        if (fullName.isEmpty()) {
            profileStatusLabel.setText("Le nom complet est obligatoire.");
            return;
        }
        if (email.isEmpty() || !email.contains("@")) {
            profileStatusLabel.setText("Email invalide.");
            return;
        }

        boolean pwdChange = !newPwd.isBlank() || !confirm.isBlank();
        if (pwdChange) {
            if (newPwd.length() < 6) {
                profileStatusLabel.setText("Mot de passe trop court (min 6 caractères).");
                return;
            }
            if (!newPwd.equals(confirm)) {
                profileStatusLabel.setText("La confirmation du mot de passe ne correspond pas.");
                return;
            }
        }

        // Construire un User à mettre à jour (on garde role/active/id)
        User updated = new User();
        updated.setId(current.getId());
        updated.setRole(current.getRole());
        updated.setActive(current.isActive());
        updated.setFullName(fullName);
        updated.setEmail(email);
        updated.setPasswordHash(pwdChange ? Hashing.sha256(newPwd) : current.getPasswordHash());
        // Correction: utiliser profilePhotoPath qui est mis à jour par le FileChooser
        updated.setProfilePhoto(profilePhotoPath);

        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);
            boolean ok = dao.update(updated);
            if (!ok) {
                profileStatusLabel.setText("Aucune ligne mise à jour. Vérifie l'utilisateur.");
                return;
            }
            AppState.setCurrentUser(updated);
            refreshHeader();
            loadProfilePhotoFromState();
            profileNewPasswordField.clear();
            profileConfirmPasswordField.clear();
            profileStatusLabel.setText("Profil mis à jour.");
        } catch (SQLException ex) {
            profileStatusLabel.setText("Erreur DB: " + ex.getMessage());
        }
    }

    @FXML
    public void onResetProfile(ActionEvent e) {
        loadProfileFormFromState();
        loadProfilePhotoFromState();
    }

    @FXML
    public void onLogout(ActionEvent e) {
        AppState.clear();
        SceneNavigator.goLogin();
    }

    @FXML
    public void onOpenChat(ActionEvent e) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/chat_window.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Chat FInTrack");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setOnCloseRequest(event -> {
                ChatController controller = loader.getController();
                if (controller != null)
                    controller.stopPolling();
            });
            stage.show();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String getExtensionLower(String fileName) {
        if (fileName == null)
            return "";
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1)
            return "";
        return fileName.substring(dot + 1).toLowerCase();
    }

    @FXML
    public void onAdminSaveEditedUser(ActionEvent e) {
        // Cette méthode est vide car la logique est maintenant dans
        // AdminDashboardController
    }

    @FXML
    public void onAdminDeleteEditedUser(ActionEvent e) {
        // Cette méthode est vide car la logique est maintenant dans
        // AdminDashboardController
    }

    @FXML
    public void onCloseAdminEditUser(ActionEvent e) {
        // Fermer l'overlay d'édition utilisateur
        // Cette fonctionnalité est désactivée dans home.fxml (client view)
    }

    @FXML
    public void onAdminSaveUser(ActionEvent e) {
        // Sauvegarder l'utilisateur édité
        // Cette fonctionnalité est désactivée dans home.fxml (client view)
    }

    @FXML
    public void onAdminDeleteUser(ActionEvent e) {
        // Supprimer l'utilisateur
        // Cette fonctionnalité est désactivée dans home.fxml (client view)
    }

    @FXML
    public void onAdminEditUserBackdropClick(javafx.scene.input.MouseEvent e) {
        // Fermer l'overlay en cliquant sur le backdrop
        // Cette fonctionnalité est désactivée dans home.fxml (client view)
    }

    @FXML
    public void onToggleTheme(javafx.event.ActionEvent e) {
        if (welcomeLabel == null || welcomeLabel.getScene() == null)
            return;

        String currentTheme = prefs.get(PREF_THEME_KEY, THEME_ORANGE);
        String newTheme = THEME_ORANGE.equals(currentTheme) ? THEME_LOGO : THEME_ORANGE;

        // Appliquer le nouveau thème
        welcomeLabel.getScene().getStylesheets().remove(getClass().getResource(currentTheme).toExternalForm());
        welcomeLabel.getScene().getStylesheets().add(getClass().getResource(newTheme).toExternalForm());

        // Sauvegarder dans les préférences
        prefs.put(PREF_THEME_KEY, newTheme);
    }

    @FXML
    public void onOpenAiAssistant(javafx.event.ActionEvent e) {
        SceneNavigator.goAiAssistant();
    }
}
