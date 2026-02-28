package controllers;

import models.Service;
import services.ServiceService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.sql.SQLException;

public class ModifierServiceController {
    @FXML private TextField nomField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField tarifField;
    @FXML private ComboBox<String> frequenceCombo;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<String> statutCombo;

    private Service service;
    private Stage dialogStage;
    private boolean okClicked = false;
    private ServiceService serviceService = new ServiceService();

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList("abonnement", "facture"));
        frequenceCombo.setItems(FXCollections.observableArrayList("mensuel", "annuel"));
        statutCombo.setItems(FXCollections.observableArrayList("actif", "suspendu", "expire"));
    }

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
        }
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            try {
                service.setNomService(nomField.getText());
                service.setTypeService(typeCombo.getValue());
                service.setTarif(new BigDecimal(tarifField.getText()));
                service.setFrequence(frequenceCombo.getValue());
                service.setDateDebut(dateDebutPicker.getValue());
                service.setDateFin(dateFinPicker.getValue());
                service.setStatut(statutCombo.getValue());
                serviceService.modifier(service);
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
        try { new BigDecimal(tarifField.getText()); } catch (NumberFormatException e) { return false; }
        if (frequenceCombo.getValue() == null) return false;
        if (dateDebutPicker.getValue() == null) return false;
        if (statutCombo.getValue() == null) return false;
        return true;
    }

    @FXML private void handleCancel() { dialogStage.close(); }
    public void setDialogStage(Stage stage) { this.dialogStage = stage; }
    public boolean isOkClicked() { return okClicked; }
    private void showAlert(String title, String msg) { new Alert(Alert.AlertType.ERROR, msg).show(); }
}