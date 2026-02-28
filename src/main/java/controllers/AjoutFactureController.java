package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import models.Facture;
import models.Service;
import models.Produit;
import services.ServiceFacture;
import services.ServiceService;
import services.ServiceProduit;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AjoutFactureController {

    @FXML private TextField numeroFactureField;
    @FXML private TextField montantField;
    @FXML private DatePicker dateFacturePicker;
    @FXML private DatePicker dateEcheancePicker;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<Object> referenceCombo;
    @FXML private ComboBox<String> statutCombo;

    @FXML private Label numeroError;
    @FXML private Label montantError;
    @FXML private Label dateError;
    @FXML private Label typeError;
    @FXML private Label referenceError;
    @FXML private Label statutError;

    private ServiceFacture factureService;
    private ServiceService serviceService;
    private ServiceProduit produitService;
    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    public void initialize() {
        factureService = new ServiceFacture();
        serviceService = new ServiceService();
        produitService = new ServiceProduit();

        typeCombo.setItems(FXCollections.observableArrayList("Service", "Produit"));
        statutCombo.setItems(FXCollections.observableArrayList("payee", "impayee", "en_attente", "annulee"));
        statutCombo.setValue("impayee");

        dateFacturePicker.setValue(LocalDate.now());
        dateEcheancePicker.setValue(LocalDate.now().plusDays(30));

        setupValidation();

        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            chargerReferences(newVal);
        });
    }

    private void chargerReferences(String type) {
        referenceCombo.getItems().clear();
        try {
            if ("Service".equals(type)) {
                List<Service> services = serviceService.recupererTous();
                referenceCombo.setItems(FXCollections.observableArrayList(services));
                if (!services.isEmpty()) {
                    referenceCombo.setValue(services.get(0));
                }
            } else if ("Produit".equals(type)) {
                List<Produit> produits = produitService.recupererTous();
                referenceCombo.setItems(FXCollections.observableArrayList(produits));
                if (!produits.isEmpty()) {
                    referenceCombo.setValue(produits.get(0));
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les références: " + e.getMessage());
        }
    }

    private void setupValidation() {
        numeroFactureField.textProperty().addListener((obs, old, n) -> {
            if (n == null || n.trim().isEmpty()) {
                numeroError.setText("Numéro obligatoire");
                numeroError.setTextFill(Color.RED);
            } else {
                numeroError.setText("✓");
                numeroError.setTextFill(Color.GREEN);
            }
        });

        montantField.textProperty().addListener((obs, old, n) -> {
            if (n == null || n.trim().isEmpty()) {
                montantError.setText("Montant obligatoire");
                montantError.setTextFill(Color.RED);
            } else {
                try {
                    double m = Double.parseDouble(n);
                    if (m <= 0) {
                        montantError.setText("Montant > 0");
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

        dateFacturePicker.valueProperty().addListener((obs, old, n) -> validateDates());
        dateEcheancePicker.valueProperty().addListener((obs, old, n) -> validateDates());

        typeCombo.valueProperty().addListener((obs, old, n) -> {
            if (n == null) {
                typeError.setText("Type obligatoire");
                typeError.setTextFill(Color.RED);
            } else {
                typeError.setText("✓");
                typeError.setTextFill(Color.GREEN);
            }
        });

        referenceCombo.valueProperty().addListener((obs, old, n) -> {
            if (n == null) {
                referenceError.setText("Référence obligatoire");
                referenceError.setTextFill(Color.RED);
            } else {
                referenceError.setText("✓");
                referenceError.setTextFill(Color.GREEN);
            }
        });

        statutCombo.valueProperty().addListener((obs, old, n) -> {
            if (n == null) {
                statutError.setText("Statut obligatoire");
                statutError.setTextFill(Color.RED);
            } else {
                statutError.setText("✓");
                statutError.setTextFill(Color.GREEN);
            }
        });
    }

    private void validateDates() {
        LocalDate debut = dateFacturePicker.getValue();
        LocalDate fin = dateEcheancePicker.getValue();

        if (debut == null) {
            dateError.setText("Date facture obligatoire");
            dateError.setTextFill(Color.RED);
        } else if (fin != null && fin.isBefore(debut)) {
            dateError.setText("Échéance > date facture");
            dateError.setTextFill(Color.RED);
        } else {
            dateError.setText("✓");
            dateError.setTextFill(Color.GREEN);
        }
    }

    public boolean validateForm() {
        boolean isValid = true;

        if (numeroFactureField.getText() == null || numeroFactureField.getText().trim().isEmpty()) {
            numeroError.setText("Numéro obligatoire");
            numeroError.setTextFill(Color.RED);
            isValid = false;
        }

        if (montantField.getText() == null || montantField.getText().trim().isEmpty()) {
            montantError.setText("Montant obligatoire");
            montantError.setTextFill(Color.RED);
            isValid = false;
        } else {
            try {
                Double.parseDouble(montantField.getText());
            } catch (NumberFormatException e) {
                montantError.setText("Format invalide");
                montantError.setTextFill(Color.RED);
                isValid = false;
            }
        }

        if (dateFacturePicker.getValue() == null) {
            dateError.setText("Date facture obligatoire");
            dateError.setTextFill(Color.RED);
            isValid = false;
        }

        if (typeCombo.getValue() == null) {
            typeError.setText("Type obligatoire");
            typeError.setTextFill(Color.RED);
            isValid = false;
        }

        if (referenceCombo.getValue() == null) {
            referenceError.setText("Référence obligatoire");
            referenceError.setTextFill(Color.RED);
            isValid = false;
        }

        if (statutCombo.getValue() == null) {
            statutError.setText("Statut obligatoire");
            statutError.setTextFill(Color.RED);
            isValid = false;
        }

        return isValid;
    }

    public Facture getFacture() {
        if (!validateForm()) return null;

        try {
            Facture facture = new Facture();
            facture.setNumeroFacture(numeroFactureField.getText().trim());
            facture.setMontant(new BigDecimal(montantField.getText().trim()));
            facture.setDateFacture(dateFacturePicker.getValue());
            facture.setDateEcheance(dateEcheancePicker.getValue());
            facture.setStatut(statutCombo.getValue());

            Object reference = referenceCombo.getValue();
            if (reference instanceof Service) {
                Service service = (Service) reference;
                facture.setIdService(service.getIdService());
                facture.setIdProduit(null);
            } else if (reference instanceof Produit) {
                Produit produit = (Produit) reference;
                facture.setIdProduit(produit.getIdProduit());
                facture.setIdService(null);
            }

            return facture;
        } catch (Exception e) {
            showAlert("Erreur", "Données invalides: " + e.getMessage());
            return null;
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
        if (validateForm()) {
            try {
                Facture facture = getFacture();
                if (facture != null) {
                    factureService.ajouter(facture);
                    okClicked = true;
                    if (dialogStage != null) {
                        dialogStage.close();
                    } else {
                        Stage stage = (Stage) numeroFactureField.getScene().getWindow();
                        stage.close();
                    }
                }
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible d'ajouter la facture: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        } else {
            Stage stage = (Stage) numeroFactureField.getScene().getWindow();
            stage.close();
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