package Services;

import Models.ScheduledTransfer;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduledTransferService {

    // CREATE
    public void ajouter(ScheduledTransfer st) {
        String sql = "INSERT INTO scheduled_transfer (user_id, from_card_id, to_card_id, amount, scheduled_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, st.getUserId());
            ps.setInt(2, st.getFromCardId());
            ps.setInt(3, st.getToCardId());
            ps.setDouble(4, st.getAmount());
            ps.setTimestamp(5, Timestamp.valueOf(st.getScheduledDate()));
            ps.setString(6, st.getStatus());
            ps.executeUpdate();
            System.out.println("✅ Transfert programmé ajouté");
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout transfert programmé: " + e.getMessage());
        }
    }


    public List<ScheduledTransfer> getByUser(int userId) {
        List<ScheduledTransfer> list = new ArrayList<>();
        String sql = "SELECT * FROM scheduled_transfer WHERE user_id = ? ORDER BY scheduled_date";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération transferts programmés: " + e.getMessage());
        }
        return list;
    }



    // READ ALL PENDING (due now or before)
    public List<ScheduledTransfer> getPendingDue() {
        List<ScheduledTransfer> list = new ArrayList<>();
        String sql = "SELECT * FROM scheduled_transfer WHERE status = 'PENDING' AND scheduled_date <= ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération transferts programmés: " + e.getMessage());
        }
        return list;
    }

    // UPDATE status and attempts
    public void updateStatus(int id, String status, int attempts, String errorMessage) {
        String sql = "UPDATE scheduled_transfer SET status = ?, attempts = ?, last_attempt = ?, error_message = ? WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, attempts);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, errorMessage);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour transfert programmé: " + e.getMessage());
        }
    }

    // Map ResultSet to ScheduledTransfer
    private ScheduledTransfer mapResultSet(ResultSet rs) throws SQLException {
        ScheduledTransfer st = new ScheduledTransfer();
        st.setId(rs.getInt("id"));
        st.setUserId(rs.getInt("user_id"));
        st.setFromCardId(rs.getInt("from_card_id"));
        st.setToCardId(rs.getInt("to_card_id"));
        st.setAmount(rs.getDouble("amount"));
        st.setScheduledDate(rs.getTimestamp("scheduled_date").toLocalDateTime());
        st.setStatus(rs.getString("status"));
        st.setAttempts(rs.getInt("attempts"));
        Timestamp last = rs.getTimestamp("last_attempt");
        if (last != null) st.setLastAttempt(last.toLocalDateTime());
        st.setErrorMessage(rs.getString("error_message"));
        st.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        st.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return st;
    }
}