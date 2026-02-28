import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.MyDataBase;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/interfaces/MainView.fxml")));
        primaryStage.setTitle("FinTrack - Gestion des Services");
        primaryStage.setScene(new Scene(root, 1000, 700));
        primaryStage.show();
    }

    public static void main(String[] args) {
        System.out.println("Démarrage de l'application...");

        // Tester la connexion à la base de données
        try {
            MyDataBase db = MyDataBase.getInstance();
            if (db.getConnection() != null) {
                System.out.println("Connexion à la base de données réussie!");
            }
        } catch (Exception e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
        }

        // Lancer l'application JavaFX
        launch(args);
    }
}