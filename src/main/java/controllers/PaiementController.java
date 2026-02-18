package controllers;

import models.*;
import services.ServicePaiement;
import services.ServiceService;
import services.ServiceProduit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.geometry.Pos;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PaiementController implements Initializable {

    @FXML private FlowPane paiementsFlow;
    @FXML private ComboBox<String> periodeCombo;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private Text totalPaiements;
    @FXML private Text nbPaiements;

    private ServicePaiement servicePaiement;
    private ServiceService serviceService;
    private ServiceProduit produitService;
    private ObservableList<Paiement> paiementList;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        servicePaiement = new ServicePaiement();
        serviceService = new ServiceService();
        produitService = new ServiceProduit();
        paiementList = FXCollections.observableArrayList();

        setupFilters();
        loadPaiements();
    }

    private void setupFilters() {
        periodeCombo.setItems(FXCollections.observableArrayList(
                "Aujourd'hui", "Cette semaine", "Ce mois", "Cette année", "Personnalisé"
        ));

        periodeCombo.setOnAction(e -> {
            if ("Personnalisé".equals(periodeCombo.getValue())) {
                dateDebut.setDisable(false);
                dateFin.setDisable(false);
            } else {
                dateDebut.setDisable(true);
                dateFin.setDisable(true);
                filterByPeriode(periodeCombo.getValue());
            }
        });

        dateDebut.setOnAction(e -> filterByDate());
        dateFin.setOnAction(e -> filterByDate());
    }

    private void filterByPeriode(String periode) {
        LocalDate debut = null;
        LocalDate fin = LocalDate.now();

        switch (periode) {
            case "Aujourd'hui":
                debut = fin;
                break;
            case "Cette semaine":
                debut = fin.minusDays(fin.getDayOfWeek().getValue() - 1);
                break;
            case "Ce mois":
                debut = fin.withDayOfMonth(1);
                break;
            case "Cette année":
                debut = fin.withDayOfYear(1);
                break;
            default:
                return;
        }

        try {
            LocalDate finalDebut = debut;
            List<Paiement> filtered = servicePaiement.recupererTous().stream()
                    .filter(p -> !p.getDatePaiement().isBefore(finalDebut) && !p.getDatePaiement().isAfter(fin))
                    .toList();
            displayPaiements(filtered);
            updateTotaux(filtered);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterByDate() {
        if (dateDebut.getValue() != null && dateFin.getValue() != null) {
            try {
                List<Paiement> filtered = servicePaiement.recupererTous().stream()
                        .filter(p -> !p.getDatePaiement().isBefore(dateDebut.getValue())
                                && !p.getDatePaiement().isAfter(dateFin.getValue()))
                        .toList();
                displayPaiements(filtered);
                updateTotaux(filtered);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadPaiements() {
        try {
            List<Paiement> paiements = servicePaiement.recupererTous();
            paiementList.setAll(paiements);
            displayPaiements(paiements);
            updateTotaux(paiements);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les paiements: " + e.getMessage());
        }
    }

    private void displayPaiements(List<Paiement> paiements) {
        paiementsFlow.getChildren().clear();

        for (Paiement p : paiements) {
            VBox card = createPaiementCard(p);
            paiementsFlow.getChildren().add(card);
        }
    }

    private void updateTotaux(List<Paiement> paiements) {
        BigDecimal total = paiements.stream()
                .filter(p -> p.getStatut() == Paiement.StatutPaiement.effectue)
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalPaiements.setText(total + " TND");
        nbPaiements.setText(paiements.size() + " paiement(s)");
    }

    private VBox createPaiementCard(Paiement p) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15;");
        card.setPadding(new Insets(15));
        card.setPrefWidth(300);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setColor(Color.rgb(24, 45, 136, 0.1));
        card.setEffect(shadow);

        // Référence
        HBox refBox = new HBox(10);
        refBox.setAlignment(Pos.CENTER_LEFT);

        Circle icon = new Circle(8);
        icon.setFill(Color.web("#182d88"));

        Text refText = new Text(p.getReferenceTransaction());
        refText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: #182d88;");

        refBox.getChildren().addAll(icon, refText);

        // Montant
        HBox montantBox = new HBox(10);
        montantBox.setAlignment(Pos.CENTER_LEFT);

        Text montantIcon = new Text("💰");
        montantIcon.setStyle("-fx-font-size: 18px;");

        Text montantText = new Text(p.getMontant() + " TND");
        montantText.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-fill: #182d88;");

        montantBox.getChildren().addAll(montantIcon, montantText);

        // Détails
        GridPane details = new GridPane();
        details.setHgap(10);
        details.setVgap(5);

        Text dateLabel = new Text("Date:");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-fill: #182d88;");

        Text dateValue = new Text(p.getDatePaiement().format(dateFormatter));
        dateValue.setStyle("-fx-font-size: 12px; -fx-fill: #182d88; -fx-opacity: 0.7;");

        Text modeLabel = new Text("Mode:");
        modeLabel.setStyle("-fx-font-size: 12px; -fx-fill: #182d88;");

        Text modeValue = new Text(formatModePaiement(p.getModePaiement()));
        modeValue.setStyle("-fx-font-size: 12px; -fx-fill: #182d88; -fx-opacity: 0.7;");

        details.add(dateLabel, 0, 0);
        details.add(dateValue, 1, 0);
        details.add(modeLabel, 0, 1);
        details.add(modeValue, 1, 1);

        // Association (Service/Produit)
        VBox assocBox = new VBox(5);
        assocBox.setStyle("-fx-background-color: #d2e7fe; -fx-background-radius: 10; -fx-padding: 10;");

        Text assocTitle = new Text();
        assocTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: #182d88;");

        Text assocDetail = new Text();
        assocDetail.setStyle("-fx-font-size: 11px; -fx-fill: #182d88; -fx-opacity: 0.7;");

        if (p.getIdService() != null && p.getService() != null) {
            assocTitle.setText("Service associé");
            assocDetail.setText(p.getService().getNomService());
        } else if (p.getIdProduit() != null && p.getProduit() != null) {
            assocTitle.setText("Produit associé");
            assocDetail.setText(p.getProduit().getNomProduit() + " - " + p.getProduit().getCodeUnique());
        } else {
            assocBox.setVisible(false);
        }

        assocBox.getChildren().addAll(assocTitle, assocDetail);

        // Statut
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Circle statusDot = new Circle(5);
        Text statusText = new Text(formatStatut(p.getStatut()));
        statusText.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        switch (p.getStatut()) {
            case effectue:
                statusDot.setFill(Color.web("#8dbc71"));
                statusText.setFill(Color.web("#8dbc71"));
                break;
            case en_attente:
                statusDot.setFill(Color.web("#fecf47"));
                statusText.setFill(Color.web("#fecf47"));
                break;
            case echoue:
            case rembourse:
                statusDot.setFill(Color.web("#f78f34"));
                statusText.setFill(Color.web("#f78f34"));
                break;
        }

        statusBox.getChildren().addAll(statusDot, statusText);

        card.getChildren().addAll(refBox, montantBox, details, assocBox, statusBox);
        return card;
    }

    private String formatModePaiement(Paiement.ModePaiement mode) {
        switch (mode) {
            case carte_bancaire: return "Carte bancaire";
            case carte_cadeau: return "Carte cadeau";
            case carte_abonnement: return "Carte d'abonnement";
            case carte_prepayee: return "Carte prépayée";
            case especes: return "Espèces";
            default: return mode.toString();
        }
    }

    private String formatStatut(Paiement.StatutPaiement statut) {
        switch (statut) {
            case effectue: return "Effectué";
            case en_attente: return "En attente";
            case echoue: return "Échoué";
            case rembourse: return "Remboursé";
            default: return statut.toString();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}