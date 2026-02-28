import Services.Config;
import Services.RealTimeCurrencyService;
import utils.MyDataBase;
import Services.NavigationService;
import Services.ScheduledTransferExecutor;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    private ScheduledTransferExecutor executor;

    @Override
    public void init() {
        executor = new ScheduledTransferExecutor();
        executor.start();
        System.out.println("🚀 Scheduled transfer executor started.");

        // Test de configuration email
        System.out.println("📧 Test de configuration email:");
        System.out.println("   Clé API: " + (Config.getApiKey() != null ? "✅ Configurée" : "❌ Non configurée"));
        System.out.println("   From: " + Config.getFromEmail());
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialiser le service de navigation
            NavigationService.init(primaryStage);

            // Tester connexion DB
            if (MyDataBase.getInstance().getConnection() == null) {
                System.err.println("❌ Connexion DB échouée!");
                return;
            }
            System.out.println("✅ Connexion DB réussie!");

            // ✅ TEST DES TAUX DE CHANGE EN TEMPS RÉEL
            System.out.println("\n💱 TEST DES TAUX DE CHANGE:");
            try {
                double usdToDt = RealTimeCurrencyService.getExchangeRate("USD", "TND");
                double eurToDt = RealTimeCurrencyService.getExchangeRate("EUR", "TND");
                System.out.println("   ✅ Connexion API réussie!");
                System.out.println("   💵 1 USD = " + String.format("%.3f", usdToDt) + " TND");
                System.out.println("   💶 1 EUR = " + String.format("%.3f", eurToDt) + " TND");
                System.out.println("   ⏱️  Mis à jour: " + java.time.LocalDateTime.now());
            } catch (Exception e) {
                System.out.println("   ❌ Erreur API taux de change: " + e.getMessage());
                System.out.println("   ⚠️  Utilisation des taux de secours (3.1 USD, 3.4 EUR)");
            }
            System.out.println("----------------------------------------\n");

            // Charger login.fxml via le service de navigation
            NavigationService.navigateTo("/fxml/login.fxml", "Connexion");

            // Afficher la stage (important !)
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("❌ Erreur au démarrage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (executor != null) {
            executor.stop();
            System.out.println("🛑 Scheduled transfer executor stopped.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}