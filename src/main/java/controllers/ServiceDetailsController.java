package controllers;

import models.Service;
import services.ServicePersonne;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ServiceDetailsController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField idField;
    @FXML private TextField nomField;
    @FXML private TextField typeField;
    @FXML private TextField tarifField;
    @FXML private TextField frequenceField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField statutField;
    // NOTE: Il n'y a PAS de descriptionArea dans votre FXML, donc pas de variable ici

    private Service service;
    private ServicePersonne servicePersonne;
    private Stage dialogStage;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        servicePersonne = new ServicePersonne();
        System.out.println("✅ ServiceDetailsController initialisé");
    }

    public void setService(Service service) {
        this.service = service;
        if (service != null) {
            titleLabel.setText("Détails du service : " + service.getNomService());
            idField.setText(String.valueOf(service.getIdService()));
            nomField.setText(service.getNomService());
            typeField.setText(service.getTypeService());
            tarifField.setText(String.format("%.2f DT", service.getTarif()));
            frequenceField.setText(service.getFrequence());
            dateDebutPicker.setValue(service.getDateDebut());
            dateFinPicker.setValue(service.getDateFin());
            statutField.setText(service.getStatut());

            // Colorer le statut selon sa valeur
            colorerStatut();
        }
    }

    private void colorerStatut() {
        if (statutField == null) return;

        switch (service.getStatut()) {
            case "actif":
                statutField.setStyle("-fx-text-fill: #3e893e; -fx-font-weight: bold;");
                break;
            case "suspendu":
                statutField.setStyle("-fx-text-fill: #f78f34; -fx-font-weight: bold;");
                break;
            case "expire":
                statutField.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                break;
            default:
                statutField.setStyle("");
                break;
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleRefresh() {
        try {
            if (service == null) return;

            Service refreshed = servicePersonne.getServiceById(service.getIdService());
            if (refreshed != null) {
                setService(refreshed);
                showInfo("Succès", "Données rafraîchies avec succès");
            } else {
                showAlert("Erreur", "Service non trouvé");
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de rafraîchir: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModifier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/ModifierServiceDialog.fxml"));
            Parent root = loader.load();

            ModifierServiceController controller = loader.getController();
            controller.setService(service);

            Stage stage = new Stage();
            stage.setTitle("Modifier le service");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isOkClicked()) {
                handleRefresh();
            }

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFermer() {
        if (dialogStage != null) {
            dialogStage.close();
        } else {
            // Si pas de stage dédié, chercher le stage parent
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.close();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}