package controllers;

import models.Service;
import services.ServiceService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.SQLException;

public class AjoutServiceDialogController {

    @FXML private TextField nomField;
    @FXML private TextField typeField;
    @FXML private TextField tarifField;
    @FXML private TextField frequenceField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField statutField;

    private Stage dialogStage;
    private Service service;
    private boolean okClicked = false;
    private final ServiceService serviceService = new ServiceService();

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Pré-remplit le formulaire avec les données d'un service existant pour la modification.
     * Si le service est null, le formulaire reste vide pour un ajout.
     */
    public void setService(Service service) {
        this.service = service;

        if (service != null) {
            nomField.setText(service.getNomService());
            typeField.setText(service.getTypeService());
            tarifField.setText(service.getTarif().toString());
            frequenceField.setText(service.getFrequence());
            dateDebutPicker.setValue(service.getDateDebut());
            dateFinPicker.setValue(service.getDateFin());
            statutField.setText(service.getStatut());
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        try {
            if (service == null) { // Mode Ajout
                service = new Service();
            }

            // Mettre à jour l'objet service avec les données du formulaire
            // Les setters vont valider les données et lever une exception si elles sont invalides
            service.setNomService(nomField.getText());
            service.setTypeService(typeField.getText());
            service.setTarif(new BigDecimal(tarifField.getText()));
            service.setFrequence(frequenceField.getText());
            service.setDateDebut(dateDebutPicker.getValue());
            service.setDateFin(dateFinPicker.getValue());
            service.setStatut(statutField.getText());

            // Sauvegarder en base de données
            if (service.getIdService() == null) {
                serviceService.ajouter(service);
            } else {
                serviceService.modifier(service);
            }

            okClicked = true;
            dialogStage.close();

        } catch (IllegalArgumentException e) {
            // C'est ici que le contrôle de saisie devient fonctionnel !
            // On attrape l'erreur de validation et on l'affiche à l'utilisateur.
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez corriger les champs invalides.", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", "Impossible de sauvegarder le service.", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}