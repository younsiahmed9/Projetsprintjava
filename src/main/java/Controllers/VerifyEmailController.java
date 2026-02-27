package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import Models.User;
import Models.EmailVerification;
import Services.EmailVerificationDao;
import Services.JdbcUserDao;
import Services.MailService;
import utils.Db;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.*;
import java.time.Instant;
import java.sql.Connection;

@Controller
public class VerifyEmailController {
    @FXML private TextField codeField;
    @FXML private Button verifyBtn;
    @FXML private Label statusLabel;
    @FXML private Button resendBtn;

    private User currentUser;

    @Autowired
    private Environment env;

    public void setUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void onVerifyCode() {
        String code = codeField.getText();
        if (code == null || code.length() != 6) {
            statusLabel.setText("Code invalide.");
            return;
        }
        try (Connection cn = Db.getConnection()) {
            EmailVerificationDao dao = new EmailVerificationDao(cn);
            var evOpt = dao.findLatestByUserId(currentUser.getId());
            if (evOpt.isEmpty()) {
                statusLabel.setText("Aucun code trouvé.");
                return;
            }
            EmailVerification ev = evOpt.get();
            if (ev.isUsed()) {
                statusLabel.setText("Code déjà utilisé.");
                return;
            }
            if (ev.getExpiresAt() != null && ev.getExpiresAt().isBefore(Instant.now())) {
                statusLabel.setText("Code expiré.");
                return;
            }
            if (!ev.getToken().equals(code)) {
                statusLabel.setText("Code incorrect.");
                return;
            }
            // Marquer le code comme utilisé et activer l'utilisateur
            dao.markUsed(ev.getId());
            JdbcUserDao userDao = new JdbcUserDao(cn);
            currentUser.setActive(true);
            userDao.update(currentUser);
            statusLabel.setText("Email vérifié avec succès !");
            // rediriger vers la page de login après succès
            SceneNavigator.goLogin();
        } catch (Exception ex) {
            statusLabel.setText("Erreur: " + ex.getMessage());
        }
    }

    @FXML
    public void onResendCode() {
        try (Connection cn = Db.getConnection()) {
            String code = generateVerificationCode();
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(600); // 10 min
            EmailVerification ev = new EmailVerification(currentUser.getId(), code, now, expiresAt, false);
            EmailVerificationDao dao = new EmailVerificationDao(cn);
            dao.save(ev);
            String smtpUser = utils.AppConfig.get("mail.smtp.user");
            String smtpPass = utils.AppConfig.get("mail.smtp.password");
            String smtpFrom = utils.AppConfig.get("mail.from");
            MailService mailService = new MailService(smtpUser, smtpPass, smtpFrom);
            mailService.sendVerificationCode(currentUser.getEmail(), code);
            statusLabel.setText("Code renvoyé par mail.");
        } catch (Exception ex) {
            statusLabel.setText("Erreur: " + ex.getMessage());
        }
    }

    private String generateVerificationCode() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    @GetMapping(path = "/verify-email", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String verifyEmail(@RequestParam(required = true) String token) {
        String dbUrl = env.getProperty("db.url");
        String dbUser = env.getProperty("db.user");
        String dbPass = env.getProperty("db.password");

        if (token == null || token.isBlank()) {
            return html("Token manquant", "Le lien de vérification ne contient pas de token.");
        }

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            // chercher le token
            try (PreparedStatement ps = conn.prepareStatement("SELECT id, user_id, expires_at, verified FROM email_verification WHERE token = ?")) {
                ps.setString(1, token);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return html("Token invalide", "Le lien de vérification est invalide ou a déjà été utilisé.");
                    }

                    long evId = rs.getLong("id");
                    long userId = rs.getLong("user_id");
                    Timestamp expiresAt = rs.getTimestamp("expires_at");
                    boolean verified = rs.getBoolean("verified");

                    if (verified) {
                        return html("Déjà vérifié", "Cet email a déjà été vérifié. Vous pouvez vous connecter.");
                    }

                    if (expiresAt != null && expiresAt.before(Timestamp.from(Instant.now()))) {
                        return html("Lien expiré", "Le lien de vérification a expiré. Veuillez redemander un nouvel email.");
                    }

                    // marquer verified=true
                    try (PreparedStatement upd = conn.prepareStatement("UPDATE email_verification SET verified = TRUE WHERE id = ?")) {
                        upd.setLong(1, evId);
                        upd.executeUpdate();
                    }

                    // activer l'utilisateur (is_active = 1)
                    try (PreparedStatement upd2 = conn.prepareStatement("UPDATE users SET is_active = 1 WHERE id = ?")) {
                        upd2.setLong(1, userId);
                        upd2.executeUpdate();
                    }

                    return html("Vérification réussie", "Votre adresse email a été vérifiée avec succès. Vous pouvez maintenant vous connecter.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return html("Erreur serveur", "Une erreur est survenue lors de la vérification (" + escape(e.getMessage()) + ").");
        }
    }

    private String html(String title, String body) {
        return "<!doctype html><html><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>" + escape(title) + "</title>"
                + "<style>body{font-family:Arial,Helvetica,sans-serif;background:#f5f5f5;color:#222;padding:40px} .card{max-width:720px;margin:40px auto;padding:28px;background:#fff;border-radius:8px;box-shadow:0 6px 18px rgba(0,0,0,0.12)} h1{margin:0 0 8px;font-size:20px} p{margin:0;line-height:1.5} a.button{display:inline-block;margin-top:14px;padding:10px 16px;background:#2b8aef;color:#fff;text-decoration:none;border-radius:6px}</style></head><body>"
                + "<div class=\"card\"><h1>" + escape(title) + "</h1><p>" + escape(body) + "</p>"
                + "<p><a class=\"button\" href=\"/\">Retour à la page d'accueil</a></p></div></body></html>";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;");
    }

    @FXML
    public void initialize() {
        // Suppression de toute référence à ResponsiveUtil
    }
}
