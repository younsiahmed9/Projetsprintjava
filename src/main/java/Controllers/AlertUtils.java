package Controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import utils.UiStyles;

public class AlertUtils {

    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        UiStyles.applyDialogStyles(alert.getDialogPane());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        UiStyles.applyDialogStyles(alert.getDialogPane());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        UiStyles.applyDialogStyles(alert.getDialogPane());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        UiStyles.applyDialogStyles(alert.getDialogPane());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK;
    }

    public static void showErrorWithDetails(String title, String message, String details) {
        Alert alert = new Alert(AlertType.ERROR);
        UiStyles.applyDialogStyles(alert.getDialogPane());
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(details);
        alert.showAndWait();
    }

    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        UiStyles.applyDialogStyles(alert.getDialogPane());
        alert.setTitle(title);
        alert.setHeaderText("✓ Succès");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showDeleteSuccess(String entityType, String entityName) {
        Alert alert = new Alert(AlertType.INFORMATION);
        UiStyles.applyDialogStyles(alert.getDialogPane());
        alert.setTitle("✓ Suppression réussie");
        alert.setHeaderText("L'élément a été supprimé avec succès");
        alert.setContentText(entityType + " : \"" + entityName + "\" supprimé(e) définitivement.");
        alert.showAndWait();
    }

    public static void showCreateSuccess(String entityType, String entityName) {
        Alert alert = new Alert(AlertType.INFORMATION);
        UiStyles.applyDialogStyles(alert.getDialogPane());
        alert.setTitle("✓ Création réussie");
        alert.setHeaderText("L'élément a été créé avec succès");
        alert.setContentText(entityType + " : \"" + entityName + "\" créé(e).");
        alert.showAndWait();
    }

    public static void showUpdateSuccess(String entityType, String entityName) {
        Alert alert = new Alert(AlertType.INFORMATION);
        UiStyles.applyDialogStyles(alert.getDialogPane());
        alert.setTitle("✓ Modification réussie");
        alert.setHeaderText("L'élément a été modifié avec succès");
        alert.setContentText(entityType + " : \"" + entityName + "\" mis à jour.");
        alert.showAndWait();
    }
}
