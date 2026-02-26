package Controllers;

import Models.Portefeuille;
import Models.Session;
import Services.PortefeuilleService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class PortefeuilleFormController {

    @FXML private Label titleLabel;
    @FXML private TextField txtNom;
    @FXML private TextField txtSolde;
    @FXML private ComboBox<String> comboDevise;

    private PortefeuilleService portefeuilleService = new PortefeuilleService();
    private Portefeuille portefeuille;

    @FXML
    public void initialize() {
        comboDevise.setItems(FXCollections.observableArrayList("DT", "USD", "EUR"));
        if (portefeuille != null) {
            titleLabel.setText("Modifier Portefeuille");
            txtNom.setText(portefeuille.getNom());
            txtSolde.setText(String.valueOf(portefeuille.getSoldeTotal()));
            comboDevise.setValue(portefeuille.getDevisePrincipale());
        }
    }

    public void setPortefeuille(Portefeuille p) { this.portefeuille = p; }

    @FXML
    private void handleEnregistrer() {
        String nom = txtNom.getText().trim();
        String devise = comboDevise.getValue();
        String soldeText = txtSolde.getText().trim();

        if (nom.isEmpty()) { showAlert("Le nom du portefeuille est requis."); return; }
        if (devise == null) { showAlert("Veuillez sélectionner une devise."); return; }
        if (soldeText.isEmpty()) { showAlert("Le solde initial est requis."); return; }

        double solde;
        try {
            solde = Double.parseDouble(soldeText);
            if (solde < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Solde invalide. Veuillez entrer un nombre positif.");
            return;
        }

        if (portefeuille == null) {
            Portefeuille p = new Portefeuille(nom, solde, devise);
            portefeuilleService.ajouterPourUtilisateur(p, Session.getUtilisateur().getId());
        } else {
            portefeuille.setNom(nom);
            portefeuille.setSoldeTotal(solde);
            portefeuille.setDevisePrincipale(devise);
            portefeuilleService.modifier(portefeuille);
        }
        fermer();
    }

    @FXML private void handleAnnuler() { fermer(); }
    private void fermer() { ((Stage) txtNom.getScene().getWindow()).close(); }
    private void showAlert(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}