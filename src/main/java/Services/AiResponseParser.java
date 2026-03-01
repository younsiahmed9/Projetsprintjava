package Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AiResponseParser {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String extractAssistantReply(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return "Aucune reponse de l'assistant.";
        }
        try {
            JsonNode root = MAPPER.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode content = choices.get(0).path("message").path("content");
                if (!content.isMissingNode()) {
                    return content.asText().trim();
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur de parsing de la reponse IA:");
            e.printStackTrace();
        }
        return "Je n'ai pas pu lire la reponse de l'assistant.";
    }

    public static String extractErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return "Aucun detail d'erreur.";
        }
        try {
            JsonNode root = MAPPER.readTree(responseBody);
            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                String message = error.path("message").asText("");
                if (!message.isEmpty()) {
                    return message;
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur de parsing de l'erreur IA:");
            e.printStackTrace();
        }
        return "Impossible de lire le detail d'erreur.";
    }
}
