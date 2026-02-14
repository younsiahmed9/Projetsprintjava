package tn.esprit.repository;

import tn.esprit.domain.Role;
import tn.esprit.domain.User;

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
        String sql = "INSERT INTO users(email, password_hash, full_name, profile_photo, role, is_active) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            if (user.getProfilePhoto() == null || user.getProfilePhoto().isBlank()) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, user.getProfilePhoto());
            }
            ps.setString(5, user.getRole().name());
            ps.setBoolean(6, user.isActive());
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
        String sql = "SELECT id, email, password_hash, full_name, profile_photo, role, is_active, created_at, updated_at FROM users WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, email, password_hash, full_name, profile_photo, role, is_active, created_at, updated_at FROM users WHERE email=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, email, password_hash, full_name, profile_photo, role, is_active, created_at, updated_at FROM users ORDER BY id";
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
        String sql = "UPDATE users SET email=?, password_hash=?, full_name=?, profile_photo=?, role=?, is_active=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            if (user.getProfilePhoto() == null || user.getProfilePhoto().isBlank()) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, user.getProfilePhoto());
            }
            ps.setString(5, user.getRole().name());
            ps.setBoolean(6, user.isActive());
            ps.setLong(7, user.getId());
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
                if (!rs.next()) return 0L;
                return rs.getLong(1);
            }
        }
    }

    /**
     * Stat: nombre d'utilisateurs créés par jour sur les derniers {@code days} jours (inclus aujourd'hui).
     * Retourne une liste triée par date asc.
     */
    public List<DailyCount> countCreatedUsersLastDays(int days) throws SQLException {
        if (days <= 0) throw new IllegalArgumentException("days doit être > 0");

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

    public record DailyCount(java.time.LocalDate date, long count) {}

    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setProfilePhoto(rs.getString("profile_photo"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActive(rs.getBoolean("is_active"));

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        if (created != null) u.setCreatedAt(created.toInstant());
        if (updated != null) u.setUpdatedAt(updated.toInstant());
        return u;
    }
}
