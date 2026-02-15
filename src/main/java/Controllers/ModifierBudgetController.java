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

public class ModifierBudgetController implements Initializable {

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

    private MainPageController mainPageController;
    private Budget budgetEnCours;

    public void setMainPageController(MainPageController controller) {
        this.mainPageController = controller;
    }

    public void setBudget(Budget budget) {
        this.budgetEnCours = budget;
        if (budget != null) {
            txtIdUtilisateur.setText(String.valueOf(budget.getIdUtilisateur()));
            txtNom.setText(budget.getNomBudget());
            txtMontant.setText(String.valueOf(budget.getMontantTotal()));
            comboPeriode.setValue(budget.getPeriode());
            comboStatut.setValue(budget.getStatut());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboPeriode.getItems().addAll("mensuel", "annuel");
        comboStatut.getItems().addAll("actif", "clôturé");
        lblError.setText("");
    }

    @FXML
    private void onEnregistrer() {
        lblError.setText("");

        String nom = txtNom.getText().trim();
        String montantText = txtMontant.getText().trim();
        String periode = comboPeriode.getValue();
        String statut = comboStatut.getValue();

        if (nom.isEmpty() || montantText.isEmpty() || periode == null || statut == null) {
            lblError.setText("Tous les champs sont obligatoires.");
            return;
        }

        try {
            double montant = Double.parseDouble(montantText);

            if (montant <= 0) {
                lblError.setText("Le montant doit être positif.");
                return;
            }

            budgetEnCours.setNomBudget(nom);
            budgetEnCours.setMontantTotal(montant);
            budgetEnCours.setPeriode(periode);
            budgetEnCours.setStatut(statut);

            ServiceBudget serviceBudget = new ServiceBudget();
            serviceBudget.modifier(budgetEnCours);

            System.out.println("Budget modifié avec succès!");

            if (mainPageController != null) {
                mainPageController.refreshBudgets();
                mainPageController.retournerAListe();
            }

            onAnnuler();

        } catch (NumberFormatException e) {
            lblError.setText("Le montant doit être un nombre valide.");
        } catch (Exception e) {
            System.err.println("Erreur lors de la modification du budget:");
            e.printStackTrace();
        }
    }

    @FXML
    private void onAnnuler() {
        if (budgetEnCours != null) {
            setBudget(budgetEnCours);
        }
        lblError.setText("");
    }

    @FXML
    private void onRetour() {
        if (mainPageController != null) {
            mainPageController.retournerAListe();
        }
    }
}

