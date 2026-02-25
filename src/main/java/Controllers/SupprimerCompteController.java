package Controllers;

import Models.Compte;
import Services.ServiceCompte;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.sql.SQLDataException;

public class SupprimerCompteController {

    @FXML private Button btnAnnuler;

    private Compte compteASupprimer;
    private ServiceCompte service = new ServiceCompte();
    private boolean suppressionConfirmee = false;

    public void setCompte(Compte compte) {
        this.compteASupprimer = compte;
    }

    public boolean isSuppressionConfirmee() {
        return suppressionConfirmee;
    }

    @FXML
    private void handleConfirmer() {
        try {
            if (compteASupprimer != null) {
                service.supprimer(compteASupprimer);
                suppressionConfirmee = true;
                closeStage();
            }
        } catch (SQLDataException e) {
            System.err.println("Erreur SQL lors de la suppression : " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        suppressionConfirmee = false;
        closeStage();
    }

    private void closeStage() {
        if (btnAnnuler != null && btnAnnuler.getScene() != null) {
            Stage stage = (Stage) btnAnnuler.getScene().getWindow();
            stage.close();
        }
    }
}