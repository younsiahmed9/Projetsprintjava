package tn.esprit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger le FXML
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/AfficherCompte.fxml"));

        // Créer la scène
        Scene scene = new Scene(root);

        // Ajouter ton CSS
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        // Configurer la fenêtre
        primaryStage.setTitle("Gestion des Comptes");
        primaryStage.setScene(scene);
        primaryStage.setWidth(900);
        primaryStage.setHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
