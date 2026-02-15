package Controllers;

import Models.Credit;
import Services.ServiceCredit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class AfficherCreditController {

    // --- ÉLÉMENTS DU DASHBOARD (Injections FXML) ---
    @FXML private Label lblInfosCompte, lblTotalEmprunt, lblTotalMensualite, lblNbCredits;
    @FXML private VBox containerCredits;
    @FXML private BarChart<String, Number> barChartCredits;

    private int idCompte;
    private ServiceCredit serviceCredit = new ServiceCredit();

    /**
     * Appelé par DetailCompteController pour initialiser la vue
     */
    public void setCompteData(int idCompte, String numCompte) {
        this.idCompte = idCompte;
        if (lblInfosCompte != null) {
            lblInfosCompte.setText("Compte N° " + numCompte);
        }
        chargerDonnees();
    }

    private void chargerDonnees() {
        if (containerCredits == null || barChartCredits == null) return;

        containerCredits.getChildren().clear();
        List<Credit> credits = serviceCredit.recupererParCompte(idCompte);

        double totalEmprunt = 0;
        double totalMens = 0;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Mensualités");

        int index = 1;
        for (Credit c : credits) {
            // Ajouter la ligne dans la liste
            containerCredits.getChildren().add(createCreditRow(c));

            totalEmprunt += c.getMontant();
            totalMens += c.getMensualite();

            // Ajouter au graphique
            String labelBarre = "Crédit " + index + " (" + (int)c.getMontant() + " DT)";
            series.getData().add(new XYChart.Data<>(labelBarre, c.getMensualite()));
            index++;
        }

        // Mise à jour des KPIs
        lblTotalEmprunt.setText(String.format("%.2f DT", totalEmprunt));
        lblTotalMensualite.setText(String.format("%.2f DT", totalMens));
        lblNbCredits.setText(String.valueOf(credits.size()));

        // Mise à jour du graphique
        barChartCredits.getData().clear();
        barChartCredits.getData().add(series);
    }

    private HBox createCreditRow(Credit c) {
        HBox row = new HBox(0);
        row.getStyleClass().add("custom-row-credit");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15, 25, 15, 25));
        row.setPrefHeight(80); // On fixe une hauteur pour éviter que ça se tasse

        // 1. Infos (Montant et Taux) - Largeur 300
        VBox infoBox = new VBox(5);
        infoBox.setMinWidth(300); infoBox.setPrefWidth(300); infoBox.setMaxWidth(300);
        Label mont = new Label(String.format("%.2f DT", c.getMontant()));
        mont.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: #182d88;");
        Label taux = new Label("Taux: " + c.getTauxInteret() + "%");
        taux.setStyle("-fx-text-fill: #999; -fx-font-size: 12;");
        infoBox.getChildren().addAll(mont, taux);

        // 2. Durée - Largeur 150
        // Assure-toi que c.getDureeMois() ne retourne pas 0
        Label duree = new Label(c.getDureeMois() + " Mois");
        duree.setMinWidth(150); duree.setPrefWidth(150); duree.setMaxWidth(150);
        duree.setAlignment(Pos.CENTER);
        duree.setStyle("-fx-font-weight: 500; -fx-text-fill: #444;");

        // 3. Mensualité - Largeur 200
        Label mens = new Label(String.format("%.2f DT", c.getMensualite()));
        mens.setMinWidth(200); mens.setPrefWidth(200); mens.setMaxWidth(200);
        mens.setAlignment(Pos.CENTER);
        mens.setStyle("-fx-text-fill: #3e893e; -fx-font-weight: bold;");

        // 4. Statut - Largeur 150
        // On gère le cas où le statut serait null
        String statutTexte = (c.getStatut() != null) ? c.getStatut().toUpperCase() : "EN ATTENTE";
        Label status = new Label(statutTexte);
        status.getStyleClass().add("status-badge");

        // Application dynamique du style de badge
        if(statutTexte.contains("ACTIF") || statutTexte.contains("COURS")) {
            status.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-padding: 5 10; -fx-background-radius: 15;");
        } else {
            status.setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00; -fx-padding: 5 10; -fx-background-radius: 15;");
        }

        StackPane stCont = new StackPane(status);
        stCont.setMinWidth(150); stCont.setPrefWidth(150); stCont.setMaxWidth(150);
        stCont.setAlignment(Pos.CENTER);

        // Espaceur
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 5. Actions (Modifier + Supprimer) - Largeur 120
        HBox actions = new HBox(15);
        actions.setMinWidth(120); actions.setPrefWidth(120); actions.setMaxWidth(120);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnEdit = new Button("✎"); // Icône modifier
        btnEdit.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-cursor: hand; -fx-font-size: 16; -fx-background-radius: 5;");
        btnEdit.setOnAction(e -> handleModifierCredit(c));

        Button btnDel = new Button("🗑"); // Icône supprimer
        btnDel.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #d32f2f; -fx-cursor: hand; -fx-font-size: 16; -fx-background-radius: 5;");
        btnDel.setOnAction(e -> handleSupprimerCredit(c));

        actions.getChildren().addAll(btnEdit, btnDel);

        // AJOUTER TOUS LES ÉLÉMENTS DANS LA LIGNE
        row.getChildren().addAll(infoBox, duree, mens, stCont, spacer, actions);

        return row;
    }

    @FXML
    private void handleSupprimerCredit(Credit c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SupprimerCredit.fxml"));
            VBox popup = loader.load();

            popup.setMaxWidth(400);
            popup.setMaxHeight(Region.USE_PREF_SIZE);
            StackPane.setAlignment(popup, javafx.geometry.Pos.CENTER);

            Region overlay = new Region();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
            overlay.setPickOnBounds(true);

            SupprimerCreditController controller = loader.getController();
            // On passe le crédit, le stackpane parent et la fonction de rafraîchissement
            controller.setData(c, rootStackPane, overlay, this::chargerDonnees);

            rootStackPane.getChildren().addAll(overlay, popup);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModifierCredit(Credit c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifierCredit.fxml"));
            VBox popup = loader.load();

            // --- ASTUCE POUR LA TAILLE ---
            popup.setMaxWidth(400); // Largeur fixe
            popup.setMaxHeight(Region.USE_PREF_SIZE); // Hauteur ajustée au contenu uniquement

            StackPane.setAlignment(popup, Pos.CENTER);

            Region overlay = new Region();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

            ModifierCreditController controller = loader.getController();
            controller.setCreditData(c, rootStackPane, overlay, this::chargerDonnees);

            rootStackPane.getChildren().addAll(overlay, popup);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetour() {
        Stage stage = (Stage) lblInfosCompte.getScene().getWindow();
        stage.close();
    }

    @FXML private StackPane rootStackPane; // Injecté depuis le FXML ci-dessus

    @FXML
    private void handleAjouterCredit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjouterCredit.fxml"));
            VBox popup = loader.load();

            StackPane.setAlignment(popup, javafx.geometry.Pos.CENTER);
            StackPane.setMargin(popup, new javafx.geometry.Insets(50));

            Region overlay = new Region();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
            overlay.setPickOnBounds(true);

            AjouterCreditController controller = loader.getController();
            controller.setCompteInfo(this.idCompte, rootStackPane, overlay, popup);

            // --- SOLUTION PROBLÈME 2 : RAFRAÎCHISSEMENT ---
            controller.setOnCloseListener(() -> {
                chargerDonnees(); // Relance ton affichage complet (Tableau + KPIs + Chart)
            });

            rootStackPane.getChildren().addAll(overlay, popup);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}