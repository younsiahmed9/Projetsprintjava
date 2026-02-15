package utils;

/**
 * Classe utilitaire pour valider les saisies utilisateur
 */
public class ValidationUtils {

    /**
     * Valide que le champ nom n'est pas vide
     */
    public static boolean isValidNom(String nom) {
        return nom != null && !nom.trim().isEmpty() && nom.trim().length() >= 3;
    }

    /**
     * Valide que le champ description n'est pas vide (optionnel mais recommandé)
     */
    public static boolean isValidDescription(String description) {
        return description == null || description.trim().length() >= 0;
    }

    /**
     * Valide que le champ titre n'est pas vide
     */
    public static boolean isValidTitre(String titre) {
        return titre != null && !titre.trim().isEmpty() && titre.trim().length() >= 3;
    }

    /**
     * Valide qu'un dossier a été sélectionné
     */
    public static boolean isValidDossier(Object dossier) {
        return dossier != null;
    }

    /**
     * Valide que le chemin du fichier n'est pas vide
     */
    public static boolean isValidFilePath(String filePath) {
        return filePath != null && !filePath.trim().isEmpty();
    }

    /**
     * Valide que le fichier existe
     */
    public static boolean isFileExists(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        java.io.File file = new java.io.File(filePath.trim());
        return file.exists() && file.isFile();
    }

    /**
     * Retourne un message d'erreur pour nom invalide
     */
    public static String getErrorNom() {
        return "Le nom doit contenir au moins 3 caractères.";
    }

    /**
     * Retourne un message d'erreur pour titre invalide
     */
    public static String getErrorTitre() {
        return "Le titre doit contenir au moins 3 caractères.";
    }

    /**
     * Retourne un message d'erreur pour dossier non sélectionné
     */
    public static String getErrorDossier() {
        return "Veuillez sélectionner un dossier.";
    }

    /**
     * Retourne un message d'erreur pour chemin fichier invalide
     */
    public static String getErrorFilePath() {
        return "Veuillez saisir un chemin de fichier valide.";
    }

    /**
     * Retourne un message d'erreur pour fichier inexistant
     */
    public static String getErrorFileNotExists() {
        return "Le fichier n'existe pas. Vérifiez le chemin.";
    }

    /**
     * Valide un email (optionnel)
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Optionnel
        }
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Valide un numéro de téléphone (optionnel)
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Optionnel
        }
        return phone.matches("^[0-9+\\-\\s()]+$") && phone.length() >= 10;
    }

    /**
     * Nettoie et normalise une chaîne
     */
    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim();
    }
}

