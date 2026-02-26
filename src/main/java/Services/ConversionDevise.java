package Services;

import Models.Devise;

public class ConversionDevise {

    // Taux fixes de secours (en cas d'échec API)
    private static final double USD_TO_DT = 3.1;
    private static final double EUR_TO_DT = 3.4;

    /**
     * Convertit un montant en utilisant les taux en temps réel
     */
    public static double convertir(double montant, String source, String cible) {
        try {
            return RealTimeCurrencyService.convert(montant, source, cible);
        } catch (Exception e) {
            System.err.println("⚠️ Erreur API taux de change, utilisation des taux fixes: " + e.getMessage());
            return convertirFixe(montant, source, cible);
        }
    }

    /**
     * Version avec les enums Devise
     */
    public static double convertir(double montant, Devise source, Devise cible) {
        return convertir(montant, source.name(), cible.name());
    }

    /**
     * Méthode de conversion avec taux fixes (rendue publique)
     */
    public static double convertirFixe(double montant, String source, String cible) {
        if (source.equalsIgnoreCase(cible)) {
            return montant;
        }

        double montantEnDT;
        switch (source.toUpperCase()) {
            case "USD": montantEnDT = montant * USD_TO_DT; break;
            case "EUR": montantEnDT = montant * EUR_TO_DT; break;
            case "DT": montantEnDT = montant; break;
            default:
                System.err.println("Devise source inconnue: " + source);
                return montant;
        }

        switch (cible.toUpperCase()) {
            case "USD": return montantEnDT / USD_TO_DT;
            case "EUR": return montantEnDT / EUR_TO_DT;
            case "DT": return montantEnDT;
            default:
                System.err.println("Devise cible inconnue: " + cible);
                return montantEnDT;
        }
    }

    /**
     * Version enum de la méthode fixe
     */
    public static double convertirFixe(double montant, Devise source, Devise cible) {
        return convertirFixe(montant, source.name(), cible.name());
    }

    public static void afficherTaux() {
        System.out.println("📡 Récupération des taux en temps réel...");
        try {
            RealTimeCurrencyService.printCurrentRates();
        } catch (Exception e) {
            System.out.println("💰 Taux fixes: 1 USD = " + USD_TO_DT + " DT | 1 EUR = " + EUR_TO_DT + " DT");
        }
    }
}