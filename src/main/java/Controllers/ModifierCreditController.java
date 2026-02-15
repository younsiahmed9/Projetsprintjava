package Controllers;

import Models.Credit;
import Services.ServiceCredit;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ModifierCreditController {

    @FXML private TextField txtMontantReadOnly, txtTaux, txtDuree;
    @FXML private Label lblMensualite;
    @FXML private ComboBox<String> comboStatut;
    @FXML private VBox mainContainer;

    private Credit currentCredit;
    private ServiceCredit service = new ServiceCredit();
    private StackPane parentStack;
    private Region overlay;
    private Runnable onRefresh;

    @FXML
    public void initialize() {
        comboStatut.setItems(FXCollections.observableArrayList("EN_COURS", "TERMINE", "REFUSE"));

        // Calcul automatique lors de la saisie
        txtTaux.textProperty().addListener((obs, old, val) -> calculerMensualite());
        txtDuree.textProperty().addListener((obs, old, val) -> calculerMensualite());
    }

    public void setCreditData(Credit c, StackPane stack, Region ov, Runnable refreshCallback) {
        this.currentCredit = c;
        this.parentStack = stack;
        this.overlay = ov;
        this.onRefresh = refreshCallback;

        // Remplissage des champs
        txtMontantReadOnly.setText(String.format("%.2f DT", c.getMontant()));
        txtTaux.setText(String.valueOf(c.getTauxInteret()));
        txtDuree.setText(String.valueOf(c.getDureeMois()));
        comboStatut.setValue(c.getStatut());
        calculerMensualite();
    }

    private void calculerMensualite() {
        try {
            double p = currentCredit.getMontant();
            double r = (Double.parseDouble(txtTaux.getText().replace(",", ".")) / 100) / 12;
            int n = Integer.parseInt(txtDuree.getText());
            if (n > 0 && r > 0) {
                double m = (p * r) / (1 - Math.pow(1 + r, -n));
                lblMensualite.setText(String.format("%.2f DT", m));
            }
        } catch (Exception e) {
            lblMensualite.setText("0.00 DT");
        }
    }

    @FXML
    private void handleModifier() {
        try {
            currentCredit.setTauxInteret(Double.parseDouble(txtTaux.getText().replace(",", ".")));
            currentCredit.setDureeMois(Integer.parseInt(txtDuree.getText()));
            currentCredit.setStatut(comboStatut.getValue());

            String mStr = lblMensualite.getText().replaceAll("[^0-9.,]", "").replace(",", ".");
            currentCredit.setMensualite(Double.parseDouble(mStr));

            service.modifier(currentCredit);
            handleAnnuler();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Veuillez vérifier vos saisies.");
            alert.show();
        }
    }

    @FXML
    private void handleAnnuler() {
        parentStack.getChildren().removeAll(overlay, mainContainer);
        if (onRefresh != null) onRefresh.run();
    }
}