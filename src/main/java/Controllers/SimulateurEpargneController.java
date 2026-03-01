package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SimulateurEpargneController {

    @FXML
    private TextField objectifField;
    @FXML
    private TextField montantCibleField;
    @FXML
    private TextField delaiMoisField;
    @FXML
    private Label resultatLabel;
    @FXML
    private Label capaciteLabel;

    private MainPageController mainPageController;
    private double currentRemainingBudget = 0;

    public void setMainPageController(MainPageController mainPageController, double currentRemainingBudget) {
        this.mainPageController = mainPageController;
        this.currentRemainingBudget = currentRemainingBudget;
        capaciteLabel
                .setText(String.format("💰 Votre budget global restant ce mois-ci : %.2f DT", currentRemainingBudget));
    }

    @FXML
    private void onCalculer() {
        try {
            String objectif = objectifField.getText() == null || objectifField.getText().trim().isEmpty()
                    ? "cet objectif"
                    : objectifField.getText().trim();
            double cible = Double.parseDouble(montantCibleField.getText());
            int mois = Integer.parseInt(delaiMoisField.getText());

            if (mois <= 0) {
                resultatLabel.setText("❌ Erreur : Le délai doit être supérieur à 0.");
                resultatLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            if (cible <= 0) {
                resultatLabel.setText("❌ Erreur : Le montant cible doit être supérieur à 0.");
                resultatLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            double epargneMensuelle = cible / mois;
            StringBuilder message = new StringBuilder();
            message.append(String.format("🎯 Pour financer %s en %d mois, vous devez économiser :\n", objectif, mois));
            message.append(String.format("► %.2f DT / mois.\n\n", epargneMensuelle));

            if (currentRemainingBudget >= epargneMensuelle) {
                message.append("✅ Bonne nouvelle ! Votre budget restant actuel (")
                        .append(String.format("%.2f", currentRemainingBudget))
                        .append(" DT) est suffisant pour couvrir cette épargne.");
                resultatLabel.setStyle("-fx-text-fill: #15803d;"); // Green
            } else {
                double manque = epargneMensuelle - currentRemainingBudget;
                message.append("⚠️ Attention ! Votre budget restant actuel (")
                        .append(String.format("%.2f", currentRemainingBudget))
                        .append(" DT) est insuffisant.\nIl vous manque ")
                        .append(String.format("%.2f", manque))
                        .append(" DT par mois pour atteindre votre objectif sans dépasser votre budget.");
                resultatLabel.setStyle("-fx-text-fill: #b91c1c;"); // Red
            }

            resultatLabel.setText(message.toString());
        } catch (NumberFormatException e) {
            resultatLabel.setText("❌ Veuillez entrer des valeurs numériques valides pour le montant et le délai.");
            resultatLabel.setStyle("-fx-text-fill: #b91c1c;");
        }
    }

    @FXML
    private void onRetour() {
        if (mainPageController != null) {
            mainPageController.retournerAListe();
        }
    }
}
