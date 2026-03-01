package api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SmsModeProvider implements SmsProvider {

    private String apiKey;

    public SmsModeProvider(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public boolean sendSms(String to, String message) {
        try {
            URL url = new URL("https://api.smsmode.com/http/1.6/sendSMS.do");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String params = "accessToken=" + URLEncoder.encode(apiKey, "UTF-8") +
                    "&numero=" + URLEncoder.encode(to, "UTF-8") +
                    "&message=" + URLEncoder.encode(message, "UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();

            // 🔴 LIRE LA RÉPONSE COMPLÈTE
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("📱 Code: " + responseCode);
            System.out.println("📱 Réponse: " + response.toString());

            return responseCode == 200;

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return false;
        }
    }
    @Override
    public String getProviderName() {
        return "SMSMode (réel)";
    }
}