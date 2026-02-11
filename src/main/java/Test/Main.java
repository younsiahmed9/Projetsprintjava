package Test;

import Models.Compte;
import Models.Credit;
import Services.ServiceCompte;
import Services.ServiceCredit;

import utils.MyDatabase;

import java.time.LocalDate;
import java.sql.SQLDataException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ServiceCompte serviceCompte = new ServiceCompte();
        try {
            System.out.println("===== TEST AJOUT COMPTE =====");

            // ✅ Compte EPARGNE
            Compte epargne = new Compte(
                    "ACC001",
                    5000,
                    "EPARGNE",
                    3.5,        // taux interet
                    null,       // pas de plafond
                    LocalDate.now(),
                    "ACTIF"
            );

            // ✅ Compte COURANT
            Compte courant = new Compte(
                    "ACC002",
                    10000,
                    "COURANT",
                    null,       // pas de taux
                    2000.0,     // plafond decouvert
                    LocalDate.now(),
                    "ACTIF"
            );

            serviceCompte.ajouter(epargne);
            serviceCompte.ajouter(courant);

            System.out.println("===== TOUS LES COMPTES =====");
            System.out.println(serviceCompte.recuperer());

            System.out.println("===== TEST MODIFICATION =====");

            // ⚠ Mets un id existant dans ta base
            Compte modif = new Compte(
                    4, // ← change selon ton id réel
                    "ACC002-UPDATED",
                    15000,
                    "COURANT",
                    null,
                    3000.0,
                    LocalDate.now(),
                    "ACTIF"
            );

            serviceCompte.modifier(modif);

            System.out.println("===== APRES MODIFICATION =====");
            System.out.println(serviceCompte.recuperer());



        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }

        ServiceCredit serviceCredit = new ServiceCredit();
       try {
            // Ajouter des crédits
            Credit credit1 = new Credit(2000, 5.5, 12, 200, LocalDate.now(), "EN_COURS", new Compte(4));
            Credit credit2 = new Credit(5000, 4.5, 24, 220, LocalDate.now(), "EN_COURS", new Compte(4));

            serviceCredit.ajouter(credit1);
            serviceCredit.ajouter(credit2);

            // Modifier un crédit (id = 1)
            serviceCredit.modifier(new Credit(2500, 5.5, 12, 210, LocalDate.now(), "EN_COURS", new Compte(4), 1));

            // Afficher tous les crédits
            System.out.println(serviceCredit.recuperer());

            // Supprimer un crédit (id = 2)
            serviceCredit.supprimer(new Credit(2));

        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }

    }
}
