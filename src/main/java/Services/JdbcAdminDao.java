package Services;

import Models.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcAdminDao {
    private final Connection connection;

    public JdbcAdminDao(Connection connection) {
        this.connection = connection;
    }

    public boolean existsByUserId(long userId) throws SQLException {
        String sql = "SELECT 1 FROM admins WHERE user_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insert(Admin admin) throws SQLException {
        if (admin.getUserId() == null) {
            throw new IllegalArgumentException("admin.userId obligatoire pour insert dans admins");
        }
        String sql = "INSERT INTO admins(user_id, admin_code) VALUES (?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, admin.getUserId());
            if (admin.getAdminCode() == null || admin.getAdminCode().isBlank()) {
                ps.setNull(2, java.sql.Types.VARCHAR);
            } else {
                ps.setString(2, admin.getAdminCode());
            }
            ps.executeUpdate();
        }
    }
}
