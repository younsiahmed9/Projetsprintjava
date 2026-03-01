import api.APIPromos;
import config.PromoConfig;

public class MainPromo {
    public static void main(String[] args) {
        System.out.println("\n" + "🌟".repeat(40));
        System.out.println("🌟 SYSTÈME DE PROMOTION AUTOMATIQUE");
        System.out.println("🌟".repeat(40));

        // Afficher la configuration
        PromoConfig.afficher();

        // Démarrer l'API
        APIPromos api = new APIPromos();
        api.demarrer(8080);

        // Hook pour l'arrêt
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n⏹️ Arrêt du système...");
            api.arreter();
        }));
    }
}