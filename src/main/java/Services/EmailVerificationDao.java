package Services;

import Models.EmailVerification;
import java.sql.*;
import java.util.Optional;
import java.time.Instant;

public class EmailVerificationDao {
    private final Connection connection;

    public EmailVerificationDao(Connection connection) {
        this.connection = connection;
    }

    public void insert(EmailVerification ev) throws SQLException {
        String sql = "INSERT INTO email_verification(user_id, token, created_at, expires_at, used) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ev.getUserId());
            ps.setString(2, ev.getToken());
            if (ev.getCreatedAt() != null) ps.setTimestamp(3, Timestamp.from(ev.getCreatedAt()));
            else ps.setTimestamp(3, null);
            if (ev.getExpiresAt() != null) ps.setTimestamp(4, Timestamp.from(ev.getExpiresAt()));
            else ps.setTimestamp(4, null);
            ps.setBoolean(5, ev.isUsed());
            ps.executeUpdate();
        }
    }

    public Optional<EmailVerification> findByToken(String token) throws SQLException {
        String sql = "SELECT * FROM email_verification WHERE token = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                EmailVerification ev = new EmailVerification();
                ev.setId(rs.getLong("id"));
                ev.setUserId(rs.getLong("user_id"));
                ev.setToken(rs.getString("token"));
                Timestamp createdTs = rs.getTimestamp("created_at");
                if (createdTs != null) ev.setCreatedAt(createdTs.toInstant());
                else ev.setCreatedAt(null);
                Timestamp expiresTs = rs.getTimestamp("expires_at");
                if (expiresTs != null) ev.setExpiresAt(expiresTs.toInstant());
                else ev.setExpiresAt(null);
                ev.setUsed(rs.getBoolean("used"));
                return Optional.of(ev);
            }
        }
        return Optional.empty();
    }

    public void markUsed(Long id) throws SQLException {
        String sql = "UPDATE email_verification SET used = true WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Sauvegarde un code de vérification (code numérique)
     */
    public void save(EmailVerification ev) {
        try {
            String sql = "INSERT INTO email_verification(user_id, token, created_at, expires_at, used) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, ev.getUserId());
                ps.setString(2, ev.getToken());
                if (ev.getCreatedAt() != null) ps.setTimestamp(3, Timestamp.from(ev.getCreatedAt()));
                else ps.setTimestamp(3, null);
                if (ev.getExpiresAt() != null) ps.setTimestamp(4, Timestamp.from(ev.getExpiresAt()));
                else ps.setTimestamp(4, null);
                ps.setBoolean(5, ev.isUsed());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Erreur sauvegarde code vérification: " + e.getMessage());
        }
    }

    /**
     * Recherche le dernier code de vérification pour un utilisateur
     */
    public Optional<EmailVerification> findLatestByUserId(Long userId) {
        try {
            String sql = "SELECT * FROM email_verification WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    EmailVerification ev = new EmailVerification();
                    ev.setId(rs.getLong("id"));
                    ev.setUserId(rs.getLong("user_id"));
                    ev.setToken(rs.getString("token"));
                    Timestamp createdTs = rs.getTimestamp("created_at");
                    if (createdTs != null) ev.setCreatedAt(createdTs.toInstant());
                    else ev.setCreatedAt(null);
                    Timestamp expiresTs = rs.getTimestamp("expires_at");
                    if (expiresTs != null) ev.setExpiresAt(expiresTs.toInstant());
                    else ev.setExpiresAt(null);
                    ev.setUsed(rs.getBoolean("used"));
                    return Optional.of(ev);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche code vérification: " + e.getMessage());
        }
        return Optional.empty();
    }
}
