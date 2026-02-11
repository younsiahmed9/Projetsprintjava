import Services.*;
import Models.*;
import utils.MyDataBase;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static PortefeuilleService portefeuilleService = new PortefeuilleService();
    private static CarteVirtuelleService carteService = new CarteVirtuelleService();

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("        🚀 FINTRACK - GESTION PORTEFEUILLES & CARTES");
        System.out.println("=".repeat(60));

        // Connexion DB
        if (MyDataBase.getInstance().getConnection() == null) {
            System.out.println("❌ Connexion échouée!");
            return;
        }
        System.out.println("✅ Connecté à la base de données\n");

        while (true) {
            menuPrincipal();
        }
    }

    private static void menuPrincipal() {
        System.out.println("\n📌 MENU PRINCIPAL:");
        System.out.println("1. 💼 Créer un portefeuille");
        System.out.println("2. 💳 Créer une carte virtuelle");
        System.out.println("3. 📋 Voir tous les portefeuilles");
        System.out.println("4. 📋 Voir toutes les cartes");
        System.out.println("5. 💰 Recharger une carte");
        System.out.println("6. ❌ Quitter");
        System.out.print("\nVotre choix: ");

        int choix = lireInt();

        switch (choix) {
            case 1 -> creerPortefeuille();
            case 2 -> creerCarte();
            case 3 -> portefeuilleService.afficherTous();
            case 4 -> carteService.afficherTous();
            case 5 -> rechargerCarte();
            case 6 -> {
                System.out.println("👋 Au revoir!");
                System.exit(0);
            }
            default -> System.out.println("❌ Choix invalide!");
        }
    }

    // ✅ SCÉNARIO 1: Créer un portefeuille
    private static void creerPortefeuille() {
        System.out.println("\n💼 CRÉATION D'UN PORTEFEUILLE");
        System.out.print("Nom du portefeuille: ");
        String nom = scanner.nextLine();
        System.out.print("Devise principale (DT/USD/EUR): ");
        String devise = scanner.nextLine().toUpperCase();
        System.out.print("Solde initial: ");
        double solde = lireDouble();

        Portefeuille p = new Portefeuille(nom, solde, devise);
        portefeuilleService.ajouter(p);

        System.out.println("✅ Portefeuille créé avec succès!");
    }

    // ✅ SCÉNARIO 2: Créer une carte (associée à un portefeuille existant)
    private static void creerCarte() {
        System.out.println("\n💳 CRÉATION D'UNE CARTE VIRTUELLE");

        // Afficher les portefeuilles disponibles
        System.out.println("\n📋 Portefeuilles disponibles:");
        List<Portefeuille> portefeuilles = portefeuilleService.afficherTous();

        if (portefeuilles.isEmpty()) {
            System.out.println("❌ Aucun portefeuille! Créez-en un d'abord.");
            return;
        }

        System.out.print("\nID du portefeuille: ");
        int portefeuilleId = lireInt();

        // Vérifier si le portefeuille existe
        Portefeuille portefeuille = portefeuilleService.afficherParId(portefeuilleId);
        if (portefeuille == null) {
            System.out.println("❌ Portefeuille introuvable!");
            return;
        }

        System.out.print("Type de carte (NORMAL/GOLD/SILVER): ");
        String type = scanner.nextLine().toUpperCase();
        System.out.print("Devise (DT/USD/EUR): ");
        String devise = scanner.nextLine().toUpperCase();
        System.out.print("Solde initial: ");
        double solde = lireDouble();
        System.out.print("Plafond: ");
        double plafond = lireDouble();

        CarteVirtuelle carte = new CarteVirtuelle(
                solde, plafond,
                TypeCarte.valueOf(type),
                Devise.valueOf(devise),
                portefeuilleId
        );

        carteService.ajouter(carte);
    }

    // ✅ Recharger une carte
    private static void rechargerCarte() {
        System.out.println("\n💰 RECHARGER UNE CARTE");

        System.out.print("ID de la carte: ");
        int carteId = lireInt();
        System.out.print("Montant à recharger: ");
        double montant = lireDouble();

        carteService.recharger(carteId, montant);
    }

    // Utilitaires
    private static int lireInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Veuillez entrer un nombre valide: ");
            }
        }
    }

    private static double lireDouble() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Veuillez entrer un nombre valide: ");
            }
        }
    }
}