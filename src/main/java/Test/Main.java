package Test;

import Models.Budget;
import Models.Depense;
import Services.ServiceBudget;
import Services.ServiceDepense;

import java.util.Date;
import java.sql.SQLDataException;

public class Main {
    public static void main(String[] args) {
        ServiceBudget sb = new ServiceBudget();
        ServiceDepense sd = new ServiceDepense();

        try {
            // Instance Budget
            Budget budget = new Budget(
                    25,
                    "Budget Mensuel Mars",
                    2000.00,
                    "mensuel",
                    new Date(),
                    "actif"
            );

            sb.ajouter(budget);
            System.out.println("Budget ajouté : " + budget);

            // Instance Dépense liée au budget
            Depense depense = new Depense(
                    0,                       // idDepense (0 si auto-incrémenté en base)
                    1,                       // idUtilisateur
                    2,    // idBudget lié au budget
                    "Alimentation",          // catégorie
                    150.00,                  // montant
                    new Date(),              //  dateDepense initialisée
                    "Courses supermarché",   // description
                    "carte"                  // mode de pavement
            );

            sd.ajouter(depense);
            System.out.println("Dépense ajoutée : " + depense);


            sd.ajouter(depense);
            System.out.println("Dépense ajoutée : " + depense);

        } catch (SQLDataException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
        }
    }
}
