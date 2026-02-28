package Services;

import Models.Role;
import Models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserDao implements UserDao {

    private final Connection connection;

    public JdbcUserDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public long insert(User user) throws SQLException {
        String sql = "INSERT INTO users(email, password_hash, full_name, profile_photo, fingerprint_template, face_template, role, is_active, is_banned) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            if (user.getProfilePhoto() == null || user.getProfilePhoto().isBlank()) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, user.getProfilePhoto());
            }
            if (user.getFingerprintTemplate() == null || user.getFingerprintTemplate().length == 0) {
                ps.setNull(5, Types.BLOB);
            } else {
                ps.setBytes(5, user.getFingerprintTemplate());
            }
            if (user.getFaceTemplate() == null || user.getFaceTemplate().length == 0) {
                ps.setNull(6, Types.BLOB);
            } else {
                ps.setBytes(6, user.getFaceTemplate());
            }
            ps.setString(7, user.getRole().name());
            ps.setBoolean(8, user.isActive());
            ps.setBoolean(9, user.isBanned());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    user.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Insertion users: aucun id généré");
    }

    @Override
    public Optional<User> findById(long id) throws SQLException {
        String sql = "SELECT id, email, password_hash, full_name, profile_photo, fingerprint_template, face_template, role, is_active, is_banned, created_at, updated_at FROM users WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, email, password_hash, full_name, profile_photo, fingerprint_template, face_template, role, is_active, is_banned, created_at, updated_at FROM users WHERE email=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, email, password_hash, full_name, profile_photo, fingerprint_template, face_template, role, is_active, is_banned, created_at, updated_at FROM users ORDER BY id";
        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            List<User> out = new ArrayList<>();
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        }
    }

    @Override
    public boolean update(User user) throws SQLException {
        if (user.getId() == null) {
            throw new IllegalArgumentException("user.id obligatoire pour update()");
        }
        String sql = "UPDATE users SET email=?, password_hash=?, full_name=?, profile_photo=?, fingerprint_template=?, face_template=?, role=?, is_active=?, is_banned=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            if (user.getProfilePhoto() == null || user.getProfilePhoto().isBlank()) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, user.getProfilePhoto());
            }
            if (user.getFingerprintTemplate() == null || user.getFingerprintTemplate().length == 0) {
                ps.setNull(5, Types.BLOB);
            } else {
                ps.setBytes(5, user.getFingerprintTemplate());
            }
            if (user.getFaceTemplate() == null || user.getFaceTemplate().length == 0) {
                ps.setNull(6, Types.BLOB);
            } else {
                ps.setBytes(6, user.getFaceTemplate());
            }
            ps.setString(7, user.getRole().name());
            ps.setBoolean(8, user.isActive());
            ps.setBoolean(9, user.isBanned());
            ps.setLong(10, user.getId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean deleteById(long id) throws SQLException {
        String sql = "DELETE FROM users WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Compte les utilisateurs par rôle.
     */
    public long countByRole(Role role) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return 0L;
                return rs.getLong(1);
            }
        }
    }

    /**
     * Stat: nombre d'utilisateurs créés par jour sur les derniers {@code days}
     * jours (inclus aujourd'hui).
     * Retourne une liste triée par date asc.
     */
    public List<DailyCount> countCreatedUsersLastDays(int days) throws SQLException {
        if (days <= 0)
            throw new IllegalArgumentException("days doit être > 0");

        String sql = "SELECT DATE(created_at) as d, COUNT(*) as c " +
                "FROM users " +
                "WHERE created_at >= (CURRENT_DATE - INTERVAL ? DAY) " +
                "GROUP BY DATE(created_at) " +
                "ORDER BY d";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, days - 1);
            try (ResultSet rs = ps.executeQuery()) {
                List<DailyCount> out = new ArrayList<>();
                while (rs.next()) {
                    Date d = rs.getDate("d");
                    long c = rs.getLong("c");
                    out.add(new DailyCount(d.toLocalDate(), c));
                }
                return out;
            }
        }
    }

    public record DailyCount(java.time.LocalDate date, long count) {
    }

    /**
     * Compare deux templates d'empreintes et retourne un score de similarité
     * 
     * @param template1 Premier template
     * @param template2 Deuxième template
     * @return Score entre 0.0 (aucune similarité) et 1.0 (identique)
     */
    private double compareFingerprintTemplates(byte[] template1, byte[] template2) {
        if (template1 == null || template2 == null)
            return 0.0;
        if (template1.length == 0 || template2.length == 0)
            return 0.0;

        // Méthode 1 : Comparaison exacte (pour templates identiques)
        if (java.util.Arrays.equals(template1, template2)) {
            return 1.0;
        }

        // Méthode 2 : Calcul de similarité par blocs (Hamming distance normalisée)
        int minLength = Math.min(template1.length, template2.length);
        int matches = 0;
        int total = 0;

        // Comparer par blocs de 8 bytes
        int blockSize = 8;
        for (int i = 0; i < minLength - blockSize; i += blockSize) {
            int blockMatches = 0;
            for (int j = 0; j < blockSize && (i + j) < minLength; j++) {
                if (template1[i + j] == template2[i + j]) {
                    blockMatches++;
                }
            }
            if (blockMatches >= blockSize * 0.75) { // 75% de correspondance dans le bloc
                matches++;
            }
            total++;
        }

        if (total == 0)
            return 0.0;

        double similarity = (double) matches / total;

        // Méthode 3 : Vérification du header (les premiers bytes devraient être
        // similaires)
        int headerLength = Math.min(32, minLength);
        int headerMatches = 0;
        for (int i = 0; i < headerLength; i++) {
            if (template1[i] == template2[i]) {
                headerMatches++;
            }
        }
        double headerSimilarity = (double) headerMatches / headerLength;

        // Score final : moyenne pondérée
        return (similarity * 0.7) + (headerSimilarity * 0.3);
    }

    /**
     * Vérifie si une empreinte digitale est déjà associée à un autre utilisateur
     * 
     * @param fingerprintHash Le hash de l'empreinte à vérifier
     * @param excludeUserId   L'ID de l'utilisateur à exclure de la recherche (pour
     *                        éviter de se trouver soi-même)
     * @return Optional contenant l'utilisateur si l'empreinte existe déjà, vide
     *         sinon
     */
    public Optional<User> findByFingerprintHash(byte[] fingerprintHash, Long excludeUserId) throws SQLException {
        if (fingerprintHash == null || fingerprintHash.length == 0) {
            return Optional.empty();
        }

        String sql = "SELECT id, email, password_hash, full_name, profile_photo, fingerprint_template, face_template, role, is_active, is_banned, created_at, updated_at "
                +
                "FROM users WHERE fingerprint_template IS NOT NULL";

        if (excludeUserId != null) {
            sql += " AND id != ?";
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (excludeUserId != null) {
                ps.setLong(1, excludeUserId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    byte[] storedHash = rs.getBytes("fingerprint_template");

                    // ✅ NOUVELLE COMPARAISON : Score de similarité
                    double similarity = compareFingerprintTemplates(fingerprintHash, storedHash);

                    // Si similarité >= 60%, on considère que c'est la même empreinte
                    if (similarity >= 0.60) {
                        System.out.println("⚠️ EMPREINTE SIMILAIRE DÉTECTÉE ! Score: " + (similarity * 100) + "%");
                        return Optional.of(map(rs));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setProfilePhoto(rs.getString("profile_photo"));
        u.setFingerprintTemplate(rs.getBytes("fingerprint_template"));
        u.setFaceTemplate(rs.getBytes("face_template"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActive(rs.getBoolean("is_active"));
        u.setBanned(rs.getBoolean("is_banned"));

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        if (created != null)
            u.setCreatedAt(created.toInstant());
        if (updated != null)
            u.setUpdatedAt(updated.toInstant());
        return u;
    }
}
