package Services;

import Models.AiContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;

public class AiAssistantService {
    private static final String CONFIG_FILE = "ai.properties";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public String getReply(String userMessage, AiContext context) {
        AiConfig config = loadConfig();
        if (config.endpoint == null || config.endpoint.isEmpty() || config.apiKey == null || config.apiKey.isEmpty()) {
            return "Configuration IA manquante. Verifiez ai.properties ou la variable d’environnement AI_API_KEY.";
        }

        try {
            String payload = buildPayload(userMessage, context, config.model);
            HttpRequest request = HttpRequest.newBuilder(URI.create(config.endpoint))
                    .timeout(Duration.ofSeconds(config.timeoutSeconds))
                    .header("Authorization", "Bearer " + config.apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return AiResponseParser.extractAssistantReply(response.body());
            }
            String apiError = AiResponseParser.extractErrorMessage(response.body());
            System.err.println("Erreur API IA: " + response.statusCode() + " / " + response.body());
            return "Erreur API IA (code " + response.statusCode() + "): " + apiError;
        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel IA:");
            e.printStackTrace();
            return "Impossible de contacter l'IA pour le moment.";
        }
    }

    private String buildPayload(String userMessage, AiContext context, String model) throws Exception {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", model);
        ArrayNode messages = root.putArray("messages");
        messages.addObject()
                .put("role", "system")
                .put("content", AiPromptBuilder.buildSystemPrompt());
        messages.addObject()
                .put("role", "user")
                .put("content", AiPromptBuilder.buildUserPrompt(userMessage, context));
        root.put("temperature", 0.2);
        return MAPPER.writeValueAsString(root);
    }

    private AiConfig loadConfig() {
        AiConfig config = new AiConfig();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                config.endpoint = props.getProperty("ai.endpoint", "").trim();
                config.apiKey = props.getProperty("ai.apiKey", "").trim();
                config.model = props.getProperty("ai.model", "openai/gpt-oss-120b").trim();
                String timeout = props.getProperty("ai.timeoutSeconds", "20").trim();
                config.timeoutSeconds = Integer.parseInt(timeout);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de ai.properties:");
            e.printStackTrace();
        }

        String envKey = System.getenv("AI_API_KEY");
        if (config.apiKey == null || config.apiKey.isEmpty()) {
            config.apiKey = envKey == null ? "" : envKey.trim();
        }
        return config;
    }

    private static class AiConfig {
        private String endpoint = "";
        private String apiKey = "";
        private String model = "openai/gpt-oss-120b";
        private int timeoutSeconds = 20;
    }
}
