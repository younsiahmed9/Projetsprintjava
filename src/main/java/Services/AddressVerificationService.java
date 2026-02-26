package Services;

import java.util.Arrays;
import java.util.List;

public class AddressVerificationService {

    // Liste des pays autorisés pour les transferts
    private static final List<String> ALLOWED_COUNTRIES = Arrays.asList(
            "Tunisie", "France", "Belgique", "Suisse", "Canada", "Maroc", "Algérie",
            "Italie", "Espagne", "Allemagne", "Royaume-Uni", "États-Unis"
    );

    /**
     * Vérifie si un pays est autorisé pour les transferts
     */
    public static boolean isCountryAllowed(String country) {
        if (country == null || country.isEmpty()) {
            System.out.println("⚠️ Pays non renseigné");
            return false;
        }
        return ALLOWED_COUNTRIES.contains(country);
    }

    /**
     * Vérifie si une ville est autorisée (toujours true, mais peut être étendu)
     */
    public static boolean isCityAllowed(String city) {
        if (city == null || city.isEmpty()) {
            System.out.println("⚠️ Ville non renseignée");
            return false;
        }
        return true;
    }

    /**
     * Vérifie si l'adresse est valide pour les transferts (seulement pays et ville)
     */
    public static boolean isAddressValidForTransfer(String country, String city) {
        if (!isCountryAllowed(country)) {
            System.out.println("⚠️ Pays non autorisé: " + country);
            return false;
        }

        if (!isCityAllowed(city)) {
            System.out.println("⚠️ Ville non autorisée: " + city);
            return false;
        }

        System.out.println("✅ Pays et ville valides: " + country + ", " + city);
        return true;
    }

    /**
     * Retourne un message d'erreur approprié
     */
    public static String getErrorMessage(String country, String city) {
        if (country == null || country.isEmpty()) {
            return "❌ Pays non renseigné. Veuillez mettre à jour votre profil.";
        }
        if (!isCountryAllowed(country)) {
            return "❌ Pays non autorisé pour les transferts: " + country;
        }
        if (city == null || city.isEmpty()) {
            return "❌ Ville non renseignée. Veuillez mettre à jour votre profil.";
        }
        return "❌ Adresse invalide pour les transferts.";
    }
}