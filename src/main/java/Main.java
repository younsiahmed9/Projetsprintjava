import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.MyDataBase;
import Services.ScheduledTransferExecutor;
import javafx.application.Application;
import javafx.stage.Stage;
/**
 * Point d'entrée principal de l'application FinTrack.
 * Lance l'interface de connexion (login.fxml).
 */
public class Main extends Application {
    private ScheduledTransferExecutor executor;


    @Override
    public void init() {
        executor = new ScheduledTransferExecutor();
        executor.start();  // starts the background thread
        System.out.println("🚀 Scheduled transfer executor started.");
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Tester connexion DB
            if (MyDataBase.getInstance().getConnection() == null) {
                System.err.println("❌ Connexion DB échouée!");
                return;
            }
            System.out.println("✅ Connexion DB réussie!");

            // Charger login.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 600, 450);
            // CSS global (tokens + components)
            scene.getStylesheets().add(getClass().getResource("/styles/colors.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

            primaryStage.setTitle("FinTrack - Connexion");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("❌ Erreur au démarrage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
