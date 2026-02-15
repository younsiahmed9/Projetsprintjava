package Controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;

/**
 * Classe utilitaire pour gérer les alertes et messages
 */
public class AlertUtils {

    /**
     * Affiche une alerte d'erreur
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte d'information
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte d'avertissement
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte de confirmation
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK;
    }

    /**
     * Affiche une alerte avec détails d'erreur
     */
    public static void showErrorWithDetails(String title, String message, String details) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(details);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte de succès (en utilisant INFORMATION)
     */
    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("✓ Succès");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une notification de suppression réussie
     */
    public static void showDeleteSuccess(String entityType, String entityName) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("✓ Suppression réussie");
        alert.setHeaderText("L'élément a été supprimé avec succès");
        alert.setContentText(entityType + " : \"" + entityName + "\" supprimé(e) définitivement.");
        alert.showAndWait();
    }

    /**
     * Affiche une notification de création réussie
     */
    public static void showCreateSuccess(String entityType, String entityName) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("✓ Création réussie");
        alert.setHeaderText("L'élément a été créé avec succès");
        alert.setContentText(entityType + " : \"" + entityName + "\" créé(e).");
        alert.showAndWait();
    }

    /**
     * Affiche une notification de modification réussie
     */
    public static void showUpdateSuccess(String entityType, String entityName) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("✓ Modification réussie");
        alert.setHeaderText("L'élément a été modifié avec succès");
        alert.setContentText(entityType + " : \"" + entityName + "\" mis à jour.");
        alert.showAndWait();
    }
}
