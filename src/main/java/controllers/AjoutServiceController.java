package controllers;

import models.Service;
import services.ServiceService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.sql.SQLException;

public class AjoutServiceController {
    @FXML private TextField nomField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField tarifField;
    @FXML private ComboBox<String> frequenceCombo;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<String> statutCombo;
    @FXML private Label nomError, typeError, tarifError, frequenceError, dateError, statutError;
    @FXML private Button okButton;

    private Stage dialogStage;
    private Service service; // Pour savoir si on est en mode ajout ou modification
    private boolean okClicked = false;
    private final ServiceService serviceService = new ServiceService();

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList("abonnement", "facture"));
        frequenceCombo.setItems(FXCollections.observableArrayList("mensuel", "annuel"));
        statutCombo.setItems(FXCollections.observableArrayList("actif", "suspendu", "expire"));
        statutCombo.setValue("actif");
        
        // Disable OK button by default
        okButton.setDisable(true);
        
        setupValidation();
    }

    /**
     * Pré-remplit le formulaire avec les données d'un service existant pour la modification.
     */
    public void setService(Service service) {
        this.service = service;

        if (service != null) {
            nomField.setText(service.getNomService());
            typeCombo.setValue(service.getTypeService());
            tarifField.setText(service.getTarif().toString());
            frequenceCombo.setValue(service.getFrequence());
            dateDebutPicker.setValue(service.getDateDebut());
            dateFinPicker.setValue(service.getDateFin());
            statutCombo.setValue(service.getStatut());
            validateForm(); // Validate pre-filled data
        }
    }


    private void setupValidation() {
        // Add a listener to each field to re-validate the form on any change
        nomField.textProperty().addListener((obs, old, n) -> validateForm());
        tarifField.textProperty().addListener((obs, old, n) -> validateForm());
        typeCombo.valueProperty().addListener((obs, old, n) -> validateForm());
        frequenceCombo.valueProperty().addListener((obs, old, n) -> validateForm());
        statutCombo.valueProperty().addListener((obs, old, n) -> validateForm());
        dateDebutPicker.valueProperty().addListener((obs, old, n) -> validateForm());
        dateFinPicker.valueProperty().addListener((obs, old, n) -> validateForm());
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

    private boolean validateTarif() {
        String text = tarifField.getText();
        if (text == null || text.trim().isEmpty()) {
            tarifError.setText("Tarif obligatoire");
            return false;
        } else {
            try {
                double tarif = Double.parseDouble(text);
                if (tarif <= 0) {
                    tarifError.setText("Tarif > 0");
                    return false;
                } else {
                    tarifError.setText("✓");
                    return true;
                }
            } catch (NumberFormatException e) {
                tarifError.setText("Format invalide");
                return false;
            }
        }
    }

    private boolean validateType() {
        boolean isValid = typeCombo.getValue() != null;
        typeError.setText(isValid ? "✓" : "Type obligatoire");
        return isValid;
    }

    private boolean validateFrequence() {
        boolean isValid = frequenceCombo.getValue() != null;
        frequenceError.setText(isValid ? "✓" : "Fréquence obligatoire");
        return isValid;
    }

    private boolean validateStatut() {
        boolean isValid = statutCombo.getValue() != null;
        statutError.setText(isValid ? "✓" : "Statut obligatoire");
        return isValid;
    }

    private boolean validateDates() {
        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin = dateFinPicker.getValue();
        if (debut == null) {
            dateError.setText("Date début obligatoire");
            return false;
        } else if (fin != null && fin.isBefore(debut)) {
            dateError.setText("Date fin > début");
            return false;
        } else {
            dateError.setText("✓");
            return true;
        }
    }

    private void validateForm() {
        boolean isNomValid = validateNom();
        boolean isTarifValid = validateTarif();
        boolean isTypeValid = validateType();
        boolean isFrequenceValid = validateFrequence();
        boolean isDatesValid = validateDates();
        boolean isStatutValid = validateStatut();

        boolean isFormValid = isNomValid && isTarifValid && isTypeValid && isFrequenceValid && isDatesValid && isStatutValid;
        okButton.setDisable(!isFormValid);
    }

    public void setDialogStage(Stage stage) { this.dialogStage = stage; }
    public boolean isOkClicked() { return okClicked; }

    @FXML private void handleOk() {
        try {
            boolean isNew = (service == null);
            if (isNew) {
                service = new Service();
            }

            service.setNomService(nomField.getText().trim());
            service.setTypeService(typeCombo.getValue());
            service.setTarif(new BigDecimal(tarifField.getText().trim()));
            service.setFrequence(frequenceCombo.getValue());
            service.setDateDebut(dateDebutPicker.getValue());
            service.setDateFin(dateFinPicker.getValue());
            service.setStatut(statutCombo.getValue());

            if (isNew) {
                serviceService.ajouter(service);
            } else {
                serviceService.modifier(service);
            }

            okClicked = true;
            dialogStage.close();

        } catch (SQLException e) {
            showAlert("Erreur Base de Données", "Impossible de sauvegarder le service.", e.getMessage());
        }
    }

    @FXML private void handleCancel() {
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