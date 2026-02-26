package Controllers;

import Models.*;
import Services.CarteVirtuelleService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CarteFormController {

    @FXML private Label titleLabel;
    @FXML private ComboBox<String> comboType;
    @FXML private ComboBox<String> comboDevise;
    @FXML private TextField txtSolde;
    @FXML private TextField txtPlafond;

    private CarteVirtuelleService carteService = new CarteVirtuelleService();
    private Portefeuille portefeuille;
    private CarteVirtuelle carte;

    public CarteFormController() {}

    @FXML
    public void initialize() {
        comboType.setItems(FXCollections.observableArrayList("NORMAL", "GOLD", "SILVER"));
        comboDevise.setItems(FXCollections.observableArrayList("DT", "USD", "EUR"));
        if (carte != null) {
            titleLabel.setText("Modifier Carte");
            comboType.setValue(carte.getType().toString());
            comboDevise.setValue(carte.getDevise().toString());
            txtSolde.setText(String.valueOf(carte.getSolde()));
            txtPlafond.setText(String.valueOf(carte.getPlafond()));
        }
    }

    public void setPortefeuille(Portefeuille p) { this.portefeuille = p; }
    public void setCarte(CarteVirtuelle c) { this.carte = c; }

    @FXML
    private void handleEnregistrer() {
        String typeStr = comboType.getValue();
        String deviseStr = comboDevise.getValue();
        String soldeText = txtSolde.getText().trim();
        String plafondText = txtPlafond.getText().trim();

        if (typeStr == null) { showAlert("Veuillez sélectionner un type de carte."); return; }
        if (deviseStr == null) { showAlert("Veuillez sélectionner une devise."); return; }
        if (soldeText.isEmpty()) { showAlert("Le solde initial est requis."); return; }
        if (plafondText.isEmpty()) { showAlert("Le plafond est requis."); return; }

        double solde, plafond;
        try {
            solde = Double.parseDouble(soldeText);
            if (solde < 0) throw new NumberFormatException("Solde négatif");
        } catch (NumberFormatException e) {
            showAlert("Solde invalide. Veuillez entrer un nombre positif.");
            return;
        }
        try {
            plafond = Double.parseDouble(plafondText);
            if (plafond <= 0) throw new NumberFormatException("Plafond <=0");
            if (plafond < solde) { showAlert("Le plafond ne peut pas être inférieur au solde."); return; }
        } catch (NumberFormatException e) {
            showAlert("Plafond invalide. Veuillez entrer un nombre > 0.");
            return;
        }

        TypeCarte type = TypeCarte.valueOf(typeStr);
        Devise devise = Devise.valueOf(deviseStr);

        if (carte == null) {
            CarteVirtuelle nouvelle = new CarteVirtuelle(solde, plafond, type, devise, portefeuille.getId());
            carteService.ajouter(nouvelle);
        } else {
            carte.setType(type); carte.setDevise(devise); carte.setSolde(solde); carte.setPlafond(plafond);
            carteService.modifier(carte);
        }
        fermer();
    }

    @FXML private void handleAnnuler() { fermer(); }
    private void fermer() { ((Stage) comboType.getScene().getWindow()).close(); }
    private void showAlert(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}