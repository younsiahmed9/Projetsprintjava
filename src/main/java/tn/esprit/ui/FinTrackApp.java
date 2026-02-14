package tn.esprit.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class FinTrackApp extends Application {
    @Override
    public void start(Stage stage) {
        SceneNavigator.init(stage);
        SceneNavigator.goLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
