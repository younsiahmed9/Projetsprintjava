package api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import services.PromotionService;
import models.Produit;
import config.APIConfig.ApiConfig;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class PromotionApi {

    private PromotionService promotionService;
    private SmsApiClient smsClient;
    private Gson gson = new Gson();
    private HttpServer server;

    public PromotionApi() {
        this.promotionService = new PromotionService();
        this.smsClient = new SmsApiClient();
    }

    public void startServer(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/api/cartes/expirees", new CartesExpireesHandler());
            server.createContext("/api/promotions/lancer", new LancerPromotionsHandler());
            server.createContext("/api/notifier/sms", new NotifierSmsHandler());
            server.createContext("/api/status", new StatusHandler());
            server.setExecutor(null);
            server.start();

            System.out.println("\n🚀 API démarrée sur http://localhost:" + port);
            ApiConfig.afficherConfiguration();

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    class CartesExpireesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                List<Produit> cartes = promotionService.detecterCartesExpirees();
                JsonArray jsonArray = new JsonArray();
                for (Produit p : cartes) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", p.getIdProduit());
                    obj.addProperty("nom", p.getNomProduit());
                    obj.addProperty("prix", p.getMontant().doubleValue());
                    jsonArray.add(obj);
                }
                sendResponse(exchange, 200, gson.toJson(jsonArray));
            } catch (Exception e) {
                sendError(exchange, e);
            }
        }
    }

    class LancerPromotionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                var promos = promotionService.promouvoirCartesExpirees();
                JsonObject response = new JsonObject();
                response.addProperty("status", "success");
                response.addProperty("nombre", promos.size());
                sendResponse(exchange, 200, response.toString());
            } catch (Exception e) {
                sendError(exchange, e);
            }
        }
    }

    class NotifierSmsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = gson.fromJson(body, JsonObject.class);
                String to = json.get("to").getAsString();
                String message = json.get("message").getAsString();

                boolean sent = smsClient.sendSms(to, message);
                JsonObject response = new JsonObject();
                response.addProperty("status", sent ? "ok" : "error");
                sendResponse(exchange, 200, response.toString());
            } catch (Exception e) {
                sendError(exchange, e);
            }
        }
    }

    class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            JsonObject response = new JsonObject();
            response.addProperty("status", "online");
            response.addProperty("provider", ApiConfig.getSmsProvider());
            try {
                sendResponse(exchange, 200, response.toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendResponse(HttpExchange exchange, int code, String response) throws Exception {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void sendError(HttpExchange exchange, Exception e) {
        try {
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            sendResponse(exchange, 500, error.toString());
        } catch (Exception ex) {}
    }

    public void stopServer() {
        if (server != null) server.stop(0);
    }
}