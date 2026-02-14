package Controllers;

import Models.*;
import Services.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
 * Contrôleur pour le dashboard utilisateur.
 * Affiche les portefeuilles et cartes avec des cards modernes (pas de TableView).
 */
public class UserDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label statsLabel;
    @FXML private Label messageLabel;
    @FXML private Label cartesTitle;

    @FXML private FlowPane portefeuilleContainer;
    @FXML private FlowPane cartesContainer;

    private final PortefeuilleService portefeuilleService = new PortefeuilleService();
    private final CarteVirtuelleService carteService = new CarteVirtuelleService();

    private Portefeuille selectedPortefeuille;

    @FXML
    public void initialize() {
        setupHeader();
        loadPortefeuilles();
    }

    private void setupHeader() {
        Utilisateur user = Session.getUtilisateur();
        if (user != null) {
            welcomeLabel.setText("Bienvenue, " + user.getPrenom() + " " + user.getNom());
        }
    }

    // ============ CHARGEMENT DES DONNEES ============

    private void loadPortefeuilles() {
        portefeuilleContainer.getChildren().clear();
        try {
            Utilisateur user = Session.getUtilisateur();
            if (user == null) {
                showError("Session invalide. Veuillez vous reconnecter.");
                return;
            }

            // Only load the portefeuilles for the connected user
            List<Portefeuille> list = portefeuilleService.afficherParUtilisateur(user.getId());
            for (Portefeuille p : list) {
                portefeuilleContainer.getChildren().add(createPortefeuilleCard(p));
            }
            updateStats(list);

            // auto-select first portefeuille if none selected
            if (!list.isEmpty() && selectedPortefeuille == null) {
                selectedPortefeuille = list.get(0);
                cartesTitle.setText("Cartes: " + selectedPortefeuille.getNom());
                loadCartes(selectedPortefeuille.getId());
            }

        } catch (Exception e) {
            showError("Erreur chargement: " + e.getMessage());
        }
    }

    private void loadCartes(int portefeuilleId) {
        cartesContainer.getChildren().clear();
        try {
            List<CarteVirtuelle> list = carteService.getCartesByPortefeuille(portefeuilleId);
            for (CarteVirtuelle c : list) {
                cartesContainer.getChildren().add(createCarteCard(c));
            }
        } catch (Exception e) {
            showError("Erreur chargement cartes: " + e.getMessage());
        }
    }

    private void updateStats(List<Portefeuille> list) {
        int nbPf = list.size();
        double total = list.stream().mapToDouble(Portefeuille::getSoldeTotal).sum();
        statsLabel.setText(String.format("%d portefeuille(s) | Solde total: %.2f DT", nbPf, total));
    }

    // ============ CREATION DES CARDS ============

    private VBox createPortefeuilleCard(Portefeuille p) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-radius: 16; -fx-border-color: #E2E8F0; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);");

        // Icon container
        HBox iconBox = new HBox();
        iconBox.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("👛");
        icon.setStyle("-fx-font-size: 32; -fx-padding: 8; -fx-background-color: #DBEAFE; -fx-background-radius: 10;");
        iconBox.getChildren().add(icon);

        // Name
        Label nom = new Label(p.getNom());
        nom.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        // Balance
        Label solde = new Label(String.format("%.2f %s", p.getSoldeTotal(), p.getDevisePrincipale()));
        solde.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #10B981;");

        // ID Badge
        Label idLabel = new Label("ID: " + p.getId());
        idLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #94A3B8; -fx-background-color: #F1F5F9; " +
                "-fx-background-radius: 10; -fx-padding: 4 10;");

        // View cards button
        Button voirCartes = new Button("💳 Voir les cartes");
        voirCartes.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; " +
                "-fx-padding: 10 16; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: 600; " +
                "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);");
        voirCartes.setMaxWidth(Double.MAX_VALUE);
        voirCartes.setOnAction(e -> {
            selectedPortefeuille = p;
            cartesTitle.setText("Cartes: " + p.getNom());
            loadCartes(p.getId());
            showSuccess("Portefeuille sélectionné: " + p.getNom());

            // Update selected card style
            portefeuilleContainer.getChildren().forEach(node -> {
                if (node instanceof VBox) {
                    ((VBox) node).setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                            "-fx-border-radius: 16; -fx-border-color: #E2E8F0; -fx-border-width: 1; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);");
                }
            });
            card.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 16; " +
                    "-fx-border-radius: 16; -fx-border-color: #2563EB; -fx-border-width: 2; " +
                    "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.15), 12, 0, 0, 4);");
        });

        // Delete button
        Button supprimer = new Button("🗑️ Supprimer");
        supprimer.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; " +
                "-fx-padding: 6 12; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 11; -fx-font-weight: 500;");
        supprimer.setMaxWidth(Double.MAX_VALUE);
        supprimer.setOnAction(e -> supprimerPortefeuille(p));

        card.getChildren().addAll(iconBox, nom, solde, idLabel, voirCartes, supprimer);

        // Hover effects
        card.setOnMouseEntered(e -> {
            if (selectedPortefeuille == null || selectedPortefeuille.getId() != p.getId()) {
                card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 16; " +
                        "-fx-border-radius: 16; -fx-border-color: #93C5FD; -fx-border-width: 1; " +
                        "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.12), 14, 0, 0, 5);");
            }
        });
        card.setOnMouseExited(e -> {
            if (selectedPortefeuille == null || selectedPortefeuille.getId() != p.getId()) {
                card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                        "-fx-border-radius: 16; -fx-border-color: #E2E8F0; -fx-border-width: 1; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);");
            }
        });

        return card;
    }

    private VBox createCarteCard(CarteVirtuelle c) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(280);
        card.setPrefHeight(170);

        // Style based on card type - Modern credit card look
        String bgColor, textColor, borderColor;
        if (c.getType() == TypeCarte.GOLD) {
            bgColor = "linear-gradient(from 0% 100% to 100% 0%, #F4D03F 0%, #F9E79F 50%, #F4D03F 100%)";
            textColor = "#5D4E37";
            borderColor = "#D4AF37";
        } else if (c.getType() == TypeCarte.SILVER) {
            bgColor = "linear-gradient(from 0% 100% to 100% 0%, #E2E8F0 0%, #F8FAFC 50%, #E2E8F0 100%)";
            textColor = "#475569";
            borderColor = "#94A3B8";
        } else {
            bgColor = "linear-gradient(from 0% 100% to 100% 0%, #2563EB 0%, #3B82F6 50%, #1D4ED8 100%)";
            textColor = "white";
            borderColor = "#1E40AF";
        }

        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 16; " +
                "-fx-border-radius: 16; -fx-border-color: " + borderColor + "; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 14, 0, 0, 5);");

        // Card Header: Type badge + Status
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);

        Label type = new Label(c.getType() != null ? "★ " + c.getType().name() : "CARTE");
        type.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: " + textColor + "; -fx-opacity: 0.85;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statut = new Label(c.isActiver() ? "● Active" : "○ Inactive");
        String statutColor = c.isActiver() ? "#10B981" : "#EF4444";
        statut.setStyle("-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: " + statutColor + "; " +
                "-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10; -fx-padding: 4 10;");

        header.getChildren().addAll(type, spacer, statut);

        // Card Number (masked)
        String numero = maskCardNumber(c.getNumeroCarte());
        Label numeroLabel = new Label(numero);
        numeroLabel.setStyle("-fx-font-size: 17; -fx-font-weight: bold; -fx-text-fill: " + textColor + "; " +
                "-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-letter-spacing: 2;");

        // Balance
        Label solde = new Label(String.format("%.2f %s", c.getSolde(), c.getDevise() != null ? c.getDevise().name() : ""));
        solde.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

        // Info row: Plafond
        Label plafond = new Label("Plafond: " + String.format("%.0f %s", c.getPlafond(), c.getDevise() != null ? c.getDevise().name() : ""));
        plafond.setStyle("-fx-font-size: 11; -fx-text-fill: " + textColor + "; -fx-opacity: 0.7;");

        // Action buttons
        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER);

        Button recharger = new Button("💰 Recharger");
        recharger.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: " + textColor + "; " +
                "-fx-padding: 8 14; -fx-background-radius: 8; -fx-font-size: 11; -fx-font-weight: 600; -fx-cursor: hand; " +
                "-fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 8;");
        recharger.setOnAction(e -> rechargerCarte(c));

        Button supprimer = new Button("🗑️");
        supprimer.setStyle("-fx-background-color: rgba(239, 68, 68, 0.85); -fx-text-fill: white; " +
                "-fx-padding: 8 12; -fx-background-radius: 8; -fx-font-size: 11; -fx-cursor: hand;");
        supprimer.setOnAction(e -> supprimerCarte(c));

        buttons.getChildren().addAll(recharger, supprimer);

        card.getChildren().addAll(header, numeroLabel, solde, plafond, buttons);

        return card;
    }

    private String maskCardNumber(String numero) {
        if (numero == null || numero.length() < 4) return "****";
        return "**** **** **** " + numero.substring(numero.length() - 4);
    }

    // ============ ACTIONS ============

    @FXML
    private void handleAjouterPortefeuille() {
        Utilisateur user = Session.getUtilisateur();
        if (user == null) {
            showError("Session invalide. Veuillez vous reconnecter.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("Mon Portefeuille");
        dialog.setTitle("Nouveau Portefeuille");
        dialog.setHeaderText("Creer un nouveau portefeuille");
        dialog.setContentText("Nom:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nom -> {
            if (!nom.trim().isEmpty()) {
                Portefeuille p = new Portefeuille(nom.trim(), 0, "DT");
                // attach to current user
                portefeuilleService.ajouterPourUtilisateur(p, user.getId());
                loadPortefeuilles();
                showSuccess("Portefeuille cree: " + nom);
            }
        });
    }

    private void supprimerPortefeuille(Portefeuille p) {
        Utilisateur user = Session.getUtilisateur();
        if (user == null) { showError("Session invalide"); return; }
        if (p.getUtilisateurId() != null && p.getUtilisateurId() != user.getId()) {
            showError("Vous ne pouvez pas supprimer ce portefeuille");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer suppression");
        confirm.setHeaderText("Supprimer " + p.getNom() + " ?");
        confirm.setContentText("Cette action supprimera aussi toutes les cartes associees.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            portefeuilleService.supprimer(p.getId());
            loadPortefeuilles();
            cartesContainer.getChildren().clear();
            cartesTitle.setText("Cartes du portefeuille");
            showSuccess("Portefeuille supprime");
        }
    }

    @FXML
    private void handleAjouterCarte() {
        if (selectedPortefeuille == null) {
            showError("Selectionnez d'abord un portefeuille (cliquez sur 'Voir les cartes')");
            return;
        }

        // Build dialog with type and currency selection
        Dialog<CarteVirtuelle> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Carte");
        dialog.setHeaderText("Creer une carte dans: " + selectedPortefeuille.getNom());

        ButtonType ok = new ButtonType("Creer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        // Type selection
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("NORMAL", "GOLD", "SILVER");
        typeCombo.getSelectionModel().select("NORMAL");

        // Currency selection
        ComboBox<String> deviseCombo = new ComboBox<>();
        deviseCombo.getItems().addAll("DT", "USD", "EUR");
        deviseCombo.getSelectionModel().select("DT");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Type de carte:"), typeCombo);
        grid.addRow(1, new Label("Devise:"), deviseCombo);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != ok) return null;
            String typeStr = typeCombo.getSelectionModel().getSelectedItem();
            String deviseStr = deviseCombo.getSelectionModel().getSelectedItem();
            if (typeStr == null || deviseStr == null) return null;

            TypeCarte type = TypeCarte.valueOf(typeStr);
            Devise devise = Devise.valueOf(deviseStr);
            double plafond = type == TypeCarte.GOLD ? 5000 : (type == TypeCarte.SILVER ? 2500 : 1000);
            return new CarteVirtuelle(0, plafond, type, devise, selectedPortefeuille.getId());
        });

        Optional<CarteVirtuelle> result = dialog.showAndWait();
        result.ifPresent(carte -> {
            carteService.ajouter(carte);

            // Refresh UI + totals
            loadCartes(selectedPortefeuille.getId());
            loadPortefeuilles();

            showSuccess("Carte " + carte.getType() + " creee en " + carte.getDevise());
        });
    }

    private void rechargerCarte(CarteVirtuelle c) {
        TextInputDialog dialog = new TextInputDialog("100");
        dialog.setTitle("Recharger la carte");
        dialog.setHeaderText("Carte: " + maskCardNumber(c.getNumeroCarte()));
        dialog.setContentText("Montant:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(montantStr -> {
            try {
                double montant = Double.parseDouble(montantStr);
                if (montant > 0) {
                    carteService.recharger(c.getId(), montant);

                    // Refresh UI + totals
                    loadCartes(selectedPortefeuille.getId());
                    loadPortefeuilles();

                    showSuccess("Carte rechargee de " + montant + " DT");
                }
            } catch (NumberFormatException e) {
                showError("Montant invalide");
            }
        });
    }

    private void supprimerCarte(CarteVirtuelle c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer");
        confirm.setHeaderText("Supprimer cette carte ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            carteService.supprimer(c.getId());

            // Refresh UI + totals
            loadCartes(selectedPortefeuille.getId());
            loadPortefeuilles();

            showSuccess("Carte supprimee");
        }
    }

    @FXML
    private void handleRefresh() {
        loadPortefeuilles();
        if (selectedPortefeuille != null) {
            loadCartes(selectedPortefeuille.getId());
        }
        showSuccess("Donnees actualisees");
    }

    @FXML
    private void handleGoToTransactions() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/transactions.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("FinTrack - Transactions");
        } catch (IOException e) {
            showError("Erreur: " + e.getMessage());
        }
    }

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

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 500;");
        messageLabel.setText("❌ " + msg);
    }

    private void showSuccess(String msg) {
        messageLabel.setStyle("-fx-text-fill: #059669; -fx-font-weight: 500;");
        messageLabel.setText("✅ " + msg);
    }
}
