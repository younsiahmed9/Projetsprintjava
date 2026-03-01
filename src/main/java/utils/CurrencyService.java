package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class CurrencyService {
    // API Key gratuite (utilisation d'une clé publique ou invité si possible, sinon
    // simulation pour le test)
    // Note: Pour une mise en prod, l'utilisateur devra insérer sa propre clé
    // d'ExchangeRate-API
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/latest/";
    private static final String DEFAULT_KEY = "eb4817457a41400693a652f1"; // Clé d'exemple/test

    private static final Map<String, Double> cache = new HashMap<>();
    private static long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 3600000; // 1 heure en ms

    public static double getExchangeRate(String from, String to) {
        if (from.equals(to))
            return 1.0;

        // Système de cache simple pour éviter de saturer l'API gratuite
        String cacheKey = from + "_" + to;
        if (System.currentTimeMillis() - lastCacheUpdate < CACHE_DURATION && cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        try {
            String urlStr = API_URL + DEFAULT_KEY + "/" + from;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject rates = jsonResponse.getJSONObject("conversion_rates");

                double rate = rates.getDouble(to);
                cache.put(cacheKey, rate);
                lastCacheUpdate = System.currentTimeMillis();
                return rate;
            }
        } catch (Exception e) {
            System.err.println("Erreur API Taux de Change: " + e.getMessage());
        }

        // Taux par défaut indicatifs en cas d'échec (Offline)
        if (from.equals("EUR") && to.equals("TND"))
            return 3.40;
        if (from.equals("USD") && to.equals("TND"))
            return 3.15;

        return 1.0;
    }

    public static double convert(double amount, String from, String to) {
        double rate = getExchangeRate(from, to);
        return amount * rate;
    }
}
