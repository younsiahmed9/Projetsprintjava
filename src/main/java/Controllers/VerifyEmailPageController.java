package Controllers;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class VerifyEmailPageController {
    @FXML
    private Text resultText;

    public void setResult(boolean success) {
        if (success) {
            resultText.setText("Votre email a été vérifié avec succès ! Vous pouvez maintenant vous connecter.");
        } else {
            resultText.setText("Le lien de vérification est invalide ou expiré.");
        }
    }
}
