package utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class GeminiService {
    // API key provided by the user
    private static final String API_KEY = "AIzaSyD6Y2732SRMuT0OcnygbmAZJ-qgQ2hE50Q";

    private static String getApiUrl() {
        return "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key="
                + API_KEY;
    }

    public static String analyzeDocument(String text) {
        if (API_KEY == null || API_KEY.isEmpty() || API_KEY.startsWith("YOUR_")) {
            return "AI Analysis failed: Gemini API key is not valid or not set.";
        }
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(getApiUrl());
            post.setHeader("Content-Type", "application/json");

            JsonObject root = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();

            String prompt = "Analyze the following document text and provide a professional summary, " +
                    "identify the category (e.g., Invoice, Contract, Receipt), and extract the total amount if present. "
                    +
                    "Return it in a structured way. Text: " + text;

            part.addProperty("text", prompt);
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            root.add("contents", contents);

            StringEntity entity = new StringEntity(new Gson().toJson(root));
            post.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonObject responseJson = new Gson().fromJson(responseBody, JsonObject.class);

                // Simplified extraction from Gemini response nested structure
                try {
                    return responseJson.getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();
                } catch (Exception e) {
                    return "AI Analysis failed: " + responseBody;
                }
            }
        } catch (IOException e) {
            return "Internal error during AI analysis: " + e.getMessage();
        }
    }
}
