package Controllers;

import Models.Portefeuille;
import Models.Session;
import Models.Utilisateur;
import Services.PortefeuilleService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * Dashboard utilisateur (portefeuilles)
 * - UI moderne (cards)
 * - CRUD portefeuilles (dialogs)
 */
public class PortefeuilleController {

    @FXML private Label headerUserLabel;
    @FXML private FlowPane portefeuillesFlow;
    @FXML private Label messageLabel;

    private final PortefeuilleService portefeuilleService = new PortefeuilleService();
    private javafx.scene.Node selectedCardNode = null;

    @FXML
    public void initialize() {
        Utilisateur u = Session.getUtilisateur();
        headerUserLabel.setText(u != null ? (u.getPrenom() + " " + u.getNom()) : "Invité");
        messageLabel.setText("");
        handleRefresh();
    }

    @FXML
    private void handleRefresh() {
        portefeuillesFlow.getChildren().clear();
        selectedCardNode = null; // clear selection on refresh
        try {
            Utilisateur u = Session.getUtilisateur();
            if (u == null) {
                messageLabel.setText("Session expirée. Veuillez vous reconnecter.");
                return;
            }

            List<Portefeuille> list = portefeuilleService.afficherParUtilisateur(u.getId());
            for (Portefeuille p : list) {
                portefeuillesFlow.getChildren().add(createPortefeuilleCard(p));
            }
            messageLabel.setText(list.isEmpty() ? "Aucun portefeuille." : "");
        } catch (Exception e) {
            messageLabel.setText("Erreur chargement portefeuilles: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenSelectedPortefeuille() {
        Integer sel = Session.getSelectedPortefeuilleId();
        if (sel == null) {
            messageLabel.setText("Veuillez sélectionner un portefeuille ou cliquer sur 'Cartes' d'une carte.");
            return;
        }
        openCartesView(sel);
    }

    private VBox createPortefeuilleCard(Portefeuille p) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("card", "portefeuille-card");
        card.setPadding(new Insets(16));
        card.setPrefWidth(280);

        // allow selection
        card.setOnMouseClicked(evt -> {
            // deselect previous
            if (selectedCardNode != null) {
                selectedCardNode.setStyle("");
            }
            // select this
            selectedCardNode = card;
            card.setStyle("-fx-border-color: #2563EB; -fx-border-width: 2; -fx-background-color: #F8FAFF;");
            Session.setSelectedPortefeuilleId(p.getId());  // This line is important
            messageLabel.setText("Portefeuille sélectionné: " + p.getNom());
        });

        Label name = new Label(p.getNom());
        name.getStyleClass().add("section-title");

        Label balance = new Label(String.format("%.2f %s", p.getSoldeTotal(), p.getDevisePrincipale()));
        balance.setStyle("-fx-font-size: 18; -fx-font-weight: 700; -fx-text-fill: -fx-secondary;");

        Label id = new Label("ID: " + p.getId());
        id.setStyle("-fx-text-fill: -fx-text-muted; -fx-font-size: 11;");

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button viewCards = new Button("Cartes");
        viewCards.getStyleClass().addAll("btn", "btn-primary");
        viewCards.setOnAction(e -> openCartesForPortefeuille(p));

        Button edit = new Button("Modifier");
        edit.getStyleClass().addAll("btn", "btn-secondary");
        edit.setOnAction(e -> handleModifierPortefeuille(p));

        Button del = new Button("Supprimer");
        del.getStyleClass().addAll("btn", "btn-danger");
        del.setOnAction(e -> handleSupprimerPortefeuille(p));

        actions.getChildren().addAll(viewCards, edit, del);

        card.getChildren().addAll(name, balance, id, actions);
        return card;
    }

    @FXML
    private void handleAjouterPortefeuille() {
        Utilisateur u = Session.getUtilisateur();
        if (u == null) {
            messageLabel.setText("Session expirée. Veuillez vous reconnecter.");
            return;
        }

        Dialog<Portefeuille> dialog = buildPortefeuilleDialog("Nouveau portefeuille", null);
        dialog.showAndWait().ifPresent(p -> {
            portefeuilleService.ajouterPourUtilisateur(p, u.getId());
            handleRefresh();
            messageLabel.setText("✅ Portefeuille créé");
        });
    }

    private void handleModifierPortefeuille(Portefeuille p) {
        Dialog<Portefeuille> dialog = buildPortefeuilleDialog("Modifier portefeuille", p);
        dialog.showAndWait().ifPresent(updated -> {
            updated.setId(p.getId());
            portefeuilleService.modifier(updated);
            handleRefresh();
            messageLabel.setText("✅ Portefeuille modifié");
        });
    }

    private void handleSupprimerPortefeuille(Portefeuille p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le portefeuille ?");
        confirm.setContentText(p.getNom());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            portefeuilleService.supprimer(p.getId());
            handleRefresh();
            messageLabel.setText("✅ Portefeuille supprimé");
        }
    }

    private Dialog<Portefeuille> buildPortefeuilleDialog(String title, Portefeuille existing) {
        Dialog<Portefeuille> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(title);

        ButtonType okBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        TextField nomField = new TextField(existing != null ? existing.getNom() : "");
        TextField soldeField = new TextField(existing != null ? String.valueOf(existing.getSoldeTotal()) : "0");

        ComboBox<String> deviseCombo = new ComboBox<>();
        deviseCombo.getItems().addAll("DT", "USD", "EUR");
        deviseCombo.getSelectionModel().select(existing != null && existing.getDevisePrincipale() != null ? existing.getDevisePrincipale() : "DT");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Nom"), nomField);
        grid.addRow(1, new Label("Solde"), soldeField);
        grid.addRow(2, new Label("Devise"), deviseCombo);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != okBtn) return null;
            String nom = nomField.getText() == null ? "" : nomField.getText().trim();
            if (nom.isEmpty()) return null;

            double solde;
            try {
                solde = Double.parseDouble(soldeField.getText().trim());
            } catch (Exception e) {
                return null;
            }
            return new Portefeuille(nom, solde, deviseCombo.getValue());
        });

        return dialog;
    }

    private void openCartesForPortefeuille(Portefeuille p) {
        if (p == null) return;
        Session.setSelectedPortefeuilleId(p.getId());
        openCartesView(p.getId());
    }

    private void openCartesView(int portefeuilleId) {
        try {
            // Use the correct FXML file name from your project
            var resource = getClass().getResource("/fxml/carte_list.fxml");
            if (resource == null) {
                messageLabel.setText("carte_list.fxml introuvable");
                return;
            }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) portefeuillesFlow.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 800);

            // Add CSS if they exist
            try {
                scene.getStylesheets().add(getClass().getResource("/styles/colors.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            } catch (Exception e) {
                // CSS files might not exist, ignore
            }

            stage.setScene(scene);
            stage.setTitle("FinTrack - Cartes");
        } catch (IOException e) {
            messageLabel.setText("Erreur navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openTransactionsView() {
        try {
            var resource = getClass().getResource("/fxml/transactions.fxml");
            if (resource == null) { messageLabel.setText("transactions.fxml introuvable"); return; }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) portefeuillesFlow.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/styles/colors.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("FinTrack - Transactions");
        } catch (IOException e) {
            messageLabel.setText("Erreur navigation: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeconnexion() {
        Session.clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) portefeuillesFlow.getScene().getWindow();
            Scene scene = new Scene(root, 600, 450);
            scene.getStylesheets().add(getClass().getResource("/styles/colors.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("FinTrack - Connexion");
        } catch (IOException e) {
            messageLabel.setText("Erreur déconnexion: " + e.getMessage());
        }
    }
}
