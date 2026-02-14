package tn.esprit.ui;

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
import tn.esprit.db.Db;
import tn.esprit.domain.User;
import tn.esprit.repository.JdbcUserDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.domain.Role;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import javafx.stage.FileChooser;
import javafx.scene.layout.Region;
import javafx.scene.Parent;

import java.util.prefs.Preferences;

public class HomeController {

    @FXML private Label welcomeLabel;

    @FXML private TextField profileFullNameField;
    @FXML private TextField profileEmailField;
    @FXML private PasswordField profileNewPasswordField;
    @FXML private PasswordField profileConfirmPasswordField;
    @FXML private Label profileStatusLabel;

    @FXML private ImageView profileAvatarView;
    @FXML private Label profilePhotoHintLabel;

    @FXML private ImageView headerAvatarView;
    @FXML private VBox editProfileCard;
    @FXML private ScrollPane editProfileScroll;
    @FXML private javafx.scene.layout.StackPane editProfileBackdrop;
    @FXML private javafx.scene.layout.StackPane adminEditUserBackdrop;

    @FXML private VBox adminMenuSection;
    @FXML private VBox adminDashboardBox;
    @FXML private VBox transactionsSection;
    @FXML private VBox transactionsList;
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


    private String profilePhotoPath; // chemin local ou uri (persisté en DB)

    private final ObservableList<User> usersItems = FXCollections.observableArrayList();

    // Hauteur d'une ligne (approx). On garde une marge pour l'en-tête.
    private static final double USERS_TABLE_ROW_HEIGHT = 28.0;
    private static final double USERS_TABLE_HEADER_HEIGHT = 34.0;
    private static final double USERS_TABLE_MAX_HEIGHT = 420.0;

    private static final String THEME_ORANGE = "/css/theme-orange.css";
    private static final String THEME_LOGO = "/css/theme-logo.css";
    private static final String PREF_THEME_KEY = "fintrack.theme";

    private final Preferences prefs = Preferences.userNodeForPackage(HomeController.class);

    @FXML private VBox adminEditUserCard;
    @FXML private Label adminEditUserIdLabel;
    @FXML private TextField adminEditUserFullNameField;
    @FXML private TextField adminEditUserEmailField;
    @FXML private ComboBox<Role> adminEditUserRoleBox;
    @FXML private CheckBox adminEditUserActiveCheck;
    @FXML private Label adminEditUserStatusLabel;

    private User adminSelectedUser; // utilisateur sélectionné pour l'overlay

    @FXML
    public void initialize() {
        refreshHeader();

        // Applique le thème sauvegardé dès que la scene est disponible
        Platform.runLater(this::applySavedTheme);

        // caché par défaut (au cas où)
        showEditProfileOverlay(false);

        setupAdminTable();
        updateAdminVisibilityAndLoad();

        loadProfileFormFromState();
        loadProfilePhotoFromState();

        if (adminEditUserRoleBox != null) {
            adminEditUserRoleBox.getItems().setAll(Role.ADMIN, Role.CLIENT);
        }
        showAdminEditUserOverlay(false, null);
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
            welcomeLabel.setText("Bienvenue " + (u.getFullName() == null ? "" : u.getFullName()) + " (" + u.getRole() + ")");

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
        if (profileFullNameField != null) profileFullNameField.setText(u.getFullName() == null ? "" : u.getFullName());
        if (profileEmailField != null) profileEmailField.setText(u.getEmail() == null ? "" : u.getEmail());
        if (profileNewPasswordField != null) profileNewPasswordField.clear();
        if (profileConfirmPasswordField != null) profileConfirmPasswordField.clear();
        if (profileStatusLabel != null) profileStatusLabel.setText("");
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
        if (headerAvatarView == null) return;
        URL res = getClass().getResource("/assets/logo.png");
        if (res == null) {
            headerAvatarView.setImage(null);
            return;
        }
        headerAvatarView.setImage(new Image(res.toExternalForm(), true));
    }

