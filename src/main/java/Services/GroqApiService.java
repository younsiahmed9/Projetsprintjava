package Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class GroqApiService {
    private static final String API_KEY = "YOUR_GROQ_API_KEY"; // Remplacez par votre clé API Groq
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-8b-instant";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GroqApiService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<String> getFintechSuggestion(String userMessage) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", MODEL);

            ArrayNode messages = root.putArray("messages");

            // System message for context
            ObjectNode systemMsg = messages.addObject();
            systemMsg.put("role", "system");
            systemMsg.put("content",
                    "Tu es un assistant expert en Fintech. Ton rôle est d'aider l'utilisateur à reformuler ses messages pour qu'ils soient professionnels, clairs et précis dans le contexte bancaire/financier. Réponds uniquement avec le message reformulé.");

            // User message
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", "Reformule ce message de manière professionnelle en Fintech: " + userMessage);

            root.put("temperature", 0.7);
            root.put("max_tokens", 1024);

            String requestBody = objectMapper.writeValueAsString(root);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            return "Erreur API: " + response.statusCode() + " - " + response.body();
                        }
                        try {
                            JsonNode jsonResponse = objectMapper.readTree(response.body());
                            return jsonResponse.path("choices").get(0).path("message").path("content").asText();
                        } catch (Exception e) {
                            return "Erreur lors du traitement de la réponse IA.";
                        }
                    });
        } catch (Exception e) {
            CompletableFuture<String> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
}
