package api;

import config.APIConfig.ApiConfig;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SmsClient {

    private String provider;

    public SmsClient() {
        this.provider = ApiConfig.getSmsProvider();
        System.out.println("📱 Client SMS initialisé avec: " + provider);
    }

    /**
     * Envoie un SMS via le fournisseur configuré
     */
    public boolean sendSms(String to, String message) {
        if (ApiConfig.isSimulationMode()) {
            return simulateSms(to, message);
        } else if (ApiConfig.isTwilioMode()) {
            return sendViaTwilio(to, message);
        } else if (ApiConfig.isInfobipMode()) {
            return sendViaInfobip(to, message);
        } else {
            return simulateSms(to, message);
        }
    }

    /**
     * Envoie un SMS via Twilio
     */
    private boolean sendViaTwilio(String to, String message) {
        try {
            String accountSid = ApiConfig.getTwilioAccountSid();
            String authToken = ApiConfig.getTwilioAuthToken();
            String fromNumber = ApiConfig.getTwilioPhoneNumber();

            if (accountSid == null || accountSid.isEmpty() ||
                    authToken == null || authToken.isEmpty() ||
                    fromNumber == null || fromNumber.isEmpty()) {
                System.err.println("❌ Configuration Twilio incomplète");
                return simulateSms(to, message);
            }

            String urlString = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
            URL url = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String auth = accountSid + ":" + authToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            conn.setDoOutput(true);

            String formData = "To=" + encodeValue(to) +
                    "&From=" + encodeValue(fromNumber) +
                    "&Body=" + encodeValue(message);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = formData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            boolean success = responseCode >= 200 && responseCode < 300;

            if (success) {
                System.out.println("✅ SMS Twilio envoyé à " + to);
            } else {
                System.err.println("❌ Erreur Twilio: " + responseCode);
            }

            return success;

        } catch (Exception e) {
            System.err.println("❌ Erreur Twilio: " + e.getMessage());
            return simulateSms(to, message);
        }
    }

    /**
     * Envoie un SMS via Infobip
     */
    private boolean sendViaInfobip(String to, String message) {
        try {
            String apiKey = ApiConfig.getInfobipApiKey();
            String apiUrl = ApiConfig.getInfobipApiUrl();

            if (apiKey == null || apiKey.isEmpty() || apiUrl == null || apiUrl.isEmpty()) {
                System.err.println("❌ Configuration Infobip incomplète");
                return simulateSms(to, message);
            }

            String urlString = apiUrl + "/sms/2/text/advanced";
            URL url = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "App " + apiKey);
            conn.setDoOutput(true);

            String jsonPayload = String.format(
                    "{\"messages\":[{\"destinations\":[{\"to\":\"%s\"}],\"text\":\"%s\"}]}",
                    to, escapeJson(message)
            );

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            boolean success = responseCode >= 200 && responseCode < 300;

            if (success) {
                System.out.println("✅ SMS Infobip envoyé à " + to);
            } else {
                System.err.println("❌ Erreur Infobip: " + responseCode);
            }

            return success;

        } catch (Exception e) {
            System.err.println("❌ Erreur Infobip: " + e.getMessage());
            return simulateSms(to, message);
        }
    }

    /**
     * Envoie un SMS de promotion pour carte expirée
     */
    public boolean sendPromotionExpiree(String to, String produit, double ancienPrix,
                                        double nouveauPrix, double reduction) {
        String message = String.format(
                "⏰ DERNIÈRE CHANCE ⏰\n%s\n%.2f € au lieu de %.2f € (-%.0f%%)\nOffre limitée !",
                produit, nouveauPrix, ancienPrix, reduction
        );
        return sendSms(to, message);
    }

    /**
     * Envoie un SMS de promotion flash
     */
    public boolean sendPromotionFlash(String to, String produit, double ancienPrix,
                                      double nouveauPrix, double reduction) {
        String message = String.format(
                "🔥 PROMO FLASH 🔥\n%s\n%.2f € au lieu de %.2f € (-%.0f%%)",
                produit, nouveauPrix, ancienPrix, reduction
        );
        return sendSms(to, message);
    }

    /**
     * Envoie un SMS simple
     */
    public boolean sendSimpleMessage(String to, String message) {
        return sendSms(to, message);
    }

    private String encodeValue(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private boolean simulateSms(String to, String message) {
        System.out.println("\n📱 [SIMULATION SMS]");
        System.out.println("   À: " + to);
        System.out.println("   Message: " + message);
        return true;
    }

    /**
     * Envoie des SMS à plusieurs destinataires
     */
    public int sendBulkSms(String[] destinations, String message) {
        int count = 0;
        for (String to : destinations) {
            if (sendSms(to.trim(), message)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Envoie une promotion à tous les clients
     */
    public int sendPromotionToAll(String[] destinations, String produit,
                                  double ancienPrix, double nouveauPrix,
                                  double reduction) {
        String message = String.format(
                "🔥 PROMO FLASH 🔥\n%s\n%.2f € au lieu de %.2f € (-%.0f%%)",
                produit, nouveauPrix, ancienPrix, reduction
        );
        return sendBulkSms(destinations, message);
    }

    /**
     * Vérifie l'état du client SMS
     */
    public String getStatus() {
        if (ApiConfig.isSimulationMode()) {
            return "✅ Mode simulation (aucun SMS réel envoyé)";
        } else if (ApiConfig.isTwilioMode()) {
            return "✅ Connecté à Twilio";
        } else if (ApiConfig.isInfobipMode()) {
            return "✅ Connecté à Infobip";
        } else {
            return "⚠️ Mode non configuré";
        }
    }
}