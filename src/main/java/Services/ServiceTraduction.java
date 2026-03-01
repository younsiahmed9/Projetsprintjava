package Services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de traduction multilingue
 * Supporte plusieurs API de traduction et traduction hors ligne
 */
public class ServiceTraduction {

    private static final String LIBRE_TRANSLATE_API = "https://libretranslate.com/translate";
    private final Gson gson;

    public enum Language {
        FRENCH("fr", "Français"),
        ENGLISH("en", "English"),
        ARABIC("ar", "العربية"),
        SPANISH("es", "Español"),
        GERMAN("de", "Deutsch"),
        ITALIAN("it", "Italiano"),
        PORTUGUESE("pt", "Português"),
        RUSSIAN("ru", "Русский"),
        CHINESE("zh", "中文"),
        JAPANESE("ja", "日本語");

        private final String code;
        private final String displayName;

        Language(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Language fromCode(String code) {
            for (Language lang : values()) {
                if (lang.code.equals(code)) {
                    return lang;
                }
            }
            return ENGLISH;
        }
    }

    public ServiceTraduction() {
        this.gson = new Gson();
    }

    /**
     * Traduit un texte en utilisant LibreTranslate (API gratuite et open source)
     */
    public String translate(String text, Language from, Language to) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        if (from == to) {
            return text;
        }

        // Utiliser l'API LibreTranslate
        return translateWithLibreTranslate(text, from.getCode(), to.getCode());
    }

    /**
     * Traduit un texte avec LibreTranslate
     */
    private String translateWithLibreTranslate(String text, String sourceLang, String targetLang) throws Exception {
        URL url = new URL(LIBRE_TRANSLATE_API);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Créer le JSON de requête
        JsonObject json = new JsonObject();
        json.addProperty("q", text);
        json.addProperty("source", sourceLang);
        json.addProperty("target", targetLang);
        json.addProperty("format", "text");

        // Envoyer la requête
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Lire la réponse
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                JsonObject responseJson = gson.fromJson(response.toString(), JsonObject.class);
                return responseJson.get("translatedText").getAsString();
            }
        } else {
            throw new Exception("Erreur de traduction: HTTP " + responseCode);
        }
    }

    /**
     * Traduit un texte avec détection automatique de la langue source
     */
    public String translateAuto(String text, Language to) throws Exception {
        Language detected = detectLanguage(text);
        return translate(text, detected, to);
    }

    /**
     * Traduit un texte en plusieurs langues simultanément
     */
    public Map<Language, String> translateToMultiple(String text, Language from, List<Language> targetLanguages) {
        Map<Language, String> translations = new HashMap<>();

        for (Language targetLang : targetLanguages) {
            try {
                String translation = translate(text, from, targetLang);
                translations.put(targetLang, translation);
            } catch (Exception e) {
                translations.put(targetLang, "[Erreur de traduction: " + e.getMessage() + "]");
            }
        }

        return translations;
    }

    /**
     * Détecte la langue d'un texte (approximation simple)
     * Note: Pour une détection plus précise, utiliser une bibliothèque dédiée comme Apache Tika
     */
    public Language detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Language.FRENCH;
        }

        text = text.toLowerCase();

        // Mots indicateurs par langue
        String[] frenchWords = {"le", "la", "les", "un", "une", "des", "et", "ou", "est", "sont", "de", "du", "dans"};
        String[] englishWords = {"the", "a", "an", "and", "or", "is", "are", "of", "in", "to", "for"};
        String[] spanishWords = {"el", "la", "los", "las", "un", "una", "y", "o", "es", "son", "de", "en"};
        String[] germanWords = {"der", "die", "das", "ein", "eine", "und", "oder", "ist", "sind", "von", "in"};

        int frenchCount = countWords(text, frenchWords);
        int englishCount = countWords(text, englishWords);
        int spanishCount = countWords(text, spanishWords);
        int germanCount = countWords(text, germanWords);

        // Détection des caractères arabes
        if (text.matches(".*[\u0600-\u06FF].*")) {
            return Language.ARABIC;
        }

        // Détection des caractères chinois/japonais
        if (text.matches(".*[\u4E00-\u9FFF].*")) {
            return Language.CHINESE;
        }
        if (text.matches(".*[\u3040-\u309F\u30A0-\u30FF].*")) {
            return Language.JAPANESE;
        }

        // Trouver la langue avec le plus de correspondances
        int max = Math.max(Math.max(frenchCount, englishCount), Math.max(spanishCount, germanCount));

        if (max == frenchCount) return Language.FRENCH;
        if (max == englishCount) return Language.ENGLISH;
        if (max == spanishCount) return Language.SPANISH;
        if (max == germanCount) return Language.GERMAN;

        return Language.FRENCH; // Par défaut
    }

    /**
     * Compte le nombre de mots d'une liste présents dans un texte
     */
    private int countWords(String text, String[] words) {
        int count = 0;
        for (String word : words) {
            if (text.contains(" " + word + " ") || text.startsWith(word + " ") || text.endsWith(" " + word)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Traduction hors ligne simple (dictionnaire de base)
     * Utile pour les termes techniques récurrents
     */
    public String translateOffline(String text, Language from, Language to) {
        // Dictionnaire simple français-anglais
        Map<String, String> dictFrToEn = new HashMap<>();
        dictFrToEn.put("document", "document");
        dictFrToEn.put("dossier", "folder");
        dictFrToEn.put("catégorie", "category");
        dictFrToEn.put("échéance", "deadline");
        dictFrToEn.put("facture", "invoice");
        dictFrToEn.put("contrat", "contract");
        dictFrToEn.put("rapport", "report");

        if (from == Language.FRENCH && to == Language.ENGLISH) {
            String result = text;
            for (Map.Entry<String, String> entry : dictFrToEn.entrySet()) {
                result = result.replaceAll("(?i)\\b" + entry.getKey() + "\\b", entry.getValue());
            }
            return result;
        }

        return text; // Pas de traduction hors ligne disponible
    }

    /**
     * Obtient toutes les langues supportées
     */
    public List<Language> getSupportedLanguages() {
        return List.of(Language.values());
    }

    /**
     * Classe représentant un résultat de traduction
     */
    public static class TranslationResult {
        private final String originalText;
        private final String translatedText;
        private final Language sourceLanguage;
        private final Language targetLanguage;
        private final boolean success;
        private final String errorMessage;

        public TranslationResult(String originalText, String translatedText,
                                Language sourceLanguage, Language targetLanguage,
                                boolean success, String errorMessage) {
            this.originalText = originalText;
            this.translatedText = translatedText;
            this.sourceLanguage = sourceLanguage;
            this.targetLanguage = targetLanguage;
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public String getOriginalText() { return originalText; }
        public String getTranslatedText() { return translatedText; }
        public Language getSourceLanguage() { return sourceLanguage; }
        public Language getTargetLanguage() { return targetLanguage; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }

        @Override
        public String toString() {
            if (success) {
                return String.format("%s → %s: %s → %s",
                        sourceLanguage.getDisplayName(),
                        targetLanguage.getDisplayName(),
                        originalText,
                        translatedText);
            } else {
                return "Erreur: " + errorMessage;
            }
        }
    }
}

