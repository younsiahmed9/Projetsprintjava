package controllers;

import com.lowagie.text.DocumentException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Facture;
import services.ServicePDF;
import services.ServiceEmailMailtrapAPI; // Service utilisant l'API Mailtrap

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EnvoiFactureController {

    @FXML private TextField emailClientField;
    @FXML private TextField nomClientField;
    @FXML private RadioButton radioPDF;
    @FXML private RadioButton radioEmail;
    @FXML private ToggleGroup choixGroupe;

    private Facture facture;
    private Stage dialogStage;
    private ServicePDF servicePDF;
    private ServiceEmailMailtrapAPI serviceEmail; // Service email via API Mailtrap
    private boolean okClicked = false;

    @FXML
    public void initialize() {
        servicePDF = new ServicePDF();
        serviceEmail = new ServiceEmailMailtrapAPI(); // Utilise votre token depuis MailtrapConfig

        // Groupe de boutons radio pour le choix
        radioPDF.setToggleGroup(choixGroupe);
        radioEmail.setToggleGroup(choixGroupe);
        radioPDF.setSelected(true); // Par défaut : générer PDF uniquement
    }

    /**
     * Reçoit la facture à traiter et pré-remplit les champs si des informations existent.
     */
    public void setFacture(Facture facture) {
        this.facture = facture;
        if (facture.getEmailClient() != null) {
            emailClientField.setText(facture.getEmailClient());
        }
        if (facture.getNomClient() != null) {
            nomClientField.setText(facture.getNomClient());
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
        if (facture == null) {
            showAlert("Erreur", "Aucune facture sélectionnée");
            return;
        }

        // Mettre à jour la facture avec les informations saisies
        facture.setEmailClient(emailClientField.getText());
        facture.setNomClient(nomClientField.getText());

        try {
            // Étape 1 : générer le PDF
            String cheminPDF = servicePDF.genererFacturePDF(facture);
            facture.setCheminPDF(cheminPDF);

            // Étape 2 : selon l'option choisie
            if (radioEmail.isSelected()) {
                // Vérifier que l'email destinataire est renseigné
                if (emailClientField.getText().isEmpty()) {
                    showAlert("Erreur", "Veuillez saisir l'email du client");
                    return;
                }

                String sujet = "Votre facture " + facture.getNumeroFacture();
                String corps = "Bonjour " + facture.getNomClient() + ",\n\n"
                        + "Veuillez trouver votre facture en pièce jointe.\n\n"
                        + "Cordialement.";

                // Lire le contenu du PDF pour le joindre
                byte[] pdfContent = Files.readAllBytes(Paths.get(cheminPDF));

                // Envoyer l'email via l'API Mailtrap
                boolean success = serviceEmail.envoyerFactureEmail(
                        emailClientField.getText(), // destinataire
                        sujet,
                        corps,
                        cheminPDF // on passe le chemin pour que le service puisse lire le fichier
                );

                if (success) {
                    showInfo("Succès", "Email envoyé à " + emailClientField.getText());
                } else {
                    showAlert("Erreur", "Échec de l'envoi par email");
                    return;
                }
            } else {
                // Option "Générer PDF uniquement"
                showInfo("Succès", "PDF généré : " + cheminPDF);
            }

            okClicked = true;
            if (dialogStage != null) {
                dialogStage.close();
            }

        } catch (IOException | DocumentException e) {
            showAlert("Erreur", "Erreur lors de la génération du PDF : " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}