package api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
// import services.GestionPromo;  // ❌ À SUPPRIMER
// import config.PromoConfig;      // ❌ À SUPPRIMER
import services.PromotionService;  // ✅ À AJOUTER
import config.APIConfig.ApiConfig; // ✅ À AJOUTER
import api.SmsClient;              // ✅ À AJOUTER (celui-ci existe)

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class APIPromos {

    private PromotionService service;  // ✅ Utiliser PromotionService
    private SmsClient sms;              // ✅ Utiliser SmsClient (existe)
    private Gson gson = new Gson();
    private HttpServer serveur;

    public APIPromos() {
        this.service = new PromotionService();  // ✅ OK
        this.sms = new SmsClient();              // ✅ OK (SmsClient existe)
    }

    public void demarrer(int port) {
        try {
            serveur = HttpServer.create(new InetSocketAddress(port), 0);

            serveur.createContext("/api/cartes/expirees", new HandlerCartes());
            serveur.createContext("/api/offres/creer", new HandlerCreer());
            serveur.createContext("/api/notifier", new HandlerNotifier());
            serveur.createContext("/api/statut", new HandlerStatut());

            serveur.setExecutor(null);
            serveur.start();

            System.out.println("\n" + "=".repeat(50));
            System.out.println("🚀 API PROMOTIONS DÉMARRÉE");
            System.out.println("=".repeat(50));
            System.out.println("📡 http://localhost:" + port);
            System.out.println("\n📌 Commandes disponibles:");
            System.out.println("   GET  /api/cartes/expirees");
            System.out.println("   POST /api/offres/creer");
            System.out.println("   POST /api/notifier");
            System.out.println("   GET  /api/statut");

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    class HandlerCartes implements HttpHandler {
        @Override
        public void handle(HttpExchange e) {
            try {
                var cartes = service.detecterCartesExpirees();  // ✅ Méthode qui existe
                envoyerReponse(e, 200, gson.toJson(cartes));
            } catch (Exception ex) {
                envoyerErreur(e, ex);
            }
        }
    }

    class HandlerCreer implements HttpHandler {
        @Override
        public void handle(HttpExchange e) {
            try {
                var offres = service.promouvoirCartesExpirees();  // ✅ Méthode qui existe
                JsonObject rep = new JsonObject();
                rep.addProperty("status", "ok");
                rep.addProperty("nombre", offres.size());
                envoyerReponse(e, 200, rep.toString());
            } catch (Exception ex) {
                envoyerErreur(e, ex);
            }
        }
    }

    class HandlerNotifier implements HttpHandler {
        @Override
        public void handle(HttpExchange e) {
            try {
                var offres = service.getPromotionsActives();  // ✅ Méthode qui existe
                String[] numeros = ApiConfig.getTestNumbers();  // ✅ Utiliser ApiConfig
                int total = 0;

                for (var offre : offres) {
                    for (String numero : numeros) {
                        boolean sent = sms.sendPromotionExpiree(  // ✅ Méthode qui existe dans SmsClient
                                numero.trim(),
                                offre.getNomProduit(),
                                offre.getAncienPrix().doubleValue(),
                                offre.getNouveauPrix().doubleValue(),
                                offre.getPourcentageReduction()
                        );
                        if (sent) total++;
                    }
                }

                JsonObject rep = new JsonObject();
                rep.addProperty("status", "ok");
                rep.addProperty("notifications", total);
                envoyerReponse(e, 200, rep.toString());

            } catch (Exception ex) {
                envoyerErreur(e, ex);
            }
        }
    }

    class HandlerStatut implements HttpHandler {
        @Override
        public void handle(HttpExchange e) {
            JsonObject rep = new JsonObject();
            rep.addProperty("status", "en_ligne");
            rep.addProperty("mode", ApiConfig.isSimulationMode() ? "simulation" : "reel");
            rep.addProperty("reduction", ApiConfig.getPromotionReductionExpiree());
            rep.addProperty("duree", ApiConfig.getPromotionDureeJours());
            try {
                envoyerReponse(e, 200, rep.toString());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void envoyerReponse(HttpExchange e, int code, String reponse) throws Exception {
        e.getResponseHeaders().set("Content-Type", "application/json");
        e.sendResponseHeaders(code, reponse.getBytes().length);
        try (OutputStream os = e.getResponseBody()) {
            os.write(reponse.getBytes());
        }
    }

    private void envoyerErreur(HttpExchange e, Exception ex) {
        try {
            JsonObject err = new JsonObject();
            err.addProperty("error", ex.getMessage());
            envoyerReponse(e, 500, err.toString());
        } catch (Exception ex2) {}
    }

    public void arreter() {
        if (serveur != null) serveur.stop(0);
    }
}