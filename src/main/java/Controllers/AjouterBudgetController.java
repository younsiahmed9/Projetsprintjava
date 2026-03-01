package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.ResourceBundle;
import Models.Budget;
import Services.ServiceBudget;

public class AjouterBudgetController implements Initializable {

    @FXML
    private TextField txtIdUtilisateur;

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtMontant;

    @FXML
    private ComboBox<String> comboPeriode;

    @FXML
    private ComboBox<String> comboStatut;

    @FXML
    private Label lblError;

    // Référence vers le contrôleur principal
    private MainPageController mainPageController;

    // Setter appelé depuis MainPageController
    public void setMainPageController(MainPageController controller) {
        this.mainPageController = controller;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les ComboBox avec les valeurs
        comboPeriode.getItems().addAll("mensuel", "annuel");
        comboStatut.getItems().addAll("actif", "clôturé");

        comboPeriode.setValue("mensuel");
        comboStatut.setValue("actif");

        // Pré-remplir l'ID utilisateur
        txtIdUtilisateur.setText("1");
        lblError.setText("");
    }

    @FXML
    private void onEnregistrer() {
        lblError.setText("");

        String idUtilisateurText = txtIdUtilisateur.getText().trim();
        String nom = txtNom.getText().trim();
        String montantText = txtMontant.getText().trim();
        String periode = comboPeriode.getValue();
        String statut = comboStatut.getValue();

        // Vérification des champs vides
        if (idUtilisateurText.isEmpty() || nom.isEmpty() || montantText.isEmpty()) {
            lblError.setText("Tous les champs sont obligatoires.");
            return;
        }

        // Vérification format numérique
        int idUtilisateur;
        double montant;
        try {
            idUtilisateur = Integer.parseInt(idUtilisateurText);
            montant = Double.parseDouble(montantText);
        } catch (NumberFormatException e) {
            lblError.setText("ID utilisateur et montant doivent être des nombres valides.");
            return;
        }

        // Vérification valeurs positives
        if (idUtilisateur <= 0) {
            lblError.setText("L'ID utilisateur doit être positif.");
            return;
        }
        if (montant <= 0) {
            lblError.setText("Le montant doit être supérieur à zéro.");
            return;
        }

        // Vérification longueur du nom
        if (nom.length() < 3) {
            lblError.setText("Le nom du budget doit contenir au moins 3 caractères.");
            return;
        }

        // Vérification choix période/statut
        if (periode == null || statut == null) {
            lblError.setText("Veuillez sélectionner une période et un statut.");
            return;
        }

        // Si tout est correct → création du budget
        try {
            Budget budget = new Budget();
            budget.setIdUtilisateur(idUtilisateur);
            budget.setNomBudget(nom);
            budget.setMontantTotal(montant);
            budget.setPeriode(periode);
            budget.setStatut(statut);
            budget.setDateCreation(new java.util.Date());

            ServiceBudget serviceBudget = new ServiceBudget();
            serviceBudget.ajouter(budget);

            System.out.println("Budget ajouté avec succès!");

            if (mainPageController != null) {
                mainPageController.refreshBudgets();
                mainPageController.retournerAListe();
            }

            onAnnuler();

        } catch (Exception e) {
            lblError.setText("Erreur lors de l'ajout du budget.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onAnnuler() {
        txtIdUtilisateur.setText("1");
        txtNom.clear();
        txtMontant.clear();
        comboPeriode.setValue("mensuel");
        comboStatut.setValue("actif");
        lblError.setText("");
    }

    @FXML
    private void onRetour() {
        if (mainPageController != null) {
            mainPageController.retournerAListe();
        }
    }
}
