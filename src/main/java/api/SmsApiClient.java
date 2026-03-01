package api;

import config.APIConfig.ApiConfig;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SmsApiClient {

    private String provider;

    public SmsApiClient() {
        this.provider = ApiConfig.getSmsProvider();
        System.out.println("📱 Client SMS: " + provider);
    }

    public boolean sendSms(String to, String message) {
        if (ApiConfig.isSimulationMode()) {
            return simulateSms(to, message);
        } else if (ApiConfig.isTwilioMode()) {
            return sendViaTwilio(to, message);
        } else {
            return simulateSms(to, message);
        }
    }

    private boolean sendViaTwilio(String to, String message) {
        try {
            String accountSid = ApiConfig.getTwilioAccountSid();
            String authToken = ApiConfig.getTwilioAuthToken();
            String fromNumber = ApiConfig.getTwilioPhoneNumber();

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
                os.write(formData.getBytes(StandardCharsets.UTF_8));
            }

            return conn.getResponseCode() >= 200 && conn.getResponseCode() < 300;

        } catch (Exception e) {
            System.err.println("❌ Erreur Twilio: " + e.getMessage());
            return simulateSms(to, message);
        }
    }

    public boolean sendPromotionExpiree(String to, String produit, double ancienPrix,
                                        double nouveauPrix, double reduction) {
        String message = String.format(
                "⏰ DERNIÈRE CHANCE ⏰\n%s\n%.2f € au lieu de %.2f € (-%.0f%%)",
                produit, nouveauPrix, ancienPrix, reduction
        );
        return sendSms(to, message);
    }

    private String encodeValue(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    private boolean simulateSms(String to, String message) {
        System.out.println("\n📱 [SIMULATION] À: " + to);
        System.out.println("   Message: " + message);
        return true;
    }
}