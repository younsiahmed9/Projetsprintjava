package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

public class SimulateurPretController {

    @FXML private Slider sliderMontant, sliderDuree;
    @FXML private Label lblMontant, lblDuree, lblMensualite;

    private final double TAUX_ANNUEL = 0.08; // Taux à 8%

    // Références pour la gestion du popup global
    private StackPane rootPane;
    private StackPane myOverlay;

    /**
     * Permet au DashboardController de passer les références du root global
     */
    public void setOverlayParent(StackPane root, StackPane overlay) {
        this.rootPane = root;
        this.myOverlay = overlay;
    }

    /**
     * AJOUTÉ : Reçoit la limite maximale calculée dynamiquement par le Dashboard
     */
    public void configurerLimiteDyna(double maxPret) {
        if (maxPret > 0) {
            sliderMontant.setMax(maxPret);
            // On positionne le curseur par défaut à la moitié du max autorisé
            sliderMontant.setValue(maxPret / 2);
        } else {
            sliderMontant.setMax(1000); // Sécurité si solde nul
            sliderMontant.setValue(1000);
        }
        calculer();
    }

    @FXML
    public void initialize() {
        // Écouteurs pour mise à jour dynamique
        sliderMontant.valueProperty().addListener((obs, oldVal, newVal) -> calculer());
        sliderDuree.valueProperty().addListener((obs, oldVal, newVal) -> calculer());

        calculer();
    }

    private void calculer() {
        double montant = sliderMontant.getValue();
        int duree = (int) sliderDuree.getValue();

        if (duree <= 0) return;

        // Calcul de la mensualité (formule standard de prêt)
        double tauxMensuel = TAUX_ANNUEL / 12;
        double mensualite = (montant * tauxMensuel) / (1 - Math.pow(1 + tauxMensuel, -duree));

        // Mise à jour de l'affichage
        lblMontant.setText(String.format("%.0f DT", montant));
        lblDuree.setText(duree + " mois");
        lblMensualite.setText(String.format("%.2f DT", mensualite));
    }

    @FXML
    private void handleFermer() {
        // Supprime l'overlay grisâtre de la scène principale
        if (rootPane != null && myOverlay != null) {
            rootPane.getChildren().remove(myOverlay);
        }
    }

}