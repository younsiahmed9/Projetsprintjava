package Controllers;

import Models.Compte;
import Services.ServiceCompte;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;

import java.time.LocalDate;

public class AjouterCompteController {

    @FXML private TextField txtNumero, txtSolde, txtTaux, txtDecouvert;
    @FXML private ComboBox<String> comboType;
    @FXML private VBox boxTaux, boxDecouvert;

    private ServiceCompte service = new ServiceCompte();

    @FXML
    public void initialize() {
        comboType.setItems(FXCollections.observableArrayList("EPARGNE", "COURANT"));

        // Gestion de l'affichage dynamique des champs
        comboType.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isEpargne = "EPARGNE".equals(newVal);
            boxTaux.setVisible(isEpargne);
            boxTaux.setManaged(isEpargne);
            boxDecouvert.setVisible(!isEpargne);
            boxDecouvert.setManaged(!isEpargne);
        });
    }

    @FXML
    private void handleEnregistrer() {
        try {
            Compte c = new Compte();
            c.setNumeroCompte(txtNumero.getText());
            c.setSolde(Double.parseDouble(txtSolde.getText()));
            c.setTypeCompte(comboType.getValue());
            c.setEtat("ACTIF");
            c.setDateCreation(LocalDate.now());

            if ("EPARGNE".equals(c.getTypeCompte())) {
                c.setTauxInteret(Double.parseDouble(txtTaux.getText()));
            } else {
                c.setPlafondDecouvert(Double.parseDouble(txtDecouvert.getText()));
            }

            service.ajouter(c);
            handleAnnuler(); // Fermer la fenêtre après succès
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur de saisie : " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void handleAnnuler() {
        Stage stage = (Stage) txtNumero.getScene().getWindow();
        stage.close();
    }
}