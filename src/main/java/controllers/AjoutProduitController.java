package controllers;

import models.Produit;
import services.ServiceProduit;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

public class AjoutProduitController {

    @FXML private TextField nomField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField montantField;
    @FXML private TextField codeUniqueField;
    @FXML private DatePicker dateCreationPicker;
    @FXML private ComboBox<String> statutCombo;

    @FXML private Label nomError;
    @FXML private Label typeError;
    @FXML private Label montantError;
    @FXML private Label codeError;
    @FXML private Label dateError;
    @FXML private Label statutError;
    @FXML private Button okButton;

    private final ServiceProduit produitService = new ServiceProduit();
    private Stage dialogStage;
    private Produit produit;
    private boolean okClicked = false;

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList(
                "carte_prepayee",
                "carte_abonnement",
                "carte_cadeau"
        ));

        statutCombo.setItems(FXCollections.observableArrayList(
                "disponible",
                "vendu",
                "expire"
        ));

        statutCombo.setValue("disponible");
        dateCreationPicker.setValue(LocalDate.now());

        okButton.setDisable(true);
        setupValidation();
    }

    private void setupValidation() {
        nomField.textProperty().addListener((obs, old, n) -> validateForm());
        montantField.textProperty().addListener((obs, old, n) -> validateForm());
        codeUniqueField.textProperty().addListener((obs, old, n) -> validateForm());
        typeCombo.valueProperty().addListener((obs, old, n) -> validateForm());
        statutCombo.valueProperty().addListener((obs, old, n) -> validateForm());
        dateCreationPicker.valueProperty().addListener((obs, o, n) -> validateForm());
    }

    private boolean validateNom() {
        String text = nomField.getText();
        if (text == null || text.trim().isEmpty()) {
            nomError.setText("Nom obligatoire");
            return false;
        } else if (text.length() < 3) {
            nomError.setText("Min 3 caractères");
            return false;
        } else {
            nomError.setText("✓");
            return true;
        }
    }

    private boolean validateMontant() {
        String text = montantField.getText();
        if (text == null || text.trim().isEmpty()) {
            montantError.setText("Montant obligatoire");
            return false;
        } else {
            try {
                BigDecimal montant = new BigDecimal(text.trim());
                if (montant.signum() <= 0) {
                    montantError.setText("Montant > 0");
                    return false;
                } else {
                    montantError.setText("✓");
                    return true;
                }
            } catch (NumberFormatException e) {
                montantError.setText("Format invalide");
                return false;
            }
        }
    }

    private boolean validateCode() {
        String text = codeUniqueField.getText();
        if (text == null || text.trim().isEmpty()) {
            codeError.setText("Code obligatoire");
            return false;
        } else if (text.trim().length() < 3) {
            codeError.setText("Min 3 caractères");
            return false;
        } else {
            codeError.setText("✓");
            return true;
        }
    }

    private boolean validateType() {
        String value = typeCombo.getValue();
        if (value == null) {
            typeError.setText("Type obligatoire");
            return false;
        } else {
            typeError.setText("✓");
            return true;
        }
    }

    private boolean validateStatut() {
        String value = statutCombo.getValue();
        if (value == null) {
            statutError.setText("Statut obligatoire");
            return false;
        } else {
            statutError.setText("✓");
            return true;
        }
    }

    private boolean validateDate() {
        boolean isValid = dateCreationPicker.getValue() != null;
        dateError.setText(isValid ? "✓" : "Date obligatoire");
        return isValid;
    }

    private void validateForm() {
        boolean isNomValid = validateNom();
        boolean isMontantValid = validateMontant();
        boolean isCodeValid = validateCode();
        boolean isTypeValid = validateType();
        boolean isStatutValid = validateStatut();
        boolean isDateValid = validateDate();
        
        boolean isFormValid = isNomValid && isMontantValid && isCodeValid && isTypeValid && isStatutValid && isDateValid;
        okButton.setDisable(!isFormValid);
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        if (produit != null) {
            nomField.setText(produit.getNomProduit());
            typeCombo.setValue(produit.getTypeProduit());
            montantField.setText(produit.getMontant().toString());
            codeUniqueField.setText(produit.getCodeUnique());
            statutCombo.setValue(produit.getStatut());
            dateCreationPicker.setValue(produit.getDateCreation());
            validateForm();
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        try {
            boolean isNew = (produit == null);
            if (isNew) {
                produit = new Produit();
            }

            produit.setNomProduit(nomField.getText().trim());
            produit.setTypeProduit(typeCombo.getValue());
            produit.setMontant(new BigDecimal(montantField.getText().trim()));
            produit.setCodeUnique(codeUniqueField.getText().trim());
            produit.setStatut(statutCombo.getValue());
            produit.setDateCreation(dateCreationPicker.getValue());

            if (isNew) {
                produitService.ajouter(produit);
            } else {
                produitService.modifier(produit);
            }

            okClicked = true;
            dialogStage.close();

        } catch (IllegalArgumentException | SQLException e) {
            showAlert("Erreur de Données", "Impossible de sauvegarder le produit.", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}