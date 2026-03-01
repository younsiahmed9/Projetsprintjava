package Services;

import Models.Budget;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

public class SmsService {
    private static final String CONFIG_FILE = "twilio.properties";

    public SmsResult sendBudgetExceededSms(Budget budget, double montantDemande) {
        SmsConfig config = loadConfig();
        ensureSmsTable();

        if (!config.isValid()) {
            logSms(budget.getIdBudget(), montantDemande, budget.getMontantTotal(),
                    config.toNumber, "CONFIG_ERROR", "Missing or invalid Twilio config");
            return new SmsResult(false, "Configuration SMS manquante.");
        }

        if (isCooldownActive(budget.getIdBudget(), config.toNumber, config.cooldownMinutes)) {
            logSms(budget.getIdBudget(), montantDemande, budget.getMontantTotal(),
                    config.toNumber, "SKIPPED_COOLDOWN", "Cooldown active");
            return new SmsResult(false, "SMS deja envoye recemment (cooldown 5 min)."
            );
        }

        String body = buildMessage(budget, montantDemande);

        try {
            Twilio.init(config.accountSid, config.authToken);
            if (!config.messagingServiceSid.isEmpty()) {
                Message.creator(
                        new PhoneNumber(config.toNumber),
                        config.messagingServiceSid,
                        body
                ).create();
            } else {
                Message.creator(
                        new PhoneNumber(config.toNumber),
                        new PhoneNumber(config.fromNumber),
                        body
                ).create();
            }

            logSms(budget.getIdBudget(), montantDemande, budget.getMontantTotal(),
                    config.toNumber, "SENT", body);
            return new SmsResult(true, "SMS envoye.");
        } catch (Exception e) {
            logSms(budget.getIdBudget(), montantDemande, budget.getMontantTotal(),
                    config.toNumber, "ERROR", e.getMessage());
            return new SmsResult(false, "Echec envoi SMS: " + e.getMessage());
        }
    }

    private String buildMessage(Budget budget, double montantDemande) {
        return String.format(
                "FinTrack: budget '%s' insuffisant. Depense demandee: %.2f DT. Restant: %.2f DT. "
                        + "Pensez a augmenter le budget.",
                budget.getNomBudget(),
                montantDemande,
                budget.getMontantTotal()
        );
    }

    private SmsConfig loadConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            // Ignore and validate later.
        }

        SmsConfig config = new SmsConfig();
        config.accountSid = props.getProperty("twilio.accountSid", "").trim();
        config.authToken = props.getProperty("twilio.authToken", "").trim();
        config.fromNumber = props.getProperty("twilio.fromNumber", "").trim();
        config.messagingServiceSid = props.getProperty("twilio.messagingServiceSid", "").trim();
        config.toNumber = props.getProperty("twilio.toNumber", "").trim();
        String cooldown = props.getProperty("twilio.cooldownMinutes", "5").trim();
        try {
            config.cooldownMinutes = Integer.parseInt(cooldown);
        } catch (NumberFormatException e) {
            config.cooldownMinutes = 5;
        }
        return config;
    }


    private void ensureSmsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS sms_notification ("
                + "id_sms INT AUTO_INCREMENT PRIMARY KEY, "
                + "id_budget INT NOT NULL, "
                + "montant_demande DOUBLE NOT NULL, "
                + "montant_restant DOUBLE NOT NULL, "
                + "phone VARCHAR(30) NOT NULL, "
                + "status VARCHAR(30) NOT NULL, "
                + "message TEXT, "
                + "date_notified TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/fintrack", "root", "");
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println("Erreur lors de la creation de la table sms_notification:");
            e.printStackTrace();
        }
    }

    private boolean isCooldownActive(int budgetId, String phone, int cooldownMinutes) {
        String sql = "SELECT date_notified FROM sms_notification "
                + "WHERE id_budget = ? AND phone = ? "
                + "ORDER BY date_notified DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/fintrack", "root", "");
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, budgetId);
            ps.setString(2, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("date_notified");
                    if (ts == null) {
                        return false;
                    }
                    Duration elapsed = Duration.between(ts.toInstant(), Instant.now());
                    return elapsed.toMinutes() < cooldownMinutes;
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du check cooldown SMS:");
            e.printStackTrace();
        }
        return false;
    }

    private void logSms(int budgetId, double montantDemande, double montantRestant,
                        String phone, String status, String message) {
        String sql = "INSERT INTO sms_notification "
                + "(id_budget, montant_demande, montant_restant, phone, status, message) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/fintrack", "root", "");
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, budgetId);
            ps.setDouble(2, montantDemande);
            ps.setDouble(3, montantRestant);
            ps.setString(4, phone);
            ps.setString(5, status);
            ps.setString(6, message);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erreur lors du log SMS:");
            e.printStackTrace();
        }
    }

    public static class SmsResult {
        private final boolean sent;
        private final String userMessage;

        public SmsResult(boolean sent, String userMessage) {
            this.sent = sent;
            this.userMessage = userMessage;
        }

        public boolean isSent() {
            return sent;
        }

        public String getUserMessage() {
            return userMessage;
        }
    }

    private static class SmsConfig {
        private String accountSid;
        private String authToken;
        private String fromNumber;
        private String messagingServiceSid;
        private String toNumber;
        private int cooldownMinutes;

        private boolean isValid() {
            boolean hasSender = !fromNumber.isEmpty() || !messagingServiceSid.isEmpty();
            return !accountSid.isEmpty()
                    && !authToken.isEmpty()
                    && !toNumber.isEmpty()
                    && hasSender
                    && !accountSid.startsWith("ACxxxxx");
        }
    }
}
