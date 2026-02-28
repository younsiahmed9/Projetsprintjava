package controllers;

import models.Produit;
import services.ServiceProduit;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.sql.SQLException;

public class ModifierProduitController {
    @FXML private TextField nomField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField montantField;
    @FXML private TextField codeField;
    @FXML private ComboBox<String> statutCombo;

    private Produit produit;
    private Stage dialogStage;
    private boolean okClicked = false;
    private ServiceProduit produitService = new ServiceProduit();

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList("carte_cadeau", "carte_abonnement", "carte_prepayee"));
        statutCombo.setItems(FXCollections.observableArrayList("disponible", "vendu", "expire"));
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        if (produit != null) {
            nomField.setText(produit.getNomProduit());
            typeCombo.setValue(produit.getTypeProduit());
            montantField.setText(produit.getMontant().toString());
            codeField.setText(produit.getCodeUnique());
            statutCombo.setValue(produit.getStatut());
        }
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            try {
                produit.setNomProduit(nomField.getText());
                produit.setTypeProduit(typeCombo.getValue());
                produit.setMontant(new BigDecimal(montantField.getText()));
                produit.setCodeUnique(codeField.getText());
                produit.setStatut(statutCombo.getValue());
                produitService.modifier(produit);
                okClicked = true;
                dialogStage.close();
            } catch (SQLException | NumberFormatException e) {
                showAlert("Erreur", e.getMessage());
            }
        }
    }

    private boolean isInputValid() {
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) return false;
        if (typeCombo.getValue() == null) return false;
        try { new BigDecimal(montantField.getText()); } catch (NumberFormatException e) { return false; }
        if (codeField.getText() == null || codeField.getText().trim().isEmpty()) return false;
        if (statutCombo.getValue() == null) return false;
        return true;
    }

    @FXML private void handleCancel() { dialogStage.close(); }
    public void setDialogStage(Stage stage) { this.dialogStage = stage; }
    public boolean isOkClicked() { return okClicked; }
    private void showAlert(String title, String msg) { new Alert(Alert.AlertType.ERROR, msg).show(); }
}