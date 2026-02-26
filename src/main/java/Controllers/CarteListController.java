package Controllers;

import Models.CarteVirtuelle;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import Models.Portefeuille;
import Models.TypeCarte; // ← IMPORT AJOUTÉ
import Services.CarteVirtuelleService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CarteListController {

    @FXML private Label lblInfosPortefeuille;
    @FXML private Label lblNbCartes, lblSoldeTotal, lblPlafondTotal;
    @FXML private BarChart<String, Number> barChartTypes;
    @FXML private FlowPane cartesFlow;

    private Portefeuille portefeuille;
    private CarteVirtuelleService carteService = new CarteVirtuelleService();
    private ObservableList<CarteVirtuelle> cartes = FXCollections.observableArrayList();

    public void setPortefeuille(Portefeuille p) {
        this.portefeuille = p;
        lblInfosPortefeuille.setText(p.getNom() + " - " + p.getDevisePrincipale());
        loadCartes();
    }

    private void loadCartes() {
        cartes.setAll(carteService.getCartesByPortefeuille(portefeuille.getId()));
        updateStats();
        displayCartes();
    }

    private void updateStats() {
        lblNbCartes.setText(String.valueOf(cartes.size()));
        double soldeTotal = cartes.stream().mapToDouble(CarteVirtuelle::getSolde).sum();
        double plafondTotal = cartes.stream().mapToDouble(CarteVirtuelle::getPlafond).sum();
        lblSoldeTotal.setText(String.format("%.2f DT", soldeTotal));
        lblPlafondTotal.setText(String.format("%.2f DT", plafondTotal));

        Map<String, Long> typeCount = cartes.stream()
                .collect(Collectors.groupingBy(c -> c.getType().toString(), Collectors.counting()));
        barChartTypes.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        typeCount.forEach((type, count) -> series.getData().add(new XYChart.Data<>(type, count)));
        barChartTypes.getData().add(series);
    }

    private void displayCartes() {
        cartesFlow.getChildren().clear();
        for (CarteVirtuelle c : cartes) {
            VBox card = createCarteCard(c);
            cartesFlow.getChildren().add(card);
        }
    }

    private VBox createCarteCard(CarteVirtuelle c) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("carte-card", getCardStyleClass(c.getType()));

        Label typeLabel = new Label(c.getType().toString());
        typeLabel.getStyleClass().add("card-type");

        Label numeroLabel = new Label(masquerNumero(c.getNumeroCarte()));
        numeroLabel.getStyleClass().add("card-number");

        Label soldeLabel = new Label(String.format("%.2f %s", c.getSolde(), c.getDevise()));
        soldeLabel.getStyleClass().add("card-balance");

        Label plafondLabel = new Label("Plafond: " + String.format("%.2f %s", c.getPlafond(), c.getDevise()));
        plafondLabel.getStyleClass().add("card-limit");

        Label statutLabel = new Label(c.isActiver() ? "ACTIF" : "INACTIF");
        statutLabel.getStyleClass().add("card-status");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("✏️");
        editBtn.getStyleClass().add("btn-action-edit");
        editBtn.setOnAction(e -> ouvrirFormulaireCarte(c));

        Button deleteBtn = new Button("🗑️");
        deleteBtn.getStyleClass().add("btn-action-delete");
        deleteBtn.setOnAction(e -> confirmerSuppression(c));

        actions.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(typeLabel, numeroLabel, soldeLabel, plafondLabel, statutLabel, actions);
        return card;
    }

    private String getCardStyleClass(TypeCarte type) {
        switch (type) {
            case GOLD: return "carte-card-gold";
            case SILVER: return "carte-card-silver";
            default: return "carte-card-normal";
        }
    }

    private String masquerNumero(String numero) {
        if (numero == null || numero.length() < 12) return "****";
        return "**** **** **** " + numero.substring(numero.length() - 4);
    }

    private void ouvrirFormulaireCarte(CarteVirtuelle c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/carte_form.fxml"));
            Parent root = loader.load();
            CarteFormController controller = loader.getController();
            controller.setPortefeuille(portefeuille);
            controller.setCarte(c);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadCartes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void confirmerSuppression(CarteVirtuelle c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette carte ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                carteService.supprimer(c.getId());
                loadCartes();
            }
        });
    }

    @FXML
    private void handleAjouterCarte() {
        ouvrirFormulaireCarte(null);
    }

    @FXML
    private void handleRetour() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/client_dashboard.fxml"));
            Stage stage = (Stage) lblInfosPortefeuille.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}