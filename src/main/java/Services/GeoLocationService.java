package Services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GeoLocationService {

    // API gratuite sans clé - https://ipapi.co
    private static final String API_URL = "https://ipapi.co/json/";

    // API de secours si la première échoue
    private static final String FALLBACK_API_URL = "http://ip-api.com/json/";

    // Cache pour éviter trop d'appels
    private static String cachedCountry = null;
    private static String cachedCity = null;
    private static long lastUpdate = 0;
    private static final long CACHE_DURATION = 3600000; // 1 heure en millisecondes

    // Liste des pays autorisés pour les transferts
    private static final List<String> ALLOWED_COUNTRIES = Arrays.asList(
            "TN", "Tunisia", "Tunisie",  // Tunisie
            "FR", "France",               // France
            "BE", "Belgique", "Belgium",  // Belgique
            "CH", "Suisse", "Switzerland",// Suisse
            "CA", "Canada",                // Canada
            "MA", "Maroc", "Morocco",     // Maroc
            "DZ", "Algérie", "Algeria",   // Algérie
            "IT", "Italie", "Italy",       // Italie
            "ES", "Espagne", "Spain",      // Espagne
            "DE", "Allemagne", "Germany",  // Allemagne
            "GB", "Royaume-Uni", "United Kingdom", // Royaume-Uni
            "US", "États-Unis", "United States"    // États-Unis
    );

    /**
     * Récupère la localisation actuelle via API
     * @return LocationResult contenant pays, ville et statut
     */
    public static LocationResult getCurrentLocation() {
        // Vérifier le cache
        if (System.currentTimeMillis() - lastUpdate < CACHE_DURATION && cachedCountry != null) {
            System.out.println("📍 Utilisation du cache: " + cachedCountry + ", " + cachedCity);
            return new LocationResult(true, cachedCountry, cachedCity, "Cache");
        }

        // Essayer l'API principale
        try {
            LocationResult result = getLocationFromAPI(API_URL);
            if (result.isSuccess()) {
                cachedCountry = result.getCountry();
                cachedCity = result.getCity();
                lastUpdate = System.currentTimeMillis();
                return result;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur API principale: " + e.getMessage());
        }

        // Essayer l'API de secours
        try {
            LocationResult result = getLocationFromAPIFallback(FALLBACK_API_URL);
            if (result.isSuccess()) {
                cachedCountry = result.getCountry();
                cachedCity = result.getCity();
                lastUpdate = System.currentTimeMillis();
                return result;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur API secours: " + e.getMessage());
        }

        return new LocationResult(false, null, null, "Impossible de déterminer la localisation");
    }

    private static LocationResult getLocationFromAPI(String apiUrl) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(apiUrl);

            String response = client.execute(request, httpResponse ->
                    EntityUtils.toString(httpResponse.getEntity())
            );

            JsonObject json = JsonParser.parseString(response).getAsJsonObject();

            if (json.has("error")) {
                return new LocationResult(false, null, null, json.get("reason").getAsString());
            }

            String country = json.has("country_name") ? json.get("country_name").getAsString() :
                    (json.has("country") ? json.get("country").getAsString() : null);
            String city = json.has("city") ? json.get("city").getAsString() : null;

            if (country != null && !country.isEmpty()) {
                return new LocationResult(true, country, city, "Succès");
            }

            return new LocationResult(false, null, null, "Données incomplètes");
        }
    }

    private static LocationResult getLocationFromAPIFallback(String apiUrl) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(apiUrl);

            String response = client.execute(request, httpResponse ->
                    EntityUtils.toString(httpResponse.getEntity())
            );

            JsonObject json = JsonParser.parseString(response).getAsJsonObject();

            if (json.has("status") && json.get("status").getAsString().equals("success")) {
                String country = json.has("country") ? json.get("country").getAsString() : null;
                String city = json.has("city") ? json.get("city").getAsString() : null;

                if (country != null) {
                    return new LocationResult(true, country, city, "Succès");
                }
            }

            return new LocationResult(false, null, null, "Échec API secours");
        }
    }

    /**
     * Vérifie si le pays actuel est autorisé pour les transferts
     */
    public static boolean isCurrentLocationAllowed() {
        LocationResult location = getCurrentLocation();
        if (!location.isSuccess()) {
            System.out.println("⚠️ Impossible de vérifier la localisation");
            return false;
        }

        // Vérifier si le pays est dans la liste autorisée
        boolean allowed = ALLOWED_COUNTRIES.stream()
                .anyMatch(c -> location.getCountry().toLowerCase().contains(c.toLowerCase()) ||
                        c.toLowerCase().contains(location.getCountry().toLowerCase()));

        if (allowed) {
            System.out.println("✅ Localisation autorisée: " + location.getCountry() + ", " + location.getCity());
        } else {
            System.out.println("❌ Localisation non autorisée: " + location.getCountry());
        }

        return allowed;
    }

    /**
     * Vérifie si la localisation est valide pour un transfert
     */
    public static boolean isLocationValidForTransfer() {
        LocationResult location = getCurrentLocation();

        if (!location.isSuccess()) {
            System.out.println("⚠️ Impossible de vérifier la localisation");
            return false;
        }

        return isCurrentLocationAllowed();
    }

    /**
     * Retourne un message d'erreur approprié
     */
    public static String getLocationErrorMessage() {
        LocationResult location = getCurrentLocation();

        if (!location.isSuccess()) {
            return "❌ Impossible de vérifier votre localisation: " + location.getMessage();
        }

        if (!isCurrentLocationAllowed()) {
            return "❌ Les transferts ne sont pas autorisés depuis " + location.getCountry();
        }

        return null; // Pas d'erreur
    }

    /**
     * Classe pour stocker le résultat de localisation
     */
    public static class LocationResult {
        private final boolean success;
        private final String country;
        private final String city;
        private final String message;

        public LocationResult(boolean success, String country, String city, String message) {
            this.success = success;
            this.country = country;
            this.city = city;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getCountry() { return country; }
        public String getCity() { return city; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return String.format("Location[success=%s, country=%s, city=%s]", success, country, city);
        }
    }
}