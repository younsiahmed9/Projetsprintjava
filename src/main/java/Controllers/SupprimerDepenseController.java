package Controllers;

import Models.Depense;
import Services.ServiceDepense;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLDataException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

public class SupprimerDepenseController {

    @FXML private TextField txtSearchId;
    @FXML private VBox depenseDetailsBox;
    @FXML private TextArea txtDetails;

    private final ServiceDepense service = new ServiceDepense();
    private Depense depenseActuelle = null;

    @FXML
    private void initialize() {
        depenseDetailsBox.setDisable(true);
    }

    @FXML
    private void onRechercher() {
        try {
            int id = Integer.parseInt(txtSearchId.getText().trim());
            List<Depense> depenses = service.recuperer();
            depenseActuelle = depenses.stream()
                    .filter(d -> d.getIdDepense() == id)
                    .findFirst()
                    .orElse(null);

            if (depenseActuelle != null) {
                afficherDetails(depenseActuelle);
                depenseDetailsBox.setDisable(false);
            } else {
                showError("Dépense non trouvée avec l'ID: " + id);
                depenseDetailsBox.setDisable(true);
            }
        } catch (NumberFormatException e) {
            showError("Veuillez entrer un ID valide");
        } catch (SQLDataException e) {
            showError("Erreur base de données: " + e.getMessage());
        }
    }

    private void afficherDetails(Depense d) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String details = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
                + "ID Dépense: " + d.getIdDepense() + "\n"
                + "ID Utilisateur: " + d.getIdUtilisateur() + "\n"
                + "ID Budget: " + d.getIdBudget() + "\n"
                + "Catégorie: " + d.getCategorie() + "\n"
                + "Montant: " + String.format("%.2f", d.getMontant()) + " €\n"
                + "Date: " + sdf.format(d.getDateDepense()) + "\n"
                + "Description: " + d.getDescription() + "\n"
                + "Mode Paiement: " + d.getModePaiement() + "\n"
                + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
        txtDetails.setText(details);
    }

    @FXML
    private void onSupprimer() {
        if (depenseActuelle == null) {
            showError("Aucune dépense sélectionnée");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la dépense");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette dépense?\nCette action est irréversible.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(depenseActuelle.getIdDepense());
                showInfo("Dépense supprimée avec succès!");
                chargerMainPage();
            } catch (SQLDataException e) {
                showError("Erreur base de données: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onAnnuler() {
        chargerMainPage();
    }

    private void chargerMainPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainPage.fxml"));
            AnchorPane main = loader.load();
            AnchorPane parent = (AnchorPane) txtSearchId.getScene().getRoot();
            parent.getChildren().clear();
            parent.getChildren().add(main);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la page principale: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
