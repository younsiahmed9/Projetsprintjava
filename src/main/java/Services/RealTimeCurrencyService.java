package Services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealTimeCurrencyService {

    // Remplacez par votre clé API gratuite depuis https://www.exchangerate-api.com/
    private static final String API_KEY = "VOTRE_CLE_API";
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";

    // Cache pour éviter trop d'appels API
    private static Map<String, Double> ratesCache = new HashMap<>();
    private static LocalDateTime lastUpdate = null;
    private static final int CACHE_MINUTES = 60; // Mise à jour toutes les heures

    // Devises supportées
    private static final List<String> SUPPORTED_CURRENCIES = Arrays.asList("TND", "USD", "EUR");

    /**
     * Récupère le taux de change entre deux devises en temps réel
     */
    public static double getExchangeRate(String fromCurrency, String toCurrency) throws IOException {
        // Si mêmes devises, taux = 1
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return 1.0;
        }

        // Normaliser les devises
        String from = fromCurrency.toUpperCase();
        String to = toCurrency.toUpperCase();

        // Vérifier le cache
        if (shouldRefreshCache()) {
            refreshRates(from);
        }

        // Calculer le taux
        Double fromRate = ratesCache.get(from);
        Double toRate = ratesCache.get(to);

        if (fromRate == null || toRate == null) {
            refreshRates(from);
            fromRate = ratesCache.get(from);
            toRate = ratesCache.get(to);
        }

        if (fromRate != null && toRate != null) {
            // Taux croisé : (1 unité de fromCurrency) * (toRate / fromRate)
            return toRate / fromRate;
        }

        throw new IOException("Devises non supportées: " + from + " ou " + to);
    }

    /**
     * Convertit un montant d'une devise à une autre en temps réel
     */
    public static double convert(double amount, String fromCurrency, String toCurrency) throws IOException {
        double rate = getExchangeRate(fromCurrency, toCurrency);
        return amount * rate;
    }

    /**
     * Rafraîchit le cache des taux
     */
    private static void refreshRates(String baseCurrency) throws IOException {
        String url = API_URL + baseCurrency.toUpperCase();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);

            String response = client.execute(request, httpResponse ->
                    EntityUtils.toString(httpResponse.getEntity())
            );

            // Parser JSON
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();

            if (json.get("result").getAsString().equals("success")) {
                JsonObject rates = json.getAsJsonObject("conversion_rates");

                ratesCache.clear();
                for (String currency : SUPPORTED_CURRENCIES) {
                    if (rates.has(currency)) {
                        ratesCache.put(currency, rates.get(currency).getAsDouble());
                    }
                }

                lastUpdate = LocalDateTime.now();
                System.out.println("✅ Taux de change mis à jour: " + lastUpdate);
            } else {
                throw new IOException("Erreur API: " + json.get("error-type").getAsString());
            }
        }
    }

    private static boolean shouldRefreshCache() {
        return lastUpdate == null ||
                lastUpdate.plusMinutes(CACHE_MINUTES).isBefore(LocalDateTime.now());
    }

    /**
     * Récupère le taux USD vers DT
     */
    public static double getUsdToDt() throws IOException {
        return getExchangeRate("USD", "DT");
    }

    /**
     * Récupère le taux EUR vers DT
     */
    public static double getEurToDt() throws IOException {
        return getExchangeRate("EUR", "DT");
    }

    /**
     * Affiche les taux actuels
     */
    public static void printCurrentRates() {
        try {
            double usdToDt = getUsdToDt();
            double eurToDt = getEurToDt();
            System.out.println("💰 Taux de change actuels:");
            System.out.println("   1 USD = " + String.format("%.2f", usdToDt) + " DT");
            System.out.println("   1 EUR = " + String.format("%.2f", eurToDt) + " DT");
        } catch (Exception e) {
            System.out.println("❌ Impossible de récupérer les taux actuels");
        }
    }
}