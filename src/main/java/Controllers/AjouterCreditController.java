package Controllers;

import Models.Credit;
import Models.Compte;
import Services.ServiceCredit;
import Services.ServiceCompte;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Imports Twilio
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class AjouterCreditController {

    @FXML private TextField txtMontant, txtTaux, txtDuree, txtTelephone;
    @FXML private Label lblMensualite, lblScore;
    @FXML private ProgressBar progressScore;

    // --- CONFIGURATION TWILIO ---
    public static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    public static final String AUTH_TOKEN = "dc84443342ebb24150aa2b9d82eb2bb6";
    public static final String TWILIO_NUMBER = "+18126668106";

    private StackPane parentStack;
    private Region overlay;
    private VBox self;
    private int idCompte;
    private ServiceCredit service = new ServiceCredit();
    private ServiceCompte serviceCompte = new ServiceCompte();

    public interface OnCloseListener { void onPopupClosed(); }
    private OnCloseListener onCloseListener;
    public void setOnCloseListener(OnCloseListener listener) { this.onCloseListener = listener; }

    @FXML
    public void initialize() {
        txtMontant.textProperty().addListener((obs, old, newVal) -> mettreAJourInterface());
        txtTaux.textProperty().addListener((obs, old, newVal) -> mettreAJourInterface());
        txtDuree.textProperty().addListener((obs, old, newVal) -> mettreAJourInterface());
    }

    public void setCompteInfo(int idCompte, StackPane parent, Region overlay, VBox self) {
        this.idCompte = idCompte;
        this.parentStack = parent;
        this.overlay = overlay;
        this.self = self;
    }

    private void mettreAJourInterface() {
        calculerMensualite();
        calculerScoreSolvabilite();
    }

    private void calculerMensualite() {
        try {
            String mStr = txtMontant.getText().replace(",", ".");
            String tStr = txtTaux.getText().replace(",", ".");
            String dStr = txtDuree.getText();

            if (mStr.isEmpty() || tStr.isEmpty() || dStr.isEmpty()) {
                lblMensualite.setText("0.00 DT");
                return;
            }

            double p = Double.parseDouble(mStr);
            double r = (Double.parseDouble(tStr) / 100) / 12;
            int n = Integer.parseInt(dStr);

            if (p > 0 && r > 0 && n > 0) {
                double m = (p * r) / (1 - Math.pow(1 + r, -n));
                lblMensualite.setText(String.format("%.2f DT", m));
            } else {
                lblMensualite.setText("0.00 DT");
            }
        } catch (NumberFormatException e) {
            lblMensualite.setText("0.00 DT");
        }
    }

    private void calculerScoreSolvabilite() {
        try {
            String mStr = txtMontant.getText().replace(",", ".");
            if (mStr.isEmpty()) { resetScore(); return; }

            double montantDemande = Double.parseDouble(mStr);
            Compte compte = serviceCompte.recupererParId(this.idCompte);

            if (compte == null) return;

            int score = 50;
            if ("BLOQUE".equalsIgnoreCase(compte.getEtat())) {
                appliquerStyleScore(-1, "REFUSÉ (Bloqué)");
                return;
            }
            if (compte.getSolde() >= montantDemande * 0.25) score += 25;
            else if (compte.getSolde() < 0) score -= 20;

            if (service.aDesCreditsEnCours(this.idCompte)) score -= 30;

            appliquerStyleScore(Math.min(100, Math.max(0, score)), null);
        } catch (Exception e) { resetScore(); }
    }

    private void appliquerStyleScore(int score, String forceMsg) {
        if (score == -1) {
            progressScore.setProgress(1.0);
            progressScore.setStyle("-fx-accent: #e74c3c;");
            lblScore.setText(forceMsg);
            lblScore.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            double ratio = score / 100.0;
            progressScore.setProgress(ratio);
            if (score < 40) {
                progressScore.setStyle("-fx-accent: #e74c3c;");
                lblScore.setText("Risqué (" + score + "%)");
                lblScore.setStyle("-fx-text-fill: #e74c3c;");
            } else if (score < 70) {
                progressScore.setStyle("-fx-accent: #f1c40f;");
                lblScore.setText("Moyen (" + score + "%)");
                lblScore.setStyle("-fx-text-fill: #f1c40f;");
            } else {
                progressScore.setStyle("-fx-accent: #27ae60;");
                lblScore.setText("Excellent (" + score + "%)");
                lblScore.setStyle("-fx-text-fill: #27ae60;");
            }
        }
    }

    private void resetScore() {
        progressScore.setProgress(0);
        lblScore.setText("Analyse...");
        lblScore.setStyle("-fx-text-fill: #999;");
        progressScore.setStyle("-fx-accent: #dfe6e9;");
    }

    @FXML
    private void handleConfirmer() {
        if (!validerSaisie()) return;

        try {
            Credit c = new Credit();
            c.setCompte(this.idCompte);
            c.setMontant(Double.parseDouble(txtMontant.getText().replace(",", ".")));
            c.setTauxInteret(Double.parseDouble(txtTaux.getText().replace(",", ".")));
            c.setDureeMois(Integer.parseInt(txtDuree.getText()));
            c.setDateDebut(java.time.LocalDate.now());

            if (progressScore.getProgress() >= 0.5 && !lblScore.getText().contains("REFUSÉ")) {
                c.setStatut("EN_COURS");
            } else {
                c.setStatut("REFUSE");
            }

            String mStr = lblMensualite.getText().replaceAll("[^0-9.,]", "").replace(",", ".");
            c.setMensualite(Double.parseDouble(mStr));

            // Ajout DB
            service.ajouter(c);

            // LOGIQUE SMS
            String phone = txtTelephone.getText().trim();
            if (!phone.isEmpty()) {
                envoyerSmsNotification(phone, c.getStatut(), c.getMontant());
            }

            Alert success = new Alert(Alert.AlertType.INFORMATION, "Traitement terminé. Statut : " + c.getStatut());
            success.showAndWait();
            handleAnnuler();

        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    private void envoyerSmsNotification(String toPhone, String statut, double montant) {
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            String corpsMessage = statut.equals("EN_COURS")
                    ? "Fintrack: Votre demande de credit de " + montant + " DT est ACCEPTEE."
                    : "Fintrack: Desole, votre demande de credit de " + montant + " DT a ete REFUSEE.";

            Message.creator(new PhoneNumber(toPhone), new PhoneNumber(TWILIO_NUMBER), corpsMessage).create();
            System.out.println("SMS envoyé !");
        } catch (Exception e) {
            System.err.println("Échec SMS: " + e.getMessage());
        }
    }

    private boolean validerSaisie() {
        try {
            Double.parseDouble(txtMontant.getText().replace(",", "."));
            Double.parseDouble(txtTaux.getText().replace(",", "."));
            Integer.parseInt(txtDuree.getText());

            String phone = txtTelephone.getText().trim();
            if (!phone.isEmpty() && !phone.matches("^\\+[1-9]\\d{1,14}$")) {
                showError("Le format du téléphone doit être international (ex: +21655123456)");
                return false;
            }
            return true;
        } catch (Exception e) {
            showError("Veuillez remplir les champs obligatoires avec des nombres valides.");
            return false;
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.showAndWait();
    }

    @FXML
    private void handleAnnuler() {
        if (parentStack != null) {
            parentStack.getChildren().removeAll(overlay, self);
            if (onCloseListener != null) onCloseListener.onPopupClosed();
        }
    }
}