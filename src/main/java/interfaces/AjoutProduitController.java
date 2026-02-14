package interfaces;

import models.Produit;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AjoutProduitController {

    @FXML private TextField nomField;
    @FXML private ComboBox<Produit.TypeProduit> typeCombo;
    @FXML private TextField montantField;
    @FXML private TextField codeField;
    @FXML private ComboBox<Produit.StatutProduit> statutCombo;

    private Produit produit;

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList(Produit.TypeProduit.values()));
        statutCombo.setItems(FXCollections.observableArrayList(Produit.StatutProduit.values()));
        statutCombo.setValue(Produit.StatutProduit.disponible);
    }

    public Produit getProduit() {
        try {
            Produit p = new Produit();
            p.setNomProduit(nomField.getText());
            p.setTypeProduit(typeCombo.getValue());
            p.setMontant(new BigDecimal(montantField.getText()));
            p.setCodeUnique(codeField.getText());
            p.setStatut(statutCombo.getValue());
            p.setDateCreation(LocalDate.now());
            return p;
        } catch (Exception e) {
            return null;
        }
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
}