package api;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TextBeltProvider implements SmsProvider {

    @Override
    public boolean sendSms(String to, String message) {
        try {
            URL url = new URL("https://textbelt.com/text");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String params = "phone=" + URLEncoder.encode(to, "UTF-8") +
                    "&message=" + URLEncoder.encode(message, "UTF-8") +
                    "&key=textbelt";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            return conn.getResponseCode() == 200;

        } catch (Exception e) {
            System.err.println("❌ Erreur TextBelt: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "textbelt";
    }
}