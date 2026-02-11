package Services;

public class ConversionDevise {
    private static final double USD_TO_DT = 3.1;
    private static final double EUR_TO_DT = 3.4;

    public static double convertir(double montant, String source, String cible) {
        if (source.equals(cible)) return montant;

        double montantDT = switch (source) {
            case "USD" -> montant * USD_TO_DT;
            case "EUR" -> montant * EUR_TO_DT;
            default -> montant;
        };

        return switch (cible) {
            case "USD" -> montantDT / USD_TO_DT;
            case "EUR" -> montantDT / EUR_TO_DT;
            default -> montantDT;
        };
    }

    public static void afficherTaux() {
        System.out.println("💰 1 USD = 3.1 DT | 1 EUR = 3.4 DT");
    }
}