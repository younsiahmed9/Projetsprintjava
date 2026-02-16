package utils;

public class ValidationUtils {

    public static boolean isValidNom(String nom) {
        return nom != null && !nom.trim().isEmpty() && nom.trim().length() >= 3;
    }

    public static boolean isValidDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return true;
        }
        return description.trim().length() >= 3;
    }

    public static String getErrorDescription() {
        return "La description doit contenir au moins 3 caractères.";
    }

    public static boolean isValidTitre(String titre) {
        return titre != null && !titre.trim().isEmpty() && titre.trim().length() >= 3;
    }

    public static boolean isValidDossier(Object dossier) {
        return dossier != null;
    }

    public static boolean isValidCategorie(Object categorie) {
        return categorie != null;
    }

    public static boolean isValidFilePath(String filePath) {
        return filePath != null && !filePath.trim().isEmpty();
    }

    public static boolean isFileExists(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        java.io.File file = new java.io.File(filePath.trim());
        return file.exists() && file.isFile();
    }

    public static String getErrorNom() {
        return "Le nom doit contenir au moins 3 caractères.";
    }

    public static String getErrorTitre() {
        return "Le titre doit contenir au moins 3 caractères.";
    }

    public static String getErrorDossier() {
        return "Veuillez sélectionner un dossier.";
    }

    public static String getErrorCategorie() {
        return "Veuillez sélectionner une catégorie.";
    }

    public static String getErrorFilePath() {
        return "Veuillez saisir un chemin de fichier valide.";
    }

    public static String getErrorFileNotExists() {
        return "Le fichier n'existe pas. Vérifiez le chemin.";
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true;
        }
        return phone.matches("^[0-9+\\-\\s()]+$") && phone.length() >= 10;
    }

    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim();
    }

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

    public static String getErrorBudget() {
        return "Le budget doit être un nombre positif.";
    }

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

    public static String getErrorMontant() {
        return "Le montant doit être un nombre positif ou zéro (ex: 250.00).";
    }
}

