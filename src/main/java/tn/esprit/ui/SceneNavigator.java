package tn.esprit.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SceneNavigator {
    private static Stage stage;

    private SceneNavigator() {
    }

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void goTo(String fxmlPath, String title) {
        if (stage == null) {
            throw new IllegalStateException("SceneNavigator non initialisé");
        }
        try {
            var url = SceneNavigator.class.getResource(fxmlPath);
            if (url == null) {
                throw new IllegalArgumentException("FXML introuvable sur le classpath: " + fxmlPath);
            }
            System.out.println("[SceneNavigator] Chargement FXML: " + fxmlPath + " -> " + url);

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            // Affiche la vraie cause dans la console pour debug
            e.printStackTrace();
            throw new RuntimeException(buildLoadErrorMessage(fxmlPath, e), e);
        }
    }

    private static String buildLoadErrorMessage(String fxmlPath, Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        String detail = root.getMessage();
        return detail == null || detail.isBlank()
                ? ("Impossible de charger FXML: " + fxmlPath)
                : ("Impossible de charger FXML: " + fxmlPath + " (" + detail + ")");
    }

    public static void goLogin() {
        goTo("/fxml/login.fxml", "FinTrack - Login");
    }

    public static void goRegister() {
        goTo("/fxml/register.fxml", "FinTrack - Création de compte");
    }

    public static void goHome() {
        goTo("/fxml/home.fxml", "FinTrack - Accueil");
    }

    public static void goHomeAdmin() {
        goTo("/fxml/home.fxml", "FinTrack - Accueil (Admin)");
    }

    public static void goAdminDashboard() {
        goTo("/fxml/admin_dashboard.fxml", "FinTrack - Dashboard Admin");
    }
}
