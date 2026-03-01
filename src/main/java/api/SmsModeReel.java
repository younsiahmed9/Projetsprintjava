package api;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SmsModeReel {

    private String apiKey;

    public SmsModeReel(String apiKey) {
        this.apiKey = apiKey;
        System.out.println("📱 SMSMode prêt à envoyer de vrais SMS");
    }

    public boolean envoyerSMS(String numero, String message) {
        try {
            // URL de l'API SMSMode
            URL url = new URL("https://api.smsmode.com/http/1.6/sendSMS.do");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            // Paramètres
            String params = "accessToken=" + URLEncoder.encode(apiKey, "UTF-8") +
                    "&numero=" + URLEncoder.encode(numero, "UTF-8") +
                    "&message=" + URLEncoder.encode(message, "UTF-8") +
                    "&emetteur=Demo";

            // Envoi
            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            // Lecture de la réponse
            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                System.out.println("✅ SMS RÉEL envoyé avec succès à " + numero);
                return true;
            } else {
                System.err.println("❌ Erreur " + responseCode);
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return false;
        }
    }

    public boolean envoyerPromotion(String numero, String produit, double ancienPrix, double nouveauPrix, int reduction) {
        String message = String.format(
                "PROMO EXCEPTIONNELLE ! %s\n%.2f€ au lieu de %.2f€ (-%d%%)",
                produit, nouveauPrix, ancienPrix, reduction
        );
        return envoyerSMS(numero, message);
    }
}