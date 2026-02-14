package Controllers.components;

import Models.Portefeuille;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Contrôleur du composant portefeuille_card.fxml.
 * Fournit des callbacks (edit/delete/viewCards) injectés depuis la page parent.
 */
public class PortefeuilleCardController {

    @FXML private Label nomLabel;
    @FXML private Label soldeLabel;

    private Portefeuille portefeuille;

    private Runnable onEdit;
    private Runnable onDelete;
    private Runnable onViewCards;

    public void setData(Portefeuille portefeuille) {
        this.portefeuille = portefeuille;
        nomLabel.setText(portefeuille.getNom());
        soldeLabel.setText(String.format("%.2f %s", portefeuille.getSoldeTotal(), portefeuille.getDevisePrincipale()));
    }

    public void setOnEdit(Runnable onEdit) { this.onEdit = onEdit; }
    public void setOnDelete(Runnable onDelete) { this.onDelete = onDelete; }
    public void setOnViewCards(Runnable onViewCards) { this.onViewCards = onViewCards; }

    @FXML
    private void onEdit() {
        if (onEdit != null) onEdit.run();
    }

    @FXML
    private void onDelete() {
        if (onDelete != null) onDelete.run();
    }

    @FXML
    private void onViewCards() {
        if (onViewCards != null) onViewCards.run();
    }
}

