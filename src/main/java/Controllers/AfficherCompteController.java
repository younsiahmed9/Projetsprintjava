package Controllers;

import Models.Compte;
import Services.ServiceCompte;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.SQLDataException;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherCompteController {

    @FXML private FlowPane containerCartes;
    @FXML private Button btnTous, btnCourant, btnEpargne;
    @FXML private TextField txtRecherche; // Ajouté pour la recherche

    private ServiceCompte service = new ServiceCompte();
    private List<Compte> listeComplete;

    @FXML
    public void initialize() {
        refreshData();

        // Ajout de la logique de recherche en temps réel
        if (txtRecherche != null) {
            txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
                handleRecherche(newValue);
            });
        }
    }

    private void refreshData() {
        try {
            listeComplete = service.recuperer();
            displayComptes(listeComplete);
        } catch (SQLDataException e) {
            e.printStackTrace();
        }
    }

    private void displayComptes(List<Compte> comptes) {
        containerCartes.getChildren().clear();
        if (comptes != null) {
            for (Compte c : comptes) {
                containerCartes.getChildren().add(createAccountCard(c));
            }
        }
    }

    // --- Logique de Recherche ---
    private void handleRecherche(String query) {
        if (query == null || query.trim().isEmpty()) {
            displayComptes(listeComplete); // On affiche tout si le champ est vide
        } else {
            String lowerCaseQuery = query.toLowerCase();
            List<Compte> resultats = listeComplete.stream()
                    .filter(c -> c.getNumeroCompte().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
            displayComptes(resultats);
        }
    }

    private VBox createAccountCard(Compte c) {
        VBox card = new VBox(12);
        card.getStyleClass().add("account-card");
        card.setPrefSize(300, 260);

        Label typeHeader = new Label(c.getTypeCompte().toUpperCase());
        typeHeader.getStyleClass().add("card-type-header");

        Label numCompte = new Label("N° " + c.getNumeroCompte());
        numCompte.setStyle("-fx-font-weight: bold; -fx-font-size: 17; -fx-text-fill: #444;");

        Label solde = new Label(String.format("%.2f DT", c.getSolde()));
        solde.getStyleClass().add("card-solde-text");

        Label etat = new Label(c.getEtat());
        etat.setStyle("-fx-background-color: " + (c.getEtat().equalsIgnoreCase("ACTIF") ? "#E8F5E9;" : "#FFEBEE;") +
                "-fx-text-fill: " + (c.getEtat().equalsIgnoreCase("ACTIF") ? "#2E7D32;" : "#C62828;") +
                "-fx-padding: 5 15; -fx-background-radius: 10; -fx-font-size: 12;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnDetails = new Button("Détails >");
        btnDetails.getStyleClass().add("btn-details-orange");
        btnDetails.setOnAction(e -> ouvrirDetails(c.getIdCompte()));

        card.getChildren().addAll(typeHeader, numCompte, solde, etat, spacer, btnDetails);
        return card;
    }

    // --- Logique de Filtrage ---
    @FXML
    void filterTous() {
        updateFilterStyle(btnTous);
        handleRecherche(txtRecherche.getText()); // Garde la recherche active lors du changement de filtre
    }

    @FXML
    void filterCourant() {
        updateFilterStyle(btnCourant);
        List<Compte> filtres = listeComplete.stream()
                .filter(c -> c.getTypeCompte().equalsIgnoreCase("COURANT"))
                .filter(c -> c.getNumeroCompte().toLowerCase().contains(txtRecherche.getText().toLowerCase()))
                .collect(Collectors.toList());
        displayComptes(filtres);
    }

    @FXML
    void filterEpargne() {
        updateFilterStyle(btnEpargne);
        List<Compte> filtres = listeComplete.stream()
                .filter(c -> c.getTypeCompte().equalsIgnoreCase("EPARGNE"))
                .filter(c -> c.getNumeroCompte().toLowerCase().contains(txtRecherche.getText().toLowerCase()))
                .collect(Collectors.toList());
        displayComptes(filtres);
    }

    private void updateFilterStyle(Button active) {
        btnTous.getStyleClass().remove("filter-active");
        btnCourant.getStyleClass().remove("filter-active");
        btnEpargne.getStyleClass().remove("filter-active");
        active.getStyleClass().add("filter-active");
    }

    @FXML
    void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjouterCompte.fxml"));
            Parent popupRoot = loader.load();

            Pane overlay = new Pane();
            overlay.getStyleClass().add("overlay");

            Scene currentScene = containerCartes.getScene();
            StackPane rootStack = (StackPane) currentScene.getRoot();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.TRANSPARENT);
            popupStage.initOwner(currentScene.getWindow());

            Scene popupScene = new Scene(popupRoot);
            popupScene.setFill(null);
            popupStage.setScene(popupScene);

            rootStack.getChildren().add(overlay);
            popupStage.showAndWait();
            rootStack.getChildren().remove(overlay);
            refreshData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ouvrirDetails(int id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DetailCompte.fxml"));
            Parent root = loader.load();

            DetailCompteController controller = loader.getController();
            if (controller != null) {
                controller.setCompteId(id);
            }

            Scene currentScene = containerCartes.getScene();
            StackPane rootStack = (StackPane) currentScene.getRoot();
            Pane overlay = new Pane();
            overlay.getStyleClass().add("overlay");
            rootStack.getChildren().add(overlay);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(null);
            stage.setScene(scene);

            stage.showAndWait();
            rootStack.getChildren().remove(overlay);
            refreshData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}