package controllers;

import models.Service;
import models.Produit;
import services.ServiceService;
import services.ServiceProduit;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ServiceDetailsController implements Initializable {

    @FXML private Text idValue;
    @FXML private Text nomValue;
    @FXML private Text typeValue;
    @FXML private Text tarifValue;
    @FXML private Text frequenceValue;
    @FXML private Text dateDebutValue;
    @FXML private Text dateFinValue;
    @FXML private Text statutValue;
    @FXML private Text produitLieValue;
    @FXML private Text produitCodeValue;
    @FXML private Label statutLabel;

    @FXML private Button modifierBtn;
    @FXML private Button fermerBtn;

    private Service service;
    private ServiceService serviceService;
    private ServiceProduit produitService;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        serviceService = new ServiceService();
        produitService = new ServiceProduit();
    }

    public void setService(Service service) {
        this.service = service;
        afficherDetails();
    }

    private void afficherDetails() {
        if (service == null) return;

        idValue.setText(String.valueOf(service.getIdService()));
        nomValue.setText(service.getNomService());
        typeValue.setText(service.getTypeService().toString());
        tarifValue.setText(service.getTarif() + " TND");

        if (service.getFrequence() != null) {
            frequenceValue.setText(service.getFrequence().toString());
        } else {
            frequenceValue.setText("Non applicable");
        }

        if (service.getDateDebut() != null) {
            dateDebutValue.setText(service.getDateDebut().format(dateFormatter));
        } else {
            dateDebutValue.setText("Non définie");
        }

        if (service.getDateFin() != null) {
            dateFinValue.setText(service.getDateFin().format(dateFormatter));
        } else {
            dateFinValue.setText("Non définie");
        }

        statutValue.setText(service.getStatut().toString());

        // Changer la couleur du statut
        switch (service.getStatut()) {
            case actif:
                statutLabel.setStyle("-fx-background-color: #8dbc71; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;");
                statutLabel.setText("ACTIF");
                break;
            case suspendu:
                statutLabel.setStyle("-fx-background-color: #fecf47; -fx-text-fill: #182d88; -fx-padding: 5 10; -fx-background-radius: 5;");
                statutLabel.setText("SUSPENDU");
                break;
            case expire:
                statutLabel.setStyle("-fx-background-color: #f78f34; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;");
                statutLabel.setText("EXPIRÉ");
                break;
        }

        // ✅ CORRECTION ICI - Vérification explicite avec Integer
        Integer idProduit = service.getIdProduit();
        if (idProduit != null) {
            try {
                Produit produit = produitService.getById(idProduit);
                if (produit != null) {
                    produitLieValue.setText(produit.getNomProduit());
                    produitCodeValue.setText(produit.getCodeUnique());
                } else {
                    produitLieValue.setText("Aucun");
                    produitCodeValue.setText("");
                }
            } catch (SQLException e) {
                produitLieValue.setText("Erreur chargement");
                e.printStackTrace();
            }
        } else {
            produitLieValue.setText("Aucun");
            produitCodeValue.setText("");
        }
    }

    @FXML
    private void handleModifier() {
        // Implémenter la modification
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("Fonctionnalité de modification à venir");
        alert.showAndWait();
    }

    @FXML
    private void handleFermer() {
        // Fermer la fenêtre
        fermerBtn.getScene().getWindow().hide();
    }
}