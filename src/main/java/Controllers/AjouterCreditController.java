package Controllers;

import Models.Credit;
import Services.ServiceCredit;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import Services.ServiceCompte;
import Services.ServiceCompte;

public class AjouterCreditController {

    @FXML private TextField txtMontant, txtTaux, txtDuree;
    @FXML private Label lblMensualite;

    private StackPane parentStack;
    private Region overlay;
    private VBox self;

    private int idCompte;
    private ServiceCredit service = new ServiceCredit();

    @FXML
    public void initialize() {
        // Calcul en temps réel
        txtMontant.textProperty().addListener((obs, old, newVal) -> calculerMensualite());
        txtTaux.textProperty().addListener((obs, old, newVal) -> calculerMensualite());
        txtDuree.textProperty().addListener((obs, old, newVal) -> calculerMensualite());
    }

    public void setCompteInfo(int idCompte, StackPane parent, Region overlay, VBox self) {
        this.idCompte = idCompte;
        this.parentStack = parent;
        this.overlay = overlay;
        this.self = self;
    }

    private void calculerMensualite() {
        try {
            if (txtMontant.getText().isEmpty() || txtTaux.getText().isEmpty() || txtDuree.getText().isEmpty()) {
                lblMensualite.setText("0.00 DT");
                return;
            }
            double p = Double.parseDouble(txtMontant.getText());
            double r = (Double.parseDouble(txtTaux.getText()) / 100) / 12;
            int n = Integer.parseInt(txtDuree.getText());

            if (p > 0 && r > 0 && n > 0) {
                double m = (p * r) / (1 - Math.pow(1 + r, -n));
                lblMensualite.setText(String.format("%.2f DT", m));
            }
        } catch (NumberFormatException e) {
            lblMensualite.setText("0.00 DT");
        }
    }

    @FXML
    private void handleConfirmer() {
        try {
            // ... (tes validations de champs habituelles)

            Credit c = new Credit();
            c.setCompte(this.idCompte);
            c.setMontant(Double.parseDouble(txtMontant.getText().replace(",", ".")));
            c.setTauxInteret(Double.parseDouble(txtTaux.getText().replace(",", ".")));
            c.setDureeMois(Integer.parseInt(txtDuree.getText()));
            c.setDateDebut(java.time.LocalDate.now());

            // LOGIQUE STATUT AUTOMATIQUE
            // On récupère l'état du compte via ton service compte
            // Dans handleConfirmer() du AjouterCreditController
            // Dans handleConfirmer() de AjouterCreditController
            ServiceCompte serviceCompte = new ServiceCompte();
            String etatCompteDB = serviceCompte.getEtatParId(this.idCompte);

// DEBUG : Regarde bien ce qui s'affiche dans ta console en bas d'IntelliJ/NetBeans
            System.out.println("--- DEBUG STATUT ---");
            System.out.println("ID Compte envoyé : " + this.idCompte);
            System.out.println("État récupéré : '" + (etatCompteDB == null ? "NULL" : etatCompteDB) + "'");
// Dans handleConfirmer() de AjouterCreditController
            if (etatCompteDB != null && etatCompteDB.trim().equalsIgnoreCase("ACTIF")) {
                // REMPLACE l'espace par un underscore pour correspondre à ton ENUM SQL
                c.setStatut("EN_COURS");
            } else {
                c.setStatut("REFUSE");
            }

            // Nettoyage de la mensualité
            String mStr = lblMensualite.getText().replaceAll("[^0-9.,]", "").replace(",", ".");
            c.setMensualite(Double.parseDouble(mStr));

            service.ajouter(c);
            handleAnnuler();

        } catch (Exception e) {
            // Affichera l'erreur précise si ça échoue encore
            showError("Erreur d'ajout : " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Erreur d'ajout");
        alert.setContentText(message);
        alert.showAndWait();
    }


    // Ajoute ceci en haut de la classe AjouterCreditController
    public interface OnCloseListener {
        void onPopupClosed();
    }

    private OnCloseListener onCloseListener;

    public void setOnCloseListener(OnCloseListener listener) {
        this.onCloseListener = listener;
    }

    // Modifie ta méthode handleAnnuler (ou celle qui ferme le popup)
    @FXML
    private void handleAnnuler() {
        if (parentStack != null) {
            parentStack.getChildren().removeAll(overlay, self);
            // ON PRÉVIENT LE PARENT ICI
            if (onCloseListener != null) {
                onCloseListener.onPopupClosed();
            }
        }
    }
}