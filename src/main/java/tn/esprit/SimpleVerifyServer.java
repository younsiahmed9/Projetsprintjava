package tn.esprit;

import Models.EmailVerification;
import Services.EmailVerificationDao;
import utils.Db;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

public class SimpleVerifyServer {
    public static void main(String[] args) throws Exception {
        int port = 8081; // run on another port to avoid container security
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/verify-email", new VerifyHandler());
        server.setExecutor(null);
        System.out.println("SimpleVerifyServer started on http://localhost:" + port + "/verify-email?token=...\n");
        server.start();
    }

    static class VerifyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();
            String token = null;
            if (query != null) {
                for (String part : query.split("&")) {
                    String[] kv = part.split("=", 2);
                    if (kv.length == 2 && "token".equals(kv[0])) {
                        token = java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
                        break;
                    }
                }
            }

            String htmlStart = "<!doctype html><html><head><meta charset=\"utf-8\"><title>Vérification email</title>" +
                    "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">" +
                    "<style>body{font-family:Arial,Helvetica,sans-serif;background:#f6f6f6;color:#222;padding:40px} .card{max-width:540px;margin:40px auto;padding:24px;background:#fff;border-radius:8px;box-shadow:0 6px 20px rgba(0,0,0,.08)} h1{color:#2e7d32} p{line-height:1.4}</style>" +
                    "</head><body><div class=\"card\">";
            String htmlEnd = "</div></body></html>";

            String response;
            int status = 200;

            if (token == null || token.isEmpty()) {
                status = 400;
                response = htmlStart + "<h1>Token manquant</h1><p>Le lien de vérification est invalide.</p>" + htmlEnd;
                send(exchange, status, response);
                return;
            }

            try (Connection conn = Db.getConnection()) {
                EmailVerificationDao evDao = new EmailVerificationDao(conn);
                Optional<EmailVerification> maybe = evDao.findByToken(token);
                if (maybe.isEmpty()) {
                    status = 404;
                    response = htmlStart + "<h1>Token invalide</h1><p>Le lien de vérification est invalide ou a déjà été utilisé.</p>" + htmlEnd;
                    send(exchange, status, response);
                    return;
                }

                EmailVerification ev = maybe.get();
                if (ev.isUsed()) {
                    response = htmlStart + "<h1>Token déjà utilisé</h1><p>Ce lien de vérification a déjà été utilisé. Vous pouvez vous connecter.</p>" + htmlEnd;
                    send(exchange, status, response);
                    return;
                }

                if (ev.getExpiresAt() != null && ev.getExpiresAt().isBefore(Instant.now())) {
                    status = 410;
                    response = htmlStart + "<h1>Token expiré</h1><p>Le lien de vérification a expiré. Veuillez demander un nouveau lien depuis l'application.</p>" + htmlEnd;
                    send(exchange, status, response);
                    return;
                }

                evDao.markUsed(ev.getId());
                try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET is_active = 1 WHERE id = ?")) {
                    ps.setLong(1, ev.getUserId());
                    ps.executeUpdate();
                }

                response = htmlStart + "<h1>Adresse e-mail vérifiée</h1><p>Votre adresse e-mail a été vérifiée avec succès. Vous pouvez fermer cette fenêtre et vous connecter à l'application.</p>" + htmlEnd;
                send(exchange, status, response);

            } catch (SQLException ex) {
                status = 500;
                response = htmlStart + "<h1>Erreur serveur</h1><p>Impossible de vérifier votre adresse pour le moment. Réessayez plus tard.</p>" + htmlEnd;
                send(exchange, status, response);
            }
        }

        private void send(HttpExchange exchange, int status, String body) throws IOException {
            byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
