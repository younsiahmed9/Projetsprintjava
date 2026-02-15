package tn.esprit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger ton FXML principal
        Parent root = FXMLLoader.load(getClass().getResource("/MainPage.fxml"));


        // Créer la scène
        Scene scene = new Scene(root);

        // Ajouter ton CSS externe
        scene.getStylesheets().add(getClass().getResource("/assets/style.css").toExternalForm());

        // Configurer la fenêtre
        primaryStage.setTitle("Gestion Budget & Comptes");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
