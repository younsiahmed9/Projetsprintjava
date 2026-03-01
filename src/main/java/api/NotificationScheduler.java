package api;

import config.APIConfig;
import services.PromotionService;
import config.APIConfig.ApiConfig; import java.time.LocalTime;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    private PromotionService promotionService;
    private SmsApiClient smsClient;
    private ScheduledExecutorService scheduler;

    public NotificationScheduler() {
        this.promotionService = new PromotionService();
        this.smsClient = new SmsApiClient();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void demarrerVerificationQuotidienne(int heure, int minute) {
        long delai = calculerDelai(heure, minute);
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("\n⏰ Vérification quotidienne");
            verifierEtNotifier();
        }, delai, 24, TimeUnit.HOURS);
        System.out.println("✅ Planifié à " + heure + ":" + minute);
    }

    private void verifierEtNotifier() {
        try {
            var promos = promotionService.promouvoirCartesExpirees();
            APIConfig.ApiConfig ApiConfig = null;
            String[] numeros = ApiConfig.getTestNumbers();

            for (var promo : promos) {
                for (String num : numeros) {
                    smsClient.sendPromotionExpiree(
                            num.trim(), promo.getNomProduit(),
                            promo.getAncienPrix().doubleValue(),
                            promo.getNouveauPrix().doubleValue(),
                            promo.getPourcentageReduction()
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    private long calculerDelai(int heure, int minute) {
        LocalTime maintenant = LocalTime.now();
        LocalTime execution = LocalTime.of(heure, minute);
        long delai = Duration.between(maintenant, execution).toMillis();
        return delai < 0 ? delai + TimeUnit.DAYS.toMillis(1) : delai;
    }

    public void arreter() {
        scheduler.shutdown();
    }
}