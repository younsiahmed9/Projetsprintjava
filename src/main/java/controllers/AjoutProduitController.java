package controllers;

import models.Produit;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AjoutProduitController {

    @FXML private TextField nomField;
    @FXML private ComboBox<Produit.TypeProduit> typeCombo;
    @FXML private TextField montantField;
    @FXML private TextField codeField;
    @FXML private ComboBox<Produit.StatutProduit> statutCombo;

    @FXML private Label nomError;
    @FXML private Label typeError;
    @FXML private Label montantError;
    @FXML private Label codeError;
    @FXML private Label statutError;

    private Produit produit;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        // Initialiser les combobox
        typeCombo.setItems(FXCollections.observableArrayList(Produit.TypeProduit.values()));
        statutCombo.setItems(FXCollections.observableArrayList(Produit.StatutProduit.values()));
        statutCombo.setValue(Produit.StatutProduit.disponible);

        // Validation en temps réel
        setupRealTimeValidation();
    }

    private void setupRealTimeValidation() {
        // Validation du nom
        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                nomError.setText("Le nom est obligatoire");
                nomError.setTextFill(Color.RED);
            } else if (newVal.length() < 3) {
                nomError.setText("Le nom doit contenir au moins 3 caractères");
                nomError.setTextFill(Color.RED);
            } else {
                nomError.setText("✓");
                nomError.setTextFill(Color.GREEN);
            }
        });

        // Validation du montant
        montantField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                montantError.setText("Le montant est obligatoire");
                montantError.setTextFill(Color.RED);
            } else {
                try {
                    double montant = Double.parseDouble(newVal);
                    if (montant <= 0) {
                        montantError.setText("Le montant doit être > 0");
                        montantError.setTextFill(Color.RED);
                    } else if (montant > 1000000) {
                        montantError.setText("Montant trop élevé (max 1M)");
                        montantError.setTextFill(Color.RED);
                    } else {
                        montantError.setText("✓");
                        montantError.setTextFill(Color.GREEN);
                    }
                } catch (NumberFormatException e) {
                    montantError.setText("Format invalide");
                    montantError.setTextFill(Color.RED);
                }
            }
        });

        // Validation du code
        codeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                codeError.setText("Le code est obligatoire");
                codeError.setTextFill(Color.RED);
            } else if (newVal.length() < 5) {
                codeError.setText("Le code doit contenir au moins 5 caractères");
                codeError.setTextFill(Color.RED);
            } else if (!newVal.matches("^[A-Z0-9-]+$")) {
                codeError.setText("Uniquement lettres majuscules, chiffres et -");
                codeError.setTextFill(Color.RED);
            } else {
                codeError.setText("✓");
                codeError.setTextFill(Color.GREEN);
            }
        });

        // Validation du type
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                typeError.setText("Le type est obligatoire");
                typeError.setTextFill(Color.RED);
            } else {
                typeError.setText("✓");
                typeError.setTextFill(Color.GREEN);
            }
        });

        // Validation du statut
        statutCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                statutError.setText("Le statut est obligatoire");
                statutError.setTextFill(Color.RED);
            } else {
                statutError.setText("✓");
                statutError.setTextFill(Color.GREEN);
            }
        });
    }

    public boolean validateForm() {
        boolean isValid = true;

        // Validation nom
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            nomError.setText("Le nom est obligatoire");
            nomError.setTextFill(Color.RED);
            isValid = false;
        } else if (nomField.getText().length() < 3) {
            nomError.setText("Minimum 3 caractères");
            nomError.setTextFill(Color.RED);
            isValid = false;
        }

        // Validation type
        if (typeCombo.getValue() == null) {
            typeError.setText("Type obligatoire");
            typeError.setTextFill(Color.RED);
            isValid = false;
        }

        // Validation montant
        if (montantField.getText() == null || montantField.getText().trim().isEmpty()) {
            montantError.setText("Montant obligatoire");
            montantError.setTextFill(Color.RED);
            isValid = false;
        } else {
            try {
                double montant = Double.parseDouble(montantField.getText());
                if (montant <= 0) {
                    montantError.setText("Montant doit être > 0");
                    montantError.setTextFill(Color.RED);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                montantError.setText("Format invalide");
                montantError.setTextFill(Color.RED);
                isValid = false;
            }
        }

        // Validation code
        if (codeField.getText() == null || codeField.getText().trim().isEmpty()) {
            codeError.setText("Code obligatoire");
            codeError.setTextFill(Color.RED);
            isValid = false;
        } else if (codeField.getText().length() < 5) {
            codeError.setText("Minimum 5 caractères");
            codeError.setTextFill(Color.RED);
            isValid = false;
        } else if (!codeField.getText().matches("^[A-Z0-9-]+$")) {
            codeError.setText("Format invalide (ex: CODE-123)");
            codeError.setTextFill(Color.RED);
            isValid = false;
        }

        // Validation statut
        if (statutCombo.getValue() == null) {
            statutError.setText("Statut obligatoire");
            statutError.setTextFill(Color.RED);
            isValid = false;
        }

        return isValid;
    }

    public Produit getProduit() {
        if (!validateForm()) {
            return null;
        }

        try {
            Produit p = new Produit();
            p.setNomProduit(nomField.getText().trim());
            p.setTypeProduit(typeCombo.getValue());
            p.setMontant(new BigDecimal(montantField.getText().trim()));
            p.setCodeUnique(codeField.getText().trim().toUpperCase());
            p.setStatut(statutCombo.getValue());
            p.setDateCreation(LocalDate.now());
            return p;
        } catch (Exception e) {
            showAlert("Erreur", "Données invalides: " + e.getMessage());
            return null;
        }
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        this.isEditMode = true;

        if (produit != null) {
            nomField.setText(produit.getNomProduit());
            typeCombo.setValue(produit.getTypeProduit());
            montantField.setText(produit.getMontant().toString());
            codeField.setText(produit.getCodeUnique());
            statutCombo.setValue(produit.getStatut());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}