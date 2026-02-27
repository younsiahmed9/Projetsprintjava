package Controllers;

import javafx.application.Application;
import javafx.stage.Stage;

public class FinTrackApp extends Application {
    @Override
    public void start(Stage stage) {
        stage.setMaximized(true);
        SceneNavigator.init(stage);
        SceneNavigator.goLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
