package Services;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ProfanityFilterService {

    // Liste de base de mots impolis (à compléter)
    private static final List<String> FORBIDDEN_WORDS = Arrays.asList(
            "merde", "putain", "con", "connard", "salope", "enculé", "fdp", "abruti",
            "shit", "fuck", "bitch", "asshole", "bastard", "idiot");

    /**
     * Vérifie si le texte contient des mots impolis.
     * 
     * @param text Le contenu à analyser.
     * @return true si au moins un mot interdit est trouvé.
     */
    public boolean containsProfanity(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String lowerText = text.toLowerCase();
        for (String word : FORBIDDEN_WORDS) {
            // On utilise des expressions régulières pour éviter de bloquer des mots
            // qui contiennent la séquence de lettres (ex: "conception" ne doit pas être
            // bloqué)
            String regex = "\\b" + Pattern.quote(word) + "\\b";
            if (Pattern.compile(regex).matcher(lowerText).find()) {
                return true;
            }
        }
        return false;
    }
}
