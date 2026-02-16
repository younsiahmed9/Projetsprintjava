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
        if (description == null || description.trim().isEmpty()) {
            return true;
        }
        return description.trim().length() >= 3;
    }

    /**
     * Retourne un message d'erreur pour description invalide
     */
    public static String getErrorDescription() {
        return "La description doit contenir au moins 3 caractères.";
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
     * Valide qu'une catégorie a été sélectionnée
     */
    public static boolean isValidCategorie(Object categorie) {
        return categorie != null;
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
     * Retourne un message d'erreur pour catégorie non sélectionnée
     */
    public static String getErrorCategorie() {
        return "Veuillez sélectionner une catégorie.";
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

    /**
     * Valide un budget (optionnel, >= 0 si renseigné)
     */
    public static boolean isValidBudget(String budget) {
        if (budget == null || budget.trim().isEmpty()) {
            return true;
        }
        try {
            return Double.parseDouble(budget.trim()) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Retourne un message d'erreur pour budget invalide
     */
    public static String getErrorBudget() {
        return "Le budget doit être un nombre positif.";
    }

    /**
     * Valide un montant (obligatoire, >= 0)
     */
    public static boolean isValidMontant(String montant) {
        if (montant == null || montant.trim().isEmpty()) {
            return false;
        }
        try {
            return Double.parseDouble(montant.trim()) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Retourne un message d'erreur pour montant invalide
     */
    public static String getErrorMontant() {
        return "Le montant doit être un nombre positif ou zéro (ex: 250.00).";
    }
}
