package controllers;

import models.Produit;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

public class ProduitDetailsController {

    @FXML private TextField idField;
    @FXML private TextField nomField;
    @FXML private TextField typeField;
    @FXML private TextField montantField;
    @FXML private TextField codeUniqueField;
    @FXML private DatePicker dateCreationPicker;
    @FXML private TextField statutField;

    private Stage dialogStage;
    private Produit produit;
    private ServiceController serviceController; // Pour rafraîchir la vue principale après modification

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setServiceController(ServiceController serviceController) {
        this.serviceController = serviceController;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        if (produit != null) {
            idField.setText(String.valueOf(produit.getIdProduit()));
            nomField.setText(produit.getNomProduit());
            typeField.setText(produit.getTypeProduit());
            montantField.setText(String.format("%.2f DT", produit.getMontant()));
            codeUniqueField.setText(produit.getCodeUnique());
            dateCreationPicker.setValue(produit.getDateCreation());
            statutField.setText(produit.getStatut());
        }
    }

    @FXML
    private void handleFermer() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void handleModifier() {
        showProduitEditDialog(this.produit);
    }

    @FXML
    private void handleRefresh() {
        // Ici, vous pourriez recharger le produit depuis la base de données
        // Pour l'instant, nous réaffichons les données actuelles.
        setProduit(this.produit);
    }

    private void showProduitEditDialog(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/AjoutProduitDialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);

            AjoutProduitController controller = loader.getController();
            controller.setDialogStage(stage);
            controller.setProduit(produit);

            stage.showAndWait();

            if (controller.isOkClicked() && serviceController != null) {
                serviceController.chargerDonnees(); // Rafraîchit la vue principale
                handleFermer(); // Ferme la vue de détails
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}