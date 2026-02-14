package Controllers;

import Models.*;
import Services.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour le dashboard administrateur.
 * Utilise des cartes stylisées au lieu de TableView.
 */
public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label statsLabel;
    @FXML private Label messageLabel;

    @FXML private FlowPane utilisateursContainer;
    @FXML private FlowPane portefeuillesContainer;
    @FXML private FlowPane cartesContainer;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final PortefeuilleService portefeuilleService = new PortefeuilleService();
    private final CarteVirtuelleService carteService = new CarteVirtuelleService();

    @FXML
    public void initialize() {
        setupHeader();
        loadAllData();
    }

    private void setupHeader() {
        Utilisateur admin = Session.getUtilisateur();
        if (admin != null) {
            welcomeLabel.setText("Admin: " + admin.getPrenom() + " " + admin.getNom());
        }
    }

    private void loadAllData() {
        loadUtilisateurs();
        loadPortefeuilles();
        loadCartes();
        updateStats();
    }

    // ============ CHARGEMENT UTILISATEURS ============
    private void loadUtilisateurs() {
        utilisateursContainer.getChildren().clear();
        try {
            List<Utilisateur> list = utilisateurService.afficherTous();
            for (Utilisateur u : list) {
                utilisateursContainer.getChildren().add(createUserCard(u));
            }
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private VBox createUserCard(Utilisateur u) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18));
        card.setPrefWidth(220);
        card.setAlignment(Pos.CENTER_LEFT);

        String borderColor = u.isAdmin() ? "#F59E0B" : "#E2E8F0";
        String borderWidth = u.isAdmin() ? "2" : "1";
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-border-radius: 14; -fx-border-color: " + borderColor + "; -fx-border-width: " + borderWidth + "; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 3);");

        // Avatar
        Label icon = new Label(u.isAdmin() ? "👑" : "👤");
        icon.setStyle("-fx-font-size: 28; -fx-padding: 10; -fx-background-color: " +
                (u.isAdmin() ? "#FEF3C7" : "#F1F5F9") + "; -fx-background-radius: 50;");

        // Name
        Label nom = new Label(u.getPrenom() + " " + u.getNom());
        nom.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        // Email
        Label email = new Label(u.getEmail());
        email.setStyle("-fx-font-size: 11; -fx-text-fill: #64748B;");

        // Role badge
        Label role = new Label(u.isAdmin() ? "Admin" : "User");
        role.setStyle("-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: " +
                (u.isAdmin() ? "#D97706" : "#2563EB") + "; -fx-background-color: " +
                (u.isAdmin() ? "#FEF3C7" : "#DBEAFE") + "; -fx-background-radius: 12; -fx-padding: 4 12;");

        // Balance
        Label solde = new Label(String.format("💰 %.2f DT", u.getSolde()));
        solde.setStyle("-fx-font-size: 12; -fx-text-fill: #10B981; -fx-font-weight: 600;");

        Button modifier = new Button("✏️ Modifier");
        modifier.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8; -fx-padding: 6 12; -fx-background-radius: 8; -fx-font-size: 10; -fx-font-weight: 500; -fx-cursor: hand;");
        modifier.setMaxWidth(Double.MAX_VALUE);
        modifier.setOnAction(e -> modifierUtilisateur(u));

        // Delete button
        Button supprimer = new Button("🗑️ Supprimer");
        supprimer.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; " +
                "-fx-padding: 6 12; -fx-background-radius: 8; -fx-font-size: 10; -fx-font-weight: 500; -fx-cursor: hand;");
        supprimer.setMaxWidth(Double.MAX_VALUE);
        supprimer.setOnAction(e -> supprimerUtilisateur(u));

        card.getChildren().addAll(icon, nom, email, role, solde, modifier, supprimer);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 14; " +
                "-fx-border-radius: 14; -fx-border-color: " + (u.isAdmin() ? "#F59E0B" : "#93C5FD") + "; -fx-border-width: " + borderWidth + "; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-border-radius: 14; -fx-border-color: " + borderColor + "; -fx-border-width: " + borderWidth + "; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 3);"));

        return card;
    }

    private void supprimerUtilisateur(Utilisateur u) {
        if (u.getId() == Session.getUtilisateur().getId()) {
            showError("Vous ne pouvez pas supprimer votre propre compte");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer");
        confirm.setHeaderText("Supprimer " + u.getEmail() + " ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            utilisateurService.supprimer(u.getId());
            loadUtilisateurs();
            updateStats();
            showSuccess("Utilisateur supprime");
        }
    }

    // ============ CHARGEMENT PORTEFEUILLES ============
    private void loadPortefeuilles() {
        portefeuillesContainer.getChildren().clear();
        try {
            List<Portefeuille> list = portefeuilleService.afficherTous();
            for (Portefeuille p : list) {
                portefeuillesContainer.getChildren().add(createPortefeuilleCard(p));
            }
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private VBox createPortefeuilleCard(Portefeuille p) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18));
        card.setPrefWidth(200);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: linear-gradient(from 0% 100% to 100% 0%, #6366F1, #8B5CF6); " +
                "-fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(99, 102, 241, 0.3), 12, 0, 0, 4);");

        Label icon = new Label("👛");
        icon.setStyle("-fx-font-size: 32;");

        Label nom = new Label(p.getNom());
        nom.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: white;");

        Label solde = new Label(String.format("%.2f %s", p.getSoldeTotal(), p.getDevisePrincipale()));
        solde.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");

        Label id = new Label("ID: " + p.getId());
        id.setStyle("-fx-font-size: 10; -fx-text-fill: rgba(255,255,255,0.7);");

        Button modifier = new Button("✏️ Modifier");
        modifier.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white; -fx-padding: 8 14; -fx-background-radius: 8; -fx-font-size: 11; -fx-cursor: hand; -fx-font-weight: 500;");
        modifier.setMaxWidth(Double.MAX_VALUE);
        modifier.setOnAction(e -> modifierPortefeuille(p));

        Button supprimer = new Button("🗑️ Supprimer");
        supprimer.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-padding: 8 14; -fx-background-radius: 8; -fx-font-size: 11; -fx-cursor: hand; -fx-font-weight: 500;");
        supprimer.setMaxWidth(Double.MAX_VALUE);
        supprimer.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("Supprimer ce portefeuille ?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                portefeuilleService.supprimer(p.getId());
                loadPortefeuilles();
                loadCartes();
                updateStats();
                showSuccess("Portefeuille supprimé");
            }
        });

        card.getChildren().addAll(icon, nom, solde, id, modifier, supprimer);
        return card;
    }

    // ============ CHARGEMENT CARTES ============
    private void loadCartes() {
        cartesContainer.getChildren().clear();
        try {
            List<CarteVirtuelle> list = carteService.afficherTous();
            for (CarteVirtuelle c : list) {
                cartesContainer.getChildren().add(createCarteCard(c));
            }
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private VBox createCarteCard(CarteVirtuelle c) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setPrefWidth(240);
        card.setPrefHeight(150);

        String bgColor, textColor, borderColor;
        if (c.getType() == TypeCarte.GOLD) {
            bgColor = "linear-gradient(135deg, #F4D03F 0%, #F9E79F 50%, #F4D03F 100%)";
            textColor = "#5D4E37";
            borderColor = "#D4AF37";
        } else if (c.getType() == TypeCarte.SILVER) {
            bgColor = "linear-gradient(135deg, #E2E8F0 0%, #F8FAFC 50%, #E2E8F0 100%)";
            textColor = "#475569";
            borderColor = "#94A3B8";
        } else {
            bgColor = "linear-gradient(135deg, #2563EB 0%, #3B82F6 50%, #1D4ED8 100%)";
            textColor = "white";
            borderColor = "#1E40AF";
        }

        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 14; " +
                "-fx-border-radius: 14; -fx-border-color: " + borderColor + "; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 10, 0, 0, 4);");

        // Type
        Label type = new Label(c.getType() != null ? "★ " + c.getType().name() : "CARTE");
        type.setStyle("-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: " + textColor + "; -fx-opacity: 0.85;");

        // Card number
        String masked = c.getNumeroCarte() != null && c.getNumeroCarte().length() >= 4
            ? "**** **** **** " + c.getNumeroCarte().substring(c.getNumeroCarte().length() - 4) : "****";
        Label numero = new Label(masked);
        numero.setStyle("-fx-font-size: 13; -fx-text-fill: " + textColor + "; -fx-font-family: 'Consolas', monospace; -fx-font-weight: bold;");

        // Balance
        Label solde = new Label(String.format("%.2f %s", c.getSolde(), c.getDevise() != null ? c.getDevise().name() : ""));
        solde.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

        // Status badge
        Label statut = new Label(c.isActiver() ? "● Active" : "○ Inactive");
        String statutColor = c.isActiver() ? "#10B981" : "#EF4444";
        statut.setStyle("-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: " + statutColor + "; " +
                "-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 8; -fx-padding: 3 8;");

        // Delete button
        Button suppr = new Button("🗑️");
        suppr.setStyle("-fx-background-color: rgba(239, 68, 68, 0.85); -fx-text-fill: white; " +
                "-fx-padding: 6 10; -fx-background-radius: 6; -fx-font-size: 10; -fx-cursor: hand;");
        suppr.setOnAction(e -> {
            carteService.supprimer(c.getId());
            loadCartes();
            updateStats();
            showSuccess("Carte supprimée");
        });

        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bottomRow.getChildren().addAll(statut, spacer, suppr);

        card.getChildren().addAll(type, numero, solde, bottomRow);
        return card;
    }

    // ============ STATS ============
    private void updateStats() {
        try {
            int nbUsers = (int) utilisateurService.countUtilisateurs();
            List<Portefeuille> pfs = portefeuilleService.afficherTous();
            List<CarteVirtuelle> cartes = carteService.afficherTous();
            double total = pfs.stream().mapToDouble(Portefeuille::getSoldeTotal).sum();
            statsLabel.setText(String.format("%d utilisateurs | %d portefeuilles | %d cartes | %.2f DT total",
                    nbUsers, pfs.size(), cartes.size(), total));
        } catch (Exception e) {
            statsLabel.setText("Erreur stats");
        }
    }

    // ============ ACTIONS ============
    @FXML private void handleRefreshAll() { loadAllData(); showSuccess("Donnees actualisees"); }
    @FXML private void handleRefreshUsers() { loadUtilisateurs(); showSuccess("Utilisateurs actualises"); }
    @FXML private void handleRefreshPortefeuilles() { loadPortefeuilles(); showSuccess("Portefeuilles actualises"); }
    @FXML private void handleRefreshCartes() { loadCartes(); showSuccess("Cartes actualisees"); }

    @FXML
    private void handleDeconnexion() {
        Session.clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 450));
            stage.setTitle("FinTrack - Connexion");
        } catch (IOException e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    // ============ CRUD UTILISATEUR (ADMIN) ============
    @FXML
    private void handleAjouterUtilisateur() {
        Dialog<Utilisateur> dialog = buildUtilisateurDialog("Ajouter utilisateur", null);
        dialog.showAndWait().ifPresent(u -> {
            utilisateurService.ajouter(u);
            loadUtilisateurs();
            updateStats();
            showSuccess("Utilisateur ajouté");
        });
    }

    private void modifierUtilisateur(Utilisateur u) {
        Dialog<Utilisateur> dialog = buildUtilisateurDialog("Modifier utilisateur", u);
        dialog.showAndWait().ifPresent(updated -> {
            updated.setId(u.getId());
            utilisateurService.modifier(updated);
            loadUtilisateurs();
            updateStats();
            showSuccess("Utilisateur modifié");
        });
    }

    private Dialog<Utilisateur> buildUtilisateurDialog(String title, Utilisateur existing) {
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(title);

        ButtonType ok = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField email = new TextField(existing != null ? existing.getEmail() : "");
        TextField nom = new TextField(existing != null ? existing.getNom() : "");
        TextField prenom = new TextField(existing != null ? existing.getPrenom() : "");
        PasswordField password = new PasswordField();
        if (existing != null && existing.getPassword() != null) {
            password.setText(existing.getPassword());
        }

        ComboBox<String> role = new ComboBox<>();
        role.getItems().addAll("user", "admin");
        role.getSelectionModel().select(existing != null && existing.getRole() != null ? existing.getRole() : "user");

        TextField solde = new TextField(existing != null ? String.valueOf(existing.getSolde()) : "0");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Email"), email);
        grid.addRow(1, new Label("Nom"), nom);
        grid.addRow(2, new Label("Prenom"), prenom);
        grid.addRow(3, new Label("Password"), password);
        grid.addRow(4, new Label("Role"), role);
        grid.addRow(5, new Label("Solde"), solde);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != ok) return null;
            if (email.getText() == null || email.getText().trim().isEmpty()) return null;
            double s;
            try { s = Double.parseDouble(solde.getText().trim()); } catch (Exception e) { return null; }
            return new Utilisateur(email.getText().trim(), nom.getText().trim(), prenom.getText().trim(), password.getText(), role.getValue(), s);
        });


        return dialog;
    }

    // ============ CRUD PORTEFEUILLE (ADMIN) ============
    @FXML
    private void handleAjouterPortefeuille() {
        // Build dialog for portefeuille + user selection
        Dialog<Portefeuille> dialog = buildPortefeuilleDialog("Ajouter portefeuille", null);

        // Add a user selection dropdown to the dialog
        javafx.scene.control.ComboBox<String> userCombo = new javafx.scene.control.ComboBox<>();
        java.util.List<Models.Utilisateur> users = utilisateurService.afficherTous();
        for (Models.Utilisateur u : users) {
            userCombo.getItems().add(u.getId() + ": " + u.getEmail());
        }
        if (!userCombo.getItems().isEmpty()) userCombo.getSelectionModel().select(0);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        javafx.scene.control.TextField nom = new javafx.scene.control.TextField();
        javafx.scene.control.TextField solde = new javafx.scene.control.TextField("0");
        javafx.scene.control.ComboBox<String> devise = new javafx.scene.control.ComboBox<>();
        devise.getItems().addAll("DT","USD","EUR"); devise.getSelectionModel().select("DT");

        grid.addRow(0, new javafx.scene.control.Label("Nom"), nom);
        grid.addRow(1, new javafx.scene.control.Label("Solde"), solde);
        grid.addRow(2, new javafx.scene.control.Label("Devise"), devise);
        grid.addRow(3, new javafx.scene.control.Label("Utilisateur"), userCombo);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.control.ButtonType ok = new javafx.scene.control.ButtonType("Enregistrer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(ok, javafx.scene.control.ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != ok) return null;
            try {
                String name = nom.getText().trim();
                double s = Double.parseDouble(solde.getText().trim());
                String dev = devise.getValue();
                return new Portefeuille(name, s, dev);
            } catch (Exception e) { return null; }
        });

        dialog.showAndWait().ifPresent(p -> {
            // find selected user id
            String sel = userCombo.getSelectionModel().getSelectedItem();
            int userId = sel != null && sel.contains(":" ) ? Integer.parseInt(sel.split(":")[0]) : 0;
            portefeuilleService.ajouterPourUtilisateur(p, userId);
            loadPortefeuilles();
            updateStats();
            showSuccess("Portefeuille ajouté pour user id=" + userId);
        });
    }

    private void modifierPortefeuille(Portefeuille p) {
        Dialog<Portefeuille> dialog = buildPortefeuilleDialog("Modifier portefeuille", p);
        dialog.showAndWait().ifPresent(updated -> {
            updated.setId(p.getId());
            portefeuilleService.modifier(updated);
            loadPortefeuilles();
            updateStats();
            showSuccess("Portefeuille modifié");
        });
    }

    private Dialog<Portefeuille> buildPortefeuilleDialog(String title, Portefeuille existing) {
        Dialog<Portefeuille> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(title);

        ButtonType ok = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField nom = new TextField(existing != null ? existing.getNom() : "");
        TextField solde = new TextField(existing != null ? String.valueOf(existing.getSoldeTotal()) : "0");

        ComboBox<String> devise = new ComboBox<>();
        devise.getItems().addAll("DT", "USD", "EUR");
        devise.getSelectionModel().select(existing != null && existing.getDevisePrincipale() != null ? existing.getDevisePrincipale() : "DT");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Nom"), nom);
        grid.addRow(1, new Label("Solde"), solde);
        grid.addRow(2, new Label("Devise"), devise);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != ok) return null;
            if (nom.getText() == null || nom.getText().trim().isEmpty()) return null;
            double s;
            try { s = Double.parseDouble(solde.getText().trim()); } catch (Exception e) { return null; }
            return new Portefeuille(nom.getText().trim(), s, devise.getValue());
        });

        return dialog;
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 500;");
        messageLabel.setText("❌ " + msg);
    }

    private void showSuccess(String msg) {
        messageLabel.setStyle("-fx-text-fill: #059669; -fx-font-weight: 500;");
        messageLabel.setText("✅ " + msg);
    }
}
