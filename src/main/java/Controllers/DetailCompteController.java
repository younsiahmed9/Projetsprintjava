package Controllers;

import Models.Compte;
import Services.ServiceCompte;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class DetailCompteController {

    @FXML private Label lblNumero, lblType, lblSolde, lblEtat, lblSpecifiqueTitre, lblSpecifiqueValeur;
    @FXML private HBox rowSpecifique;
    @FXML private Button btnCredits; // <--- CETTE LIGNE EST INDISPENSABLE

    private Compte compteActuel;
    private ServiceCompte service = new ServiceCompte();

    // Cette méthode sera appelée depuis AfficherCompteController
    public void setCompteId(int id) {
        try {
            this.compteActuel = service.recupererParId(id);
            remplirChamps();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void remplirChamps() {
        if (compteActuel != null) {
            lblNumero.setText(compteActuel.getNumeroCompte());
            lblType.setText("COMPTE " + compteActuel.getTypeCompte().toUpperCase());
            lblSolde.setText(String.format("%.2f DT", compteActuel.getSolde()));

            // Style du badge d'état
            lblEtat.setText(compteActuel.getEtat().toUpperCase());
            lblEtat.getStyleClass().removeAll("badge-actif", "badge-bloque");
            if(compteActuel.getEtat().equalsIgnoreCase("ACTIF")) {
                lblEtat.getStyleClass().add("badge-actif");
            } else {
                lblEtat.getStyleClass().add("badge-bloque");
            }

            // Gestion de l'affichage dynamique (Taux vs Plafond)
            if ("EPARGNE".equalsIgnoreCase(compteActuel.getTypeCompte())) {
                lblSpecifiqueTitre.setText("TAUX D'INTÉRÊT ANNUEL");
                lblSpecifiqueValeur.setText(compteActuel.getTauxInteret() + " %");

                // --- LOGIQUE CRÉDITS ---
                btnCredits.setVisible(false);
                btnCredits.setManaged(false); // Retire l'espace du bouton
            } else {
                lblSpecifiqueTitre.setText("PLAFOND DE DÉCOUVERT");
                lblSpecifiqueValeur.setText(compteActuel.getPlafondDecouvert() + " DT");

                // --- LOGIQUE CRÉDITS ---
                btnCredits.setVisible(true);
                btnCredits.setManaged(true);
            }
        }
    }

    @FXML
    private void handleModifier() {
        VBox root = (VBox) lblNumero.getScene().getRoot();
        root.setOpacity(0.5); // Fond grisé

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifierCompte.fxml"));
            Parent editRoot = loader.load();

            ModifierCompteController controller = loader.getController();
            controller.setCompte(compteActuel); // On passe l'objet compte

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(editRoot);
            scene.setFill(null);
            stage.setScene(scene);
            stage.showAndWait();

            // Si modification enregistrée, on rafraîchit les labels
            if (controller.isModificationReussie()) {
                lblNumero.setText(compteActuel.getNumeroCompte());
                lblSolde.setText(compteActuel.getSolde() + " DT");
                lblEtat.setText(compteActuel.getEtat());

                // Si tu as un label pour le type/détails spécifiques :
                if ("EPARGNE".equals(compteActuel.getTypeCompte())) {
                    // lblSpecifique.setText(compteActuel.getTauxInteret() + "%");
                } else {
                    // lblSpecifique.setText(compteActuel.getPlafondDecouvert() + " DT");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            root.setOpacity(1.0); // Retour à la normale
        }
    }
    @FXML
    private void handleSupprimer() {
        // 1. On récupère la racine de l'interface de détails (ta VBox)
        // On utilise n'importe quel élément injecté (ex: lblNumero) pour remonter à la racine
        VBox root = (VBox) lblNumero.getScene().getRoot();

        // 2. On applique l'effet gris (opacité réduite)
        root.setOpacity(0.4);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SupprimerCompte.fxml"));
            Parent popupRoot = loader.load();

            SupprimerCompteController controller = loader.getController();
            controller.setCompte(compteActuel);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(popupRoot);
            scene.setFill(null); // Pour la transparence des coins arrondis
            stage.setScene(scene);

            stage.showAndWait();

            // 3. Après fermeture de la popup, si supprimé, on ferme les détails
            if (controller.isSuppressionConfirmee()) {
                handleFermer();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 4. TRÈS IMPORTANT : On remet l'opacité à 100% quoi qu'il arrive
            root.setOpacity(1.0);
        }
    }
    @FXML
    private void handleConsulterCredits() {
        try {
            // 1. Assure-toi de charger le fichier de la LISTE (AfficherCredit)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AfficherCredit.fxml"));
            Parent root = loader.load();

            // 2. Récupère le BON contrôleur (celui de l'affichage)
            AfficherCreditController controller = loader.getController();

            // 3. Passe les données à la liste
            controller.setCompteData(compteActuel.getIdCompte(), compteActuel.getNumeroCompte());

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Tableau de bord - Crédits");
            stage.setMaximized(true);
            stage.show();

            handleFermer();
        } catch (IOException e) {
            System.err.println("Erreur : Impossible de charger /fxml/AfficherCredit.fxml");
            e.printStackTrace();
        } catch (ClassCastException e) {
            System.err.println("Erreur de Cast : Vérifiez la balise fx:controller dans AfficherCredit.fxml");
            e.printStackTrace();
        }
    }
    @FXML private void handleFermer() {
        Stage stage = (Stage) lblNumero.getScene().getWindow();
        stage.close();
    }
}