package Controllers;

import Models.Credit;
import Services.ServiceCredit;
import javafx.fxml.FXML;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SupprimerCreditController {

    @FXML private VBox mainContainer;

    private Credit creditASupprimer;
    private ServiceCredit service = new ServiceCredit();
    private StackPane parentStack;
    private Region overlay;
    private Runnable onRefresh;

    /**
     * Initialisation des données depuis le contrôleur principal
     */
    public void setData(Credit c, StackPane stack, Region ov, Runnable refreshCallback) {
        this.creditASupprimer = c;
        this.parentStack = stack;
        this.overlay = ov;
        this.onRefresh = refreshCallback;
    }

    @FXML
    private void handleSupprimer() {
        if (creditASupprimer != null) {
            try {
                // On passe l'objet complet 'creditASupprimer' comme requis par ton service
                service.supprimer(creditASupprimer);
                fermerEtRafraichir();
            } catch (java.sql.SQLDataException e) {
                System.out.println("Erreur SQL lors de la suppression : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAnnuler() {
        fermerEtRafraichir();
    }

    private void fermerEtRafraichir() {
        if (parentStack != null) {
            parentStack.getChildren().removeAll(overlay, mainContainer);
            if (onRefresh != null) {
                onRefresh.run(); // Relance chargerDonnees() dans AfficherCredit
            }
        }
    }
}