package tn.esprit.repository;

import tn.esprit.domain.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcClientDao {
    private final Connection connection;

    public JdbcClientDao(Connection connection) {
        this.connection = connection;
    }

    public boolean existsByUserId(long userId) throws SQLException {
        String sql = "SELECT 1 FROM clients WHERE user_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insert(Client client) throws SQLException {
        if (client.getUserId() == null) {
            throw new IllegalArgumentException("client.userId obligatoire pour insert dans clients");
        }
        String sql = "INSERT INTO clients(user_id, cin, phone) VALUES (?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, client.getUserId());
            if (client.getCin() == null || client.getCin().isBlank()) {
                ps.setNull(2, java.sql.Types.VARCHAR);
            } else {
                ps.setString(2, client.getCin());
            }
            if (client.getPhone() == null || client.getPhone().isBlank()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, client.getPhone());
            }
            ps.executeUpdate();
        }
    }
}
