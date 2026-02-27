package Controllers;

import Services.EmailVerificationDao;
import Models.EmailVerification;
import utils.Db;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

@WebServlet(name = "VerifyEmailServlet", urlPatterns = {"/verify-email"})
public class VerifyEmailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String token = req.getParameter("token");
        resp.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            out.println("<!doctype html><html><head><meta charset=\"utf-8\"><title>Vérification email</title>");
            out.println("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">" );
            out.println("<style>body{font-family:Arial,Helvetica,sans-serif;background:#f6f6f6;color:#222;padding:40px} .card{max-width:540px;margin:40px auto;padding:24px;background:#fff;border-radius:8px;box-shadow:0 6px 20px rgba(0,0,0,.08)} h1{color:#2e7d32} p{line-height:1.4}</style>");
            out.println("</head><body><div class=\"card\">");

            if (token == null || token.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("<h1>Token manquant</h1><p>Le lien de vérification est invalide.</p>");
                out.println("</div></body></html>");
                return;
            }

            try (Connection conn = Db.getConnection()) {
                EmailVerificationDao evDao = new EmailVerificationDao(conn);
                Optional<EmailVerification> maybe = evDao.findByToken(token);
                if (maybe.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("<h1>Token invalide</h1><p>Le lien de vérification est invalide ou a déjà été utilisé.</p>");
                    out.println("</div></body></html>");
                    return;
                }

                EmailVerification ev = maybe.get();
                if (ev.isUsed()) {
                    out.println("<h1>Token déjà utilisé</h1><p>Ce lien de vérification a déjà été utilisé. Vous pouvez vous connecter.</p>");
                    out.println("</div></body></html>");
                    return;
                }

                if (ev.getExpiresAt() != null && ev.getExpiresAt().isBefore(Instant.now())) {
                    resp.setStatus(HttpServletResponse.SC_GONE);
                    out.println("<h1>Token expiré</h1><p>Le lien de vérification a expiré. Veuillez demander un nouveau lien depuis l'application.</p>");
                    out.println("</div></body></html>");
                    return;
                }

                // marque le token comme utilisé
                evDao.markUsed(ev.getId());

                // active l'utilisateur
                try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET is_active = 1 WHERE id = ?")) {
                    ps.setLong(1, ev.getUserId());
                    ps.executeUpdate();
                }

                // affichage succès simple (pas de formulaire)
                out.println("<h1>Adresse e-mail vérifiée</h1>");
                out.println("<p>Votre adresse e-mail a été vérifiée avec succès. Vous pouvez fermer cette fenêtre et vous connecter à l'application.</p>");
                out.println("</div></body></html>");

            } catch (SQLException ex) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("<h1>Erreur serveur</h1><p>Impossible de vérifier votre adresse pour le moment. Réessayez plus tard.</p>");
                out.println("</div></body></html>");
            }
        }
    }
}
