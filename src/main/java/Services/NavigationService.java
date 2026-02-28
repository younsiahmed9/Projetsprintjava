package Services;

import Controllers.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service centralisé pour la navigation entre les dashboards
 * Améliore la fluidité et évite la duplication de code
 */
public class NavigationService {

    private static Stage mainStage;
    private static final Map<String, Parent> viewCache = new HashMap<>();
    private static final Map<String, Object> controllers = new HashMap<>();

    /**
     * Initialiser le service avec la stage principale
     */
    public static void init(Stage stage) {
        mainStage = stage;
    }

    public static boolean fileExists(String fxmlPath) {
        return NavigationService.class.getResource(fxmlPath) != null;
    }


    /**
     * Naviguer vers une vue avec animation
     */
    public static void navigateTo(String fxmlPath, String title, boolean useCache) {
        try {
            Parent root = null;

            // Utiliser le cache si demandé
            if (useCache && viewCache.containsKey(fxmlPath)) {
                root = viewCache.get(fxmlPath);
            } else {
                FXMLLoader loader = new FXMLLoader(NavigationService.class.getResource(fxmlPath));
                root = loader.load();
                controllers.put(fxmlPath, loader.getController());

                if (useCache) {
                    viewCache.put(fxmlPath, root);
                }
            }

            Scene scene = new Scene(root, 1280, 800);

            // Ajouter le CSS
            try {
                String css = NavigationService.class.getResource("/css/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                // Ignorer si le CSS n'existe pas
            }

            // Animation de transition
            root.setOpacity(0);

            mainStage.setScene(scene);
            mainStage.setTitle("FinTrack - " + title);
            mainStage.centerOnScreen();

            // Animation fade-in
            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation vers " + title);
        }
    }

    /**
     * Naviguer sans cache (recharge la vue)
     */
    public static void navigateTo(String fxmlPath, String title) {
        navigateTo(fxmlPath, title, false);
    }

    /**
     * Retour à la page précédente
     */
    public static void goBack() {
        // Implémentation si besoin de l'historique
    }

    /**
     * Obtenir le contrôleur d'une vue
     */
    public static Object getController(String fxmlPath) {
        return controllers.get(fxmlPath);
    }

    /**
     * Vider le cache
     */
    public static void clearCache() {
        viewCache.clear();
        controllers.clear();
    }

    private static void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}