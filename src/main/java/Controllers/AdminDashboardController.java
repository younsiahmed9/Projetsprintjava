package Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import utils.Db;
import Models.Role;
import Models.User;
import Services.JdbcUserDao;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.prefs.Preferences;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private ImageView headerAvatarView;
    @FXML private VBox adminMenuSection;
    @FXML private VBox adminDashboardBox;
    @FXML private Label adminCountLabel;
    @FXML private Label clientCountLabel;
    @FXML private Label adminStatusLabel;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Long> idCol;
    @FXML private TableColumn<User, String> fullNameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, Role> roleCol;
    @FXML private TableColumn<User, Boolean> activeCol;
    @FXML private BarChart<String, Number> usersCreatedChart;
    @FXML private PieChart rolesPieChart;

    // Edit Profile Overlay
    @FXML private StackPane editProfileBackdrop;
    @FXML private ScrollPane editProfileScroll;
    @FXML private VBox editProfileCard;
    @FXML private ImageView profileAvatarView;
    @FXML private Label profilePhotoHintLabel;
    @FXML private javafx.scene.control.Button profileFingerprintBtn;
    @FXML private Label profileFingerprintStatusLabel;
    @FXML private TextField profileFullNameField;
    @FXML private TextField profileEmailField;
    @FXML private PasswordField profileNewPasswordField;
    @FXML private PasswordField profileConfirmPasswordField;
    @FXML private Label profileStatusLabel;

    // Admin Edit User Overlay
    @FXML private StackPane adminEditUserBackdrop;
    @FXML private VBox adminEditUserCard;
    @FXML private Label adminEditUserIdLabel;
    @FXML private TextField adminEditUserFullNameField;
    @FXML private TextField adminEditUserEmailField;
    @FXML private ComboBox<Role> adminEditUserRoleBox;
    @FXML private CheckBox adminEditUserActiveCheck;
    @FXML private Label adminEditUserStatusLabel;

    // Admin Add User Overlay
    @FXML private StackPane adminAddUserBackdrop;
    @FXML private VBox adminAddUserCard;
    @FXML private TextField adminAddUserFullNameField;
    @FXML private TextField adminAddUserEmailField;
    @FXML private PasswordField adminAddUserPasswordField;
    @FXML private ComboBox<Role> adminAddUserRoleBox;
    @FXML private CheckBox adminAddUserActiveCheck;
    @FXML private Label adminAddUserStatusLabel;

    private User adminSelectedUser;
    private String profilePhotoPath;
    private final ObservableList<User> usersItems = FXCollections.observableArrayList();
    private final Preferences prefs = Preferences.userNodeForPackage(HomeController.class);
    private static final String THEME_ORANGE = "/css/theme-orange.css";
    private static final String THEME_LOGO = "/css/theme-logo.css";
    private static final String PREF_THEME_KEY = "fintrack.theme";

    @FXML
    public void initialize() {
        refreshHeader();
        Platform.runLater(this::applySavedTheme);

        showEditProfileOverlay(false);
        loadProfileFormFromState();
        loadProfilePhotoFromState();

        setupAdminTable();
        updateAdminVisibilityAndLoad();

        if (adminEditUserRoleBox != null) {
            adminEditUserRoleBox.getItems().setAll(Role.ADMIN, Role.CLIENT);
        }
        showAdminEditUserOverlay(false, null);

        if (adminAddUserRoleBox != null) {
            adminAddUserRoleBox.getItems().setAll(Role.ADMIN, Role.CLIENT);
            adminAddUserRoleBox.setValue(Role.CLIENT);
        }
        showAdminAddUserOverlay(false);
    }

    private void refreshHeader() {
        User u = AppState.getCurrentUser();
        if (u != null) {
            welcomeLabel.setText("Bienvenue " + u.getFullName() + " (" + u.getRole() + ")");
            loadProfilePhotoFromState();
        }
    }

    @FXML
    public void onLogout(ActionEvent e) {
        AppState.clear();
        SceneNavigator.goLogin();
    }

    @FXML
    public void onToggleTheme(ActionEvent e) {
        Parent root = welcomeLabel.getScene().getRoot();
        if (root == null) return;
        String current = prefs.get(PREF_THEME_KEY, THEME_ORANGE);
        String next = THEME_ORANGE.equals(current) ? THEME_LOGO : THEME_ORANGE;
        prefs.put(PREF_THEME_KEY, next);
        applyThemeToRoot(root, next);
    }

    private void applySavedTheme() {
        Parent root = welcomeLabel.getScene().getRoot();
        if (root == null) return;
        String theme = prefs.get(PREF_THEME_KEY, THEME_ORANGE);
        applyThemeToRoot(root, theme);
    }

    private void applyThemeToRoot(Parent root, String themeResourcePath) {
        URL res = getClass().getResource(themeResourcePath);
        if (res == null) return;
        String uri = res.toExternalForm();
        root.getStylesheets().removeIf(s -> s.endsWith("theme-orange.css") || s.endsWith("theme-logo.css"));
        if (!root.getStylesheets().contains(uri)) {
            root.getStylesheets().add(uri);
        }
    }

    // --- Profile Methods ---
    @FXML
    public void onHeaderAvatarClick(MouseEvent e) {
        showEditProfileOverlay(true);
        Platform.runLater(() -> profileFullNameField.requestFocus());
    }

    @FXML
    public void onCloseEditProfile(ActionEvent e) { showEditProfileOverlay(false); }

    @FXML
    public void onCloseEditProfileBackdrop(MouseEvent e) { showEditProfileOverlay(false); }

    @FXML
    public void onEditProfileCardClick(MouseEvent e) { e.consume(); }

    private void showEditProfileOverlay(boolean show) {
        if (editProfileBackdrop != null) {
            editProfileBackdrop.setVisible(show);
            editProfileBackdrop.setManaged(show);
            if (show) editProfileBackdrop.toFront();
        }
    }

    private void loadProfileFormFromState() {
        User u = AppState.getCurrentUser();
        if (u == null) return;
        profileFullNameField.setText(u.getFullName());
        profileEmailField.setText(u.getEmail());
        profileNewPasswordField.clear();
        profileConfirmPasswordField.clear();
        profileStatusLabel.setText("");
    }

    private void loadProfilePhotoFromState() {
        User u = AppState.getCurrentUser();
        if (u == null) return;
        profilePhotoPath = u.getProfilePhoto();
        if (profilePhotoPath == null || profilePhotoPath.isBlank()) {
            setAvatarDefault(profileAvatarView);
            setAvatarDefault(headerAvatarView);
        } else {
            setAvatarImage(profileAvatarView, profilePhotoPath);
            setAvatarImage(headerAvatarView, profilePhotoPath);
        }
    }

    private void setAvatarDefault(ImageView avatarView) {
        if (avatarView == null) return;
        URL res = getClass().getResource("/assets/logo.png");
        if (res != null) avatarView.setImage(new Image(res.toExternalForm(), true));
    }

    private void setAvatarImage(ImageView avatarView, String pathOrUri) {
        if (avatarView == null) return;
        try {
            String uri = pathOrUri.startsWith("file:") ? pathOrUri : new File(pathOrUri).toURI().toString();
            avatarView.setImage(new Image(uri, true));
        } catch (Exception ex) {
            setAvatarDefault(avatarView);
        }
    }

    @FXML
    public void onChangeProfilePhoto(ActionEvent e) {
        User u = AppState.getCurrentUser();
        if (u == null) return;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo de profil");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selected = chooser.showOpenDialog(profileAvatarView.getScene().getWindow());
        if (selected == null) return;

        try {
            Path destDir = Path.of(System.getProperty("user.home"), ".fintrack", "profile-photos");
            Files.createDirectories(destDir);
            String ext = getExtensionLower(selected.getName());
            String fileName = "user-" + u.getId() + "." + ext;
            Path dest = destDir.resolve(fileName);
            Files.copy(selected.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            profilePhotoPath = dest.toString();
            u.setProfilePhoto(profilePhotoPath);
            setAvatarImage(profileAvatarView, profilePhotoPath);
            setAvatarImage(headerAvatarView, profilePhotoPath);
            profilePhotoHintLabel.setText("Photo sélectionnée. Enregistrez pour sauvegarder.");
        } catch (IOException ex) {
            profileStatusLabel.setText("Erreur fichier: " + ex.getMessage());
        }
    }

    @FXML
    public void onSaveProfile(ActionEvent e) {
        User current = AppState.getCurrentUser();
        if (current == null) return;

        String fullName = safeTrim(profileFullNameField.getText());
        String email = safeTrim(profileEmailField.getText());
        String newPwd = profileNewPasswordField.getText();
        String confirm = profileConfirmPasswordField.getText();

        if (fullName.isEmpty() || email.isEmpty() || !email.contains("@")) {
            profileStatusLabel.setText("Nom et email valides sont requis.");
            return;
        }
        if (!newPwd.isEmpty() && !newPwd.equals(confirm)) {
            profileStatusLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        User updated = new User();
        updated.setId(current.getId());
        updated.setFullName(fullName);
        updated.setEmail(email);
        updated.setProfilePhoto(profilePhotoPath);
        if (!newPwd.isEmpty()) {
            updated.setPasswordHash(Hashing.sha256(newPwd));
        } else {
            updated.setPasswordHash(current.getPasswordHash());
        }
        updated.setRole(current.getRole());
        updated.setActive(current.isActive());

        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);
            dao.update(updated);
            AppState.setCurrentUser(updated);
            refreshHeader();
            profileStatusLabel.setText("Profil mis à jour.");
        } catch (SQLException ex) {
            profileStatusLabel.setText("Erreur DB: " + ex.getMessage());
        }
    }

    // --- Admin User Management Methods ---
    private void setupAdminTable() {
        if (usersTable == null) return;
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        usersTable.setItems(usersItems);
        usersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    onAdminRowDoubleClick(row.getItem());
                }
            });
            return row;
        });
    }

    private void updateAdminVisibilityAndLoad() {
        User u = AppState.getCurrentUser();
        boolean isAdmin = u != null && u.getRole() == Role.ADMIN;
        if (adminDashboardBox != null) {
            adminDashboardBox.setManaged(isAdmin);
            adminDashboardBox.setVisible(isAdmin);
        }
        if (isAdmin) reloadAdminDashboard();
    }

    @FXML
    public void onAdminRefresh(ActionEvent e) { reloadAdminDashboard(); }

    private void reloadAdminDashboard() {
        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);
            long admins = dao.countByRole(Role.ADMIN);
            long clients = dao.countByRole(Role.CLIENT);
            adminCountLabel.setText(String.valueOf(admins));
            clientCountLabel.setText(String.valueOf(clients));
            usersItems.setAll(dao.findAll());
            loadUsersCreatedChart(dao);
            loadRolesPieChart(admins, clients);
        } catch (Exception ex) {
            adminStatusLabel.setText("Erreur: " + ex.getMessage());
        }
    }

    private void loadRolesPieChart(long admins, long clients) {
        if (rolesPieChart == null) return;
        rolesPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Admins", admins),
                new PieChart.Data("Clients", clients)
        ));
    }

    private void loadUsersCreatedChart(JdbcUserDao dao) throws SQLException {
        if (usersCreatedChart == null) return;
        var points = dao.countCreatedUsersLastDays(7);
        java.time.LocalDate start = java.time.LocalDate.now().minusDays(6);
        java.util.Map<java.time.LocalDate, Long> map = new java.util.HashMap<>();
        for (var p : points) map.put(p.date(), p.count());
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < 7; i++) {
            java.time.LocalDate d = start.plusDays(i);
            series.getData().add(new XYChart.Data<>(d.toString(), map.getOrDefault(d, 0L)));
        }
        usersCreatedChart.getData().setAll(series);
    }

    @FXML
    public void onAdminAddUser(ActionEvent e) { showAdminAddUserOverlay(true); }

    @FXML
    public void onCloseAdminAddUser(ActionEvent e) { showAdminAddUserOverlay(false); }

    @FXML
    public void onAdminAddUserBackdropClick(MouseEvent e) {
        if (e.getTarget() == adminAddUserBackdrop) {
            showAdminAddUserOverlay(false);
        }
    }

    private void showAdminAddUserOverlay(boolean show) {
        if (adminAddUserBackdrop != null) {
            adminAddUserBackdrop.setVisible(show);
            adminAddUserBackdrop.setManaged(show);
            if (show) {
                adminAddUserBackdrop.toFront();
                adminAddUserFullNameField.clear();
                adminAddUserEmailField.clear();
                adminAddUserPasswordField.clear();
                adminAddUserRoleBox.setValue(Role.CLIENT);
                adminAddUserActiveCheck.setSelected(true);
                adminAddUserStatusLabel.setText("");
                Platform.runLater(() -> adminAddUserFullNameField.requestFocus());
            }
        }
    }

    @FXML
    public void onAdminSaveNewUser(ActionEvent e) {
        String fullName = safeTrim(adminAddUserFullNameField.getText());
        String email = safeTrim(adminAddUserEmailField.getText());
        String password = adminAddUserPasswordField.getText();
        if (fullName.isBlank() || email.isBlank() || !email.contains("@") || password == null || password.length() < 6) {
            adminAddUserStatusLabel.setText("Veuillez remplir tous les champs correctement.");
            return;
        }
        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);
            if (dao.findByEmail(email).isPresent()) {
                adminAddUserStatusLabel.setText("Email déjà utilisé.");
                return;
            }
            User u = new User();
            u.setEmail(email);
            u.setFullName(fullName);
            u.setRole(adminAddUserRoleBox.getValue());
            u.setActive(adminAddUserActiveCheck.isSelected());
            u.setPasswordHash(Hashing.sha256(password));
            dao.insert(u);
            reloadAdminDashboard();
            showAdminAddUserOverlay(false);
        } catch (Exception ex) {
            adminAddUserStatusLabel.setText("Erreur: " + ex.getMessage());
        }
    }

    private void onAdminRowDoubleClick(User user) { showAdminEditUserOverlay(true, user); }

    private void showAdminEditUserOverlay(boolean show, User user) {
        adminSelectedUser = user;
        if (adminEditUserBackdrop != null) {
            adminEditUserBackdrop.setVisible(show);
            adminEditUserBackdrop.setManaged(show);
            if (show) {
                adminEditUserBackdrop.toFront();
                adminEditUserIdLabel.setText(String.valueOf(user.getId()));
                adminEditUserFullNameField.setText(user.getFullName());
                adminEditUserEmailField.setText(user.getEmail());
                adminEditUserRoleBox.setValue(user.getRole());
                adminEditUserActiveCheck.setSelected(user.isActive());
                adminEditUserStatusLabel.setText("");
                Platform.runLater(() -> adminEditUserFullNameField.requestFocus());
            }
        }
    }

    @FXML
    public void onCloseAdminEditUser(ActionEvent e) { showAdminEditUserOverlay(false, null); }

    @FXML
    public void onAdminEditUserBackdropClick(MouseEvent e) { showAdminEditUserOverlay(false, null); }

    @FXML
    public void onAdminSaveUser(ActionEvent e) {
        if (adminSelectedUser == null) return;
        String fullName = safeTrim(adminEditUserFullNameField.getText());
        String email = safeTrim(adminEditUserEmailField.getText());
        if (fullName.isBlank() || email.isBlank() || !email.contains("@")) {
            adminEditUserStatusLabel.setText("Nom et email valides sont requis.");
            return;
        }
        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);
            var existing = dao.findByEmail(email);
            if (existing.isPresent() && !existing.get().getId().equals(adminSelectedUser.getId())) {
                adminEditUserStatusLabel.setText("Email déjà utilisé.");
                return;
            }
            User toUpdate = dao.findById(adminSelectedUser.getId()).orElseThrow();
            toUpdate.setFullName(fullName);
            toUpdate.setEmail(email);
            toUpdate.setRole(adminEditUserRoleBox.getValue());
            toUpdate.setActive(adminEditUserActiveCheck.isSelected());
            dao.update(toUpdate);
            reloadAdminDashboard();
            showAdminEditUserOverlay(false, null);
        } catch (Exception ex) {
            adminEditUserStatusLabel.setText("Erreur: " + ex.getMessage());
        }
    }

    @FXML
    public void onAdminDeleteUser(ActionEvent e) {
        if (adminSelectedUser == null || adminSelectedUser.getId().equals(AppState.getCurrentUser().getId())) {
            adminEditUserStatusLabel.setText("Suppression impossible.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + adminSelectedUser.getEmail() + " ?", ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection cn = Db.getConnection()) {
                    new JdbcUserDao(cn).deleteById(adminSelectedUser.getId());
                    reloadAdminDashboard();
                    showAdminEditUserOverlay(false, null);
                } catch (Exception ex) {
                    adminEditUserStatusLabel.setText("Erreur: " + ex.getMessage());
                }
            }
        });
    }

    @FXML
    public void consumeEvent(MouseEvent e) { e.consume(); }

    @FXML
    public void onProfileCaptureFingerprint(ActionEvent e) {
        Services.BiometricAuthService bioService = Services.BiometricAuthService.getInstance();

        // Vérifier si le lecteur est disponible
        if (!bioService.isAvailable()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
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
        javafx.scene.control.Alert scanAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
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
                                profileFingerprintStatusLabel.setStyle("-fx-text-fill: #FF6B35; -fx-font-weight: bold;");
                            }
                        });
                        Thread.sleep(1000);
                    }

                    // Message de scan en cours
                    javafx.application.Platform.runLater(() -> {
                        if (profileFingerprintStatusLabel != null) {
                            profileFingerprintStatusLabel.setText("🔍 SCAN EN COURS... Maintenez votre doigt !");
                            profileFingerprintStatusLabel.setStyle("-fx-text-fill: #FF6B35; -fx-font-weight: bold; -fx-font-size: 13px;");
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
                            Optional<User> existingUser = userDao.findByFingerprintHash(fingerprint, currentUser.getId());
                            if (existingUser.isPresent()) {
                                // L'empreinte est déjà associée à un autre compte !
                                User other = existingUser.get();
                                javafx.application.Platform.runLater(() -> {
                                    javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                                    errorAlert.setTitle("Empreinte déjà utilisée");
                                    errorAlert.setHeaderText("⚠️ Cette empreinte est déjà associée à un autre compte");
                                    errorAlert.setContentText("Cette empreinte digitale est déjà enregistrée pour :\n\n" +
                                            "• Email : " + other.getEmail() + "\n" +
                                            "• Nom : " + other.getFullName() + "\n\n" +
                                            "Veuillez utiliser un AUTRE DOIGT pour votre empreinte.");
                                    errorAlert.showAndWait();

                                    if (profileFingerprintStatusLabel != null) {
                                        profileFingerprintStatusLabel.setText("❌ Empreinte déjà associée. Changez de doigt.");
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
                                javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                                successAlert.setTitle("Succès !");
                                successAlert.setHeaderText("✅ Empreinte enregistrée avec succès");
                                successAlert.setContentText("Votre empreinte digitale a été enregistrée.\n\n" +
                                        "Vous pouvez maintenant utiliser la connexion par empreinte !");
                                successAlert.showAndWait();

                                if (profileFingerprintStatusLabel != null) {
                                    profileFingerprintStatusLabel.setText("✓ Empreinte enregistrée avec succès");
                                    profileFingerprintStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                                }
                                if (profileFingerprintBtn != null) {
                                    profileFingerprintBtn.setText("🔄 Réenregistrer empreinte");
                                }
                            });
                        } catch (SQLException ex) {
                            javafx.application.Platform.runLater(() -> {
                                javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
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
                        javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
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

    private static String safeTrim(String s) { return s == null ? "" : s.trim(); }

    private static String getExtensionLower(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot > 0) ? fileName.substring(dot + 1).toLowerCase() : "";
    }
}
