package Controllers;

import Models.Compte;
import Services.ServiceCompte;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AjouterCompteController {

    @FXML private TextField txtNumero, txtSolde, txtTaux, txtDecouvert;
    @FXML private ComboBox<String> comboType;
    @FXML private VBox boxTaux, boxDecouvert;

    private ServiceCompte service = new ServiceCompte();

    @FXML
    public void initialize() {
        comboType.setItems(FXCollections.observableArrayList("EPARGNE", "COURANT"));

        comboType.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isEpargne = "EPARGNE".equals(newVal);
            boxTaux.setVisible(isEpargne);
            boxTaux.setManaged(isEpargne);
            boxDecouvert.setVisible(!isEpargne);
            boxDecouvert.setManaged(!isEpargne);
        });
    }

    private boolean validerSaisie() {
        StringBuilder erreurs = new StringBuilder();

        if (txtNumero.getText().trim().isEmpty()) {
            erreurs.append("- Le numéro de compte est obligatoire.\n");
        }

        try {
            double solde = Double.parseDouble(txtSolde.getText().replace(",", "."));
            if (solde < 0) erreurs.append("- Le solde initial ne peut pas être négatif.\n");
        } catch (NumberFormatException e) {
            erreurs.append("- Le solde doit être un nombre valide.\n");
        }

        if (comboType.getValue() == null) {
            erreurs.append("- Veuillez sélectionner un type de compte.\n");
        } else {
            if ("EPARGNE".equals(comboType.getValue())) {
                try {
                    double taux = Double.parseDouble(txtTaux.getText().replace(",", "."));
                    if (taux < 0 || taux > 100) erreurs.append("- Le taux doit être entre 0 et 100%.\n");
                } catch (NumberFormatException e) {
                    erreurs.append("- Le taux d'intérêt doit être un nombre.\n");
                }
            } else {
                try {
                    double decouvert = Double.parseDouble(txtDecouvert.getText().replace(",", "."));
                    if (decouvert < 0) erreurs.append("- Le plafond de découvert doit être positif.\n");
                } catch (NumberFormatException e) {
                    erreurs.append("- Le plafond de découvert doit être un nombre.\n");
                }
            }
        }

        if (erreurs.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Données invalides");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(erreurs.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    @FXML
    private void handleEnregistrer() {
        if (validerSaisie()) {
            try {
                Compte c = new Compte();
                c.setNumeroCompte(txtNumero.getText().trim());
                c.setSolde(Double.parseDouble(txtSolde.getText().replace(",", ".")));
                c.setTypeCompte(comboType.getValue());
                c.setEtat("ACTIF");
                c.setDateCreation(LocalDate.now());

                if ("EPARGNE".equals(c.getTypeCompte())) {
                    c.setTauxInteret(Double.parseDouble(txtTaux.getText().replace(",", ".")));
                } else {
                    c.setPlafondDecouvert(Double.parseDouble(txtDecouvert.getText().replace(",", ".")));
                }

                service.ajouter(c);
                handleAnnuler();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement : " + e.getMessage());
                alert.show();
            }
        }
    }

    @FXML
    private void handleAnnuler() {
        Stage stage = (Stage) txtNumero.getScene().getWindow();
        stage.close();
    }
}