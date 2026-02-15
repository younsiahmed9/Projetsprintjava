package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import Models.Depense;
import java.util.Date;

public class DepenseController {

    // Champs liés au formulaire
    @FXML private TextField txtUtilisateur;
    @FXML private TextField txtBudget;
    @FXML private TextField txtCategorie;
    @FXML private TextField txtMontant;
    @FXML private DatePicker dateDepense;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtModePaiement;

    // TableView et colonnes
    @FXML private TableView<Depense> tableDepenses;
    @FXML private TableColumn<Depense, Integer> colIDDepense;
    @FXML private TableColumn<Depense, Integer> colIDUtilisateur;
    @FXML private TableColumn<Depense, Integer> colIDBudget;
    @FXML private TableColumn<Depense, String> colCategorie;
    @FXML private TableColumn<Depense, Double> colMontant;
    @FXML private TableColumn<Depense, Date> colDateDepense;
    @FXML private TableColumn<Depense, String> coltxtDescription;
    @FXML private TableColumn<Depense, String> colModePaiement;

    // Méthodes liées aux boutons
    @FXML
    private void ajouterDepense() {
        System.out.println("Ajout dépense : " + txtCategorie.getText());
        // Ici tu appelles ServiceDepense.ajouter(...)
    }

    @FXML
    private void modifierDepense() {
        System.out.println("Modification dépense sélectionnée");
        // Ici tu appelles ServiceDepense.modifier(...)
    }

    @FXML
    private void supprimerDepense() {
        System.out.println("Suppression dépense sélectionnée");
        // Ici tu appelles ServiceDepense.supprimer(...)
    }

    @FXML
    private void chargerDepenses() {
        System.out.println("Chargement des dépenses...");
        // Ici tu appelles ServiceDepense.recuperer() et tu remplis la TableView
    }
}