    private void setHeaderAvatarImage(String pathOrUri) {
        if (headerAvatarView == null) return;
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
    public void consumeEvent(MouseEvent e) {
        // Empêche le clic dans les overlays de fermer le backdrop
        e.consume();
    }

    private void showEditProfileOverlay(boolean show) {
        System.out.println("=== DEBUG: showEditProfileOverlay(" + show + ") ===");

        if (editProfileBackdrop != null) {
            editProfileBackdrop.setVisible(show);
            // Important: si visible=false mais managed=true, JavaFX peut laisser des effets de layout/evt.
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
            // Le ScrollPane doit aussi suivre pour capter les events et afficher le contenu.
            editProfileScroll.setVisible(show);
            editProfileScroll.setManaged(show);
            if (show) {
                editProfileScroll.toFront();
            }
        }
    }

    private void setAvatarDefault() {
        if (profileAvatarView == null) return;
        URL res = getClass().getResource("/assets/logo.png");
        if (res == null) {
            profileAvatarView.setImage(null);
            return;
        }
        Image img = new Image(res.toExternalForm(), true);
        profileAvatarView.setImage(img);
    }

    private void setAvatarImage(String pathOrUri) {
        if (profileAvatarView == null) return;
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
     * Alias de compatibilité pour les anciennes versions du FXML qui appellent `#onPickProfilePhoto`.
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
            if (profileStatusLabel != null) profileStatusLabel.setText("Connecte-toi d'abord.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo de profil");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selected = chooser.showOpenDialog(profileAvatarView == null ? null : profileAvatarView.getScene().getWindow());
        if (selected == null) return;

        try {
            Path destDir = Path.of(System.getProperty("user.home"), ".fintrack", "profile-photos");
            Files.createDirectories(destDir);

            String ext = getExtensionLower(selected.getName());
            String fileName = "user-" + u.getId() + (ext.isEmpty() ? ".png" : ("." + ext));
            Path dest = destDir.resolve(fileName);

            Files.copy(selected.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            profilePhotoPath = dest.toString();
            // on met aussi à jour l'objet en mémoire (sera sauvegardé en DB lors de Enregistrer)
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
        updated.setProfilePhoto(current.getProfilePhoto());

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

    private void setupAdminTable() {
        if (usersTable == null) return;

        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        usersTable.setPlaceholder(new Label("Aucun utilisateur"));

        // Important: n'affiche/manipule pas de 'lignes vides' (utile si le layout donne trop de hauteur)
        usersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();

            // Double-clic => ouvre le formulaire d'édition (sans mot de passe)
            row.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2 && !row.isEmpty()) {
                    onAdminRowDoubleClick(row.getItem());
                }
            });

            row.itemProperty().addListener((obs, oldV, newV) -> {
                boolean empty = newV == null;
                row.setVisible(!empty);
                row.setManaged(!empty);
            });
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                row.setVisible(!isEmpty);
                row.setManaged(!isEmpty);
            });
            return row;
        });

        if (idCol != null) idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (fullNameCol != null) fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        if (emailCol != null) emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (roleCol != null) roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        if (activeCol != null) activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));

        usersTable.setItems(usersItems);

        // recalculer la hauteur à chaque changement (ajout/suppression)
        usersItems.addListener((javafx.collections.ListChangeListener<User>) c -> adjustUsersTableHeight());
        Platform.runLater(this::adjustUsersTableHeight);
    }

    private void updateAdminVisibilityAndLoad() {
        User u = AppState.getCurrentUser();
        boolean isAdmin = u != null && u.getRole() == Role.ADMIN;

        if (adminDashboardBox != null) {
            adminDashboardBox.setManaged(isAdmin);
            adminDashboardBox.setVisible(isAdmin);
        }

        if (isAdmin) {
            reloadAdminDashboard();
        }
    }

    @FXML
    public void onAdminRefresh(ActionEvent e) {
        reloadAdminDashboard();
    }

    private void reloadAdminDashboard() {
        if (adminStatusLabel != null) adminStatusLabel.setText("");

        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);

            long admins = dao.countByRole(Role.ADMIN);
            long clients = dao.countByRole(Role.CLIENT);

            if (adminCountLabel != null) adminCountLabel.setText(String.valueOf(admins));
            if (clientCountLabel != null) clientCountLabel.setText(String.valueOf(clients));

            // refresh dynamique: uniquement les lignes existantes
            usersItems.setAll(dao.findAll());
            adjustUsersTableHeight();
            loadUsersCreatedChart(dao);
            loadRolesPieChart(admins, clients);

        } catch (Exception ex) {
            if (adminStatusLabel != null) adminStatusLabel.setText("Erreur: " + ex.getMessage());
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
        for (var p : points) {
            map.put(p.date(), p.count());
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nouveaux users");
        for (int i = 0; i < 7; i++) {
            java.time.LocalDate d = start.plusDays(i);
            long c = map.getOrDefault(d, 0L);
            series.getData().add(new XYChart.Data<>(d.toString(), c));
        }

        usersCreatedChart.getData().setAll(series);
    }

    private void adjustUsersTableHeight() {
        if (usersTable == null) return;

        int rows = usersItems == null ? 0 : usersItems.size();
        // 0 item => on garde une petite hauteur pour afficher le placeholder
        double desired = USERS_TABLE_HEADER_HEIGHT + (Math.max(rows, 1) * USERS_TABLE_ROW_HEIGHT);
        double clamped = Math.min(desired, USERS_TABLE_MAX_HEIGHT);

        usersTable.setPrefHeight(clamped);
        usersTable.setMinHeight(clamped);
        usersTable.setMaxHeight(clamped);
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String getExtensionLower(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) return "";
        return fileName.substring(dot + 1).toLowerCase();
    }

    @FXML
    public void onToggleTheme(ActionEvent e) {
        Parent root = welcomeLabel == null ? null : welcomeLabel.getScene().getRoot();
        if (root == null) return;

        String current = prefs.get(PREF_THEME_KEY, THEME_ORANGE);
        String next = THEME_ORANGE.equals(current) ? THEME_LOGO : THEME_ORANGE;
        prefs.put(PREF_THEME_KEY, next);
        applyThemeToRoot(root, next);
    }

    private void applySavedTheme() {
        Parent root = welcomeLabel == null ? null : welcomeLabel.getScene().getRoot();
        if (root == null) return;

        String theme = prefs.get(PREF_THEME_KEY, THEME_ORANGE);
        applyThemeToRoot(root, theme);
    }

    private void applyThemeToRoot(Parent root, String themeResourcePath) {
        URL res = getClass().getResource(themeResourcePath);
        if (res == null) {
            // Ressource manquante -> ne casse pas l'UI
            return;
        }
        String uri = res.toExternalForm();

        // Retire uniquement nos 2 thèmes connus
        root.getStylesheets().removeIf(s -> s.endsWith("theme-orange.css") || s.endsWith("theme-logo.css"));

        if (!root.getStylesheets().contains(uri)) {
            root.getStylesheets().add(uri);
        }
    }

    @FXML
    public void onAdminAddUser(ActionEvent e) {
        User current = AppState.getCurrentUser();
        if (current == null || current.getRole() != Role.ADMIN) {
            if (adminStatusLabel != null) adminStatusLabel.setText("Accès refusé.");
            return;
        }

        UserDraft draft = showUserDialog(null);
        if (draft == null) return;

        if (draft.email.isBlank() || !draft.email.contains("@")) {
            if (adminStatusLabel != null) adminStatusLabel.setText("Email invalide.");
            return;
        }
        if (draft.fullName.isBlank()) {
            if (adminStatusLabel != null) adminStatusLabel.setText("Nom obligatoire.");
            return;
        }
        if (draft.password == null || draft.password.length() < 6) {
            if (adminStatusLabel != null) adminStatusLabel.setText("Mot de passe obligatoire (min 6).");
            return;
        }

        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);
            if (dao.findByEmail(draft.email).isPresent()) {
                if (adminStatusLabel != null) adminStatusLabel.setText("Email déjà utilisé.");
                return;
            }

            User u = new User();
            u.setEmail(draft.email);
            u.setFullName(draft.fullName);
            u.setRole(draft.role);
            u.setActive(draft.active);
            u.setPasswordHash(Hashing.sha256(draft.password));
            u.setProfilePhoto(null);

            dao.insert(u);
            reloadAdminDashboard();
            if (adminStatusLabel != null) adminStatusLabel.setText("Utilisateur ajouté.");
        } catch (Exception ex) {
            if (adminStatusLabel != null) adminStatusLabel.setText("Erreur: " + ex.getMessage());
        }
    }

    @FXML
    public void onAdminEditUser(ActionEvent e) {
        User current = AppState.getCurrentUser();
        if (current == null || current.getRole() != Role.ADMIN) {
            if (adminStatusLabel != null) adminStatusLabel.setText("Accès refusé.");
            return;
        }
        if (usersTable == null || usersTable.getSelectionModel().getSelectedItem() == null) {
            if (adminStatusLabel != null) adminStatusLabel.setText("Sélectionne un utilisateur.");
            return;
        }

        User selected = usersTable.getSelectionModel().getSelectedItem();
        UserDraft draft = showUserDialog(selected);
        if (draft == null) return;

        if (draft.email.isBlank() || !draft.email.contains("@")) {
            if (adminStatusLabel != null) adminStatusLabel.setText("Email invalide.");
            return;
        }
        if (draft.fullName.isBlank()) {
            if (adminStatusLabel != null) adminStatusLabel.setText("Nom obligatoire.");
            return;
        }

        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);
            // email unique (sauf pour lui-même)
            var existing = dao.findByEmail(draft.email);
            if (existing.isPresent() && existing.get().getId() != null && !existing.get().getId().equals(selected.getId())) {
                if (adminStatusLabel != null) adminStatusLabel.setText("Email déjà utilisé.");
                return;
            }

            User toUpdate = dao.findById(selected.getId()).orElseThrow();
            toUpdate.setEmail(draft.email);
            toUpdate.setFullName(draft.fullName);
            toUpdate.setRole(draft.role);
            toUpdate.setActive(draft.active);

            if (draft.password != null && !draft.password.isBlank()) {
                if (draft.password.length() < 6) {
                    if (adminStatusLabel != null) adminStatusLabel.setText("Mot de passe trop court (min 6).");
                    return;
                }
                toUpdate.setPasswordHash(Hashing.sha256(draft.password));
            }

            dao.update(toUpdate);
            reloadAdminDashboard();
            if (adminStatusLabel != null) adminStatusLabel.setText("Utilisateur modifié.");
        } catch (Exception ex) {
            if (adminStatusLabel != null) adminStatusLabel.setText("Erreur: " + ex.getMessage());
        }
    }

    @FXML
    public void onAdminDeleteUser(ActionEvent e) {
        User current = AppState.getCurrentUser();
        if (current == null || current.getRole() != Role.ADMIN) {
            if (adminStatusLabel != null) adminStatusLabel.setText("Accès refusé.");
            return;
        }
        if (usersTable == null || usersTable.getSelectionModel().getSelectedItem() == null) {
            if (adminStatusLabel != null) adminStatusLabel.setText("Sélectionne un utilisateur.");
            return;
        }

        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected.getId() != null && current.getId() != null && selected.getId().equals(current.getId())) {
            if (adminStatusLabel != null) adminStatusLabel.setText("Tu ne peux pas supprimer ton propre compte.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'utilisateur ?");
        alert.setContentText("ID: " + selected.getId() + "\nEmail: " + selected.getEmail());
        var res = alert.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);
            dao.deleteById(selected.getId());
            reloadAdminDashboard();
            if (adminStatusLabel != null) adminStatusLabel.setText("Utilisateur supprimé.");
        } catch (Exception ex) {
            if (adminStatusLabel != null) adminStatusLabel.setText("Erreur: " + ex.getMessage());
        }
    }

    /**
     * Dialogue simple pour créer/modifier un user.
     * - Si baseUser == null: création (password obligatoire)
     * - Sinon: édition (password facultatif)
     */
    private UserDraft showUserDialog(User baseUser) {
        Dialog<UserDraft> dialog = new Dialog<>();
        dialog.setTitle(baseUser == null ? "Ajouter un utilisateur" : "Modifier un utilisateur");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField email = new TextField(baseUser == null ? "" : safe(baseUser.getEmail()));
        TextField fullName = new TextField(baseUser == null ? "" : safe(baseUser.getFullName()));
        PasswordField password = new PasswordField();
        password.setPromptText(baseUser == null ? "Mot de passe (min 6)" : "Laisser vide pour ne pas changer");

        ComboBox<Role> role = new ComboBox<>();
        role.getItems().setAll(Role.ADMIN, Role.CLIENT);
        role.setValue(baseUser == null ? Role.CLIENT : baseUser.getRole());

        CheckBox active = new CheckBox("Actif");
        active.setSelected(baseUser == null || baseUser.isActive());

        int r = 0;
        grid.addRow(r++, new Label("Email"), email);
        grid.addRow(r++, new Label("Nom"), fullName);
        grid.addRow(r++, new Label("Mot de passe"), password);
        grid.addRow(r++, new Label("Rôle"), role);
        grid.add(active, 1, r);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(email::requestFocus);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            UserDraft d = new UserDraft();
            d.email = safeTrim(email.getText());
            d.fullName = safeTrim(fullName.getText());
            d.password = password.getText();
            d.role = role.getValue() == null ? Role.CLIENT : role.getValue();
            d.active = active.isSelected();
            return d;
        });

        return dialog.showAndWait().orElse(null);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static class UserDraft {
        String email;
        String fullName;
        String password;
        Role role;
        boolean active;
    }

    private void onAdminRowDoubleClick(User user) {
        User current = AppState.getCurrentUser();
        if (current == null || current.getRole() != Role.ADMIN) return;
        showAdminEditUserOverlay(true, user);
    }

    private void showAdminEditUserOverlay(boolean show, User user) {
        System.out.println("=== DEBUG: showAdminEditUserOverlay(" + show + ", user=" + (user != null ? user.getEmail() : "null") + ") ===");

        if (adminEditUserBackdrop != null) {
            adminEditUserBackdrop.setVisible(show);
            adminEditUserBackdrop.setManaged(show); // CHANGÉ: synchroniser avec visible
            adminEditUserBackdrop.setPickOnBounds(true);

            if (show) {
                adminEditUserBackdrop.toFront();
                adminEditUserBackdrop.requestLayout();
                System.out.println("✓ Backdrop Admin Edit User affiché - visible=" + adminEditUserBackdrop.isVisible() + ", managed=" + adminEditUserBackdrop.isManaged());
            } else {
                System.out.println("✓ Backdrop Admin Edit User masqué");
            }
        } else {
            System.out.println("✗ adminEditUserBackdrop est NULL");
        }

        if (!show) {
            adminSelectedUser = null;
            if (adminEditUserStatusLabel != null) adminEditUserStatusLabel.setText("");
            return;
        }

        adminSelectedUser = user;
        if (adminEditUserStatusLabel != null) adminEditUserStatusLabel.setText("");

        if (user == null) return;
        if (adminEditUserIdLabel != null) adminEditUserIdLabel.setText(String.valueOf(user.getId()));
        if (adminEditUserFullNameField != null) adminEditUserFullNameField.setText(safe(user.getFullName()));
        if (adminEditUserEmailField != null) adminEditUserEmailField.setText(safe(user.getEmail()));
        if (adminEditUserRoleBox != null) adminEditUserRoleBox.setValue(user.getRole());
        if (adminEditUserActiveCheck != null) adminEditUserActiveCheck.setSelected(user.isActive());

        if (adminEditUserFullNameField != null) {
            Platform.runLater(adminEditUserFullNameField::requestFocus);
        }

        if (show && adminEditUserCard != null) {
            Platform.runLater(() -> {
                adminEditUserCard.requestFocus();
                adminEditUserCard.toFront();
            });
        }
    }

    @FXML
    public void onCloseAdminEditUser(ActionEvent e) {
        showAdminEditUserOverlay(false, null);
    }

    /**
     * Clic sur le backdrop (zone sombre) => ferme l'overlay d'édition admin.
     * Référencé dans `home.fxml` via `onMouseClicked="#onAdminEditUserBackdropClick"`.
     */
    @FXML
    public void onAdminEditUserBackdropClick(MouseEvent e) {
        // évite de fermer si le clic vient d'un enfant qui a consommé l'événement
        showAdminEditUserOverlay(false, null);
    }

    @FXML
    public void onAdminSaveEditedUser(ActionEvent e) {
        User current = AppState.getCurrentUser();
        if (current == null || current.getRole() != Role.ADMIN) {
            if (adminEditUserStatusLabel != null) adminEditUserStatusLabel.setText("Accès refusé.");
            return;
        }
        if (adminSelectedUser == null || adminSelectedUser.getId() == null) {
            if (adminEditUserStatusLabel != null) adminEditUserStatusLabel.setText("Aucun utilisateur sélectionné.");
            return;
        }

        String fullName = safeTrim(adminEditUserFullNameField == null ? null : adminEditUserFullNameField.getText());
        String email = safeTrim(adminEditUserEmailField == null ? null : adminEditUserEmailField.getText());
        Role role = adminEditUserRoleBox == null ? null : adminEditUserRoleBox.getValue();
        boolean active = adminEditUserActiveCheck != null && adminEditUserActiveCheck.isSelected();

        if (fullName.isBlank()) {
            if (adminEditUserStatusLabel != null) adminEditUserStatusLabel.setText("Nom obligatoire.");
            return;
        }
        if (email.isBlank() || !email.contains("@")) {
            if (adminEditUserStatusLabel != null) adminEditUserStatusLabel.setText("Email invalide.");
            return;
        }
        if (role == null) role = Role.CLIENT;

        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);

            // email unique (sauf pour lui-même)
            var existing = dao.findByEmail(email);
            if (existing.isPresent() && existing.get().getId() != null && !existing.get().getId().equals(adminSelectedUser.getId())) {
                if (adminEditUserStatusLabel != null) adminEditUserStatusLabel.setText("Email déjà utilisé.");
                return;
            }

            User u = dao.findById(adminSelectedUser.getId()).orElseThrow();
            u.setFullName(fullName);
            u.setEmail(email);
            u.setRole(role);
            u.setActive(active);

            dao.update(u);
            reloadAdminDashboard();
            showAdminEditUserOverlay(false, null);
            if (adminStatusLabel != null) adminStatusLabel.setText("Utilisateur modifié.");
        } catch (Exception ex) {
            if (adminEditUserStatusLabel != null) adminEditUserStatusLabel.setText("Erreur: " + ex.getMessage());
        }
    }

    /**
     * Alias pour correspondre au FXML (`onAction="#onAdminSaveUser"`).
     */
    @FXML
    public void onAdminSaveUser(ActionEvent e) {
        onAdminSaveEditedUser(e);
    }

    @FXML
    public void onAdminDeleteEditedUser(ActionEvent e) {
        User current = AppState.getCurrentUser();
        if (current == null || current.getRole() != Role.ADMIN) {
            if (adminEditUserStatusLabel != null) adminEditUserStatusLabel.setText("Accès refusé.");
            return;
        }
        if (adminSelectedUser == null || adminSelectedUser.getId() == null) {
            if (adminEditUserStatusLabel != null) adminEditUserStatusLabel.setText("Aucun utilisateur sélectionné.");
            return;
        }

        if (current.getId() != null && current.getId().equals(adminSelectedUser.getId())) {
            if (adminEditUserStatusLabel != null) adminEditUserStatusLabel.setText("Tu ne peux pas supprimer ton propre compte.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'utilisateur ?");
        alert.setContentText("ID: " + adminSelectedUser.getId() + "\nEmail: " + adminSelectedUser.getEmail());
        var res = alert.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);
            dao.deleteById(adminSelectedUser.getId());
            reloadAdminDashboard();
            showAdminEditUserOverlay(false, null);
            if (adminStatusLabel != null) adminStatusLabel.setText("Utilisateur supprimé.");
        } catch (Exception ex) {
            if (adminEditUserStatusLabel != null) adminEditUserStatusLabel.setText("Erreur: " + ex.getMessage());
        }
    }
}
