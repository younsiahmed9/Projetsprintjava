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

        // Remplissage des champs communs
        txtNumero.setText(compte.getNumeroCompte());
        txtSolde.setText(String.valueOf(compte.getSolde()));
        comboEtat.setValue(compte.getEtat());

        // Affichage selon le type de compte (String)
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

    public boolean isModificationReussie() { return modificationReussie; }

    @FXML
    private void handleEnregistrer() {
        try {
            // Mise à jour de l'objet local
            compteActuel.setNumeroCompte(txtNumero.getText());
            compteActuel.setSolde(Double.parseDouble(txtSolde.getText()));
            compteActuel.setEtat(comboEtat.getValue());

            if ("EPARGNE".equalsIgnoreCase(compteActuel.getTypeCompte())) {
                compteActuel.setTauxInteret(Double.parseDouble(txtTaux.getText()));
            } else {
                compteActuel.setPlafondDecouvert(Double.parseDouble(txtPlafond.getText()));
            }

            // Appel à ta méthode SQL
            service.modifier(compteActuel);

            modificationReussie = true;
            closeStage();
        } catch (NumberFormatException e) {
            System.err.println("Erreur de format numérique : " + e.getMessage());
        } catch (SQLDataException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleAnnuler() { closeStage(); }

    private void closeStage() {
        Stage stage = (Stage) txtNumero.getScene().getWindow();
        stage.close();
    }
}