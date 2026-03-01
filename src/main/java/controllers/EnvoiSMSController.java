package controllers;

import api.SmsProvider;
import api.SmsProviderFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class EnvoiSMSController implements Initializable {

    @FXML private TextField numeroField;
    @FXML private TextArea messageField;
    @FXML private Button envoyerButton;
    @FXML private Button annulerButton;
    @FXML private Label statusLabel;

    private Stage dialogStage;
    private boolean okClicked = false;
    private SmsProvider smsProvider;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        smsProvider = SmsProviderFactory.createProvider();
        statusLabel.setText("Prêt - Mode: " + smsProvider.getProviderName());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleEnvoyer() {
        String numero = numeroField.getText().trim();
        String message = messageField.getText().trim();

        if (numero.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir un numéro");
            return;
        }

        if (message.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir un message");
            return;
        }

        statusLabel.setText("Envoi en cours...");
        envoyerButton.setDisable(true);

        new Thread(() -> {
            boolean success = smsProvider.sendSms(numero, message);

            javafx.application.Platform.runLater(() -> {
                envoyerButton.setDisable(false);

                if (success) {
                    statusLabel.setText("✅ SMS envoyé");
                    okClicked = true;
                    dialogStage.close();
                } else {
                    statusLabel.setText("❌ Échec");
                    showAlert("Erreur", "Échec de l'envoi");
                }
            });
        }).start();
    }

    @FXML
    private void handleAnnuler() {
        dialogStage.close();
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}