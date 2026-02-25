package Controllers;

import Models.Compte;
import Services.ServiceCompte;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLDataException;

public class ModifierCompteController {

    @FXML private TextField txtNumero, txtSolde, txtPlafond, txtTaux;
    @FXML private ComboBox<String> comboEtat;
    @FXML private VBox boxPlafond, boxTaux;

    private Compte compteActuel;
    private ServiceCompte service = new ServiceCompte();
    private boolean modificationReussie = false;

    @FXML
    public void initialize() {
        comboEtat.getItems().addAll("ACTIF", "BLOQUE");
    }

    public void setCompte(Compte compte) {
        this.compteActuel = compte;

        txtNumero.setText(compte.getNumeroCompte());
        txtSolde.setText(String.valueOf(compte.getSolde()));
        comboEtat.setValue(compte.getEtat());

        if ("EPARGNE".equalsIgnoreCase(compte.getTypeCompte())) {
            boxPlafond.setVisible(false);
            boxPlafond.setManaged(false);
            txtTaux.setText(String.valueOf(compte.getTauxInteret()));
        } else { // COURANT
            boxTaux.setVisible(false);
            boxTaux.setManaged(false);
            txtPlafond.setText(String.valueOf(compte.getPlafondDecouvert()));
        }
    }

    private boolean validerSaisie() {
        StringBuilder erreurs = new StringBuilder();

        if (txtNumero.getText() == null || txtNumero.getText().trim().isEmpty()) {
            erreurs.append("- Le numéro de compte ne peut pas être vide.\n");
        }

        try {
            double solde = Double.parseDouble(txtSolde.getText().replace(",", "."));
            if ("EPARGNE".equalsIgnoreCase(compteActuel.getTypeCompte()) && solde < 0) {
                erreurs.append("- Un compte épargne ne peut pas avoir un solde négatif.\n");
            }
        } catch (NumberFormatException e) {
            erreurs.append("- Le format du solde est invalide.\n");
        }

        if (comboEtat.getValue() == null) {
            erreurs.append("- Veuillez sélectionner un état (ACTIF/BLOQUE).\n");
        }

        if ("EPARGNE".equalsIgnoreCase(compteActuel.getTypeCompte())) {
            try {
                double taux = Double.parseDouble(txtTaux.getText().replace(",", "."));
                if (taux < 0 || taux > 100) erreurs.append("- Le taux d'intérêt doit être entre 0 et 100%.\n");
            } catch (NumberFormatException e) {
                erreurs.append("- Le format du taux d'intérêt est invalide.\n");
            }
        } else {
            try {
                double plafond = Double.parseDouble(txtPlafond.getText().replace(",", "."));
                if (plafond < 0) erreurs.append("- Le plafond de découvert doit être positif.\n");
            } catch (NumberFormatException e) {
                erreurs.append("- Le format du plafond de découvert est invalide.\n");
            }
        }

        if (erreurs.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Modification");
            alert.setHeaderText("Données incorrectes détectées");
            alert.setContentText(erreurs.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }

    public boolean isModificationReussie() { return modificationReussie; }

    @FXML
    private void handleEnregistrer() {
        if (validerSaisie()) {
            try {
                compteActuel.setNumeroCompte(txtNumero.getText().trim());
                compteActuel.setSolde(Double.parseDouble(txtSolde.getText().replace(",", ".")));
                compteActuel.setEtat(comboEtat.getValue());

                if ("EPARGNE".equalsIgnoreCase(compteActuel.getTypeCompte())) {
                    compteActuel.setTauxInteret(Double.parseDouble(txtTaux.getText().replace(",", ".")));
                } else {
                    compteActuel.setPlafondDecouvert(Double.parseDouble(txtPlafond.getText().replace(",", ".")));
                }

                service.modifier(compteActuel);

                modificationReussie = true;
                closeStage();

            } catch (SQLDataException e) {
                showError("Erreur SQL : " + e.getMessage());
            } catch (Exception e) {
                showError("Erreur inattendue : " + e.getMessage());
            }
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML private void handleAnnuler() { closeStage(); }

    private void closeStage() {
        Stage stage = (Stage) txtNumero.getScene().getWindow();
        stage.close();
    }
}