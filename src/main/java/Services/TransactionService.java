package Services;

import Models.*;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {

    public TransactionService() {}

    public void ajouter(Transaction t) {
        String sql = "INSERT INTO transaction (montant, devise, type, statut, carte_source_id, carte_dest_id, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, t.getMontant());
            ps.setString(2, t.getDevise());
            ps.setString(3, t.getType().name());
            ps.setString(4, t.getStatut().name());
            if (t.getCarteSourceId() != null) {
                ps.setInt(5, t.getCarteSourceId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            if (t.getCarteDestId() != null) {
                ps.setInt(6, t.getCarteDestId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, t.getDescription());
            ps.executeUpdate();
            System.out.println("✅ Transaction enregistrée");
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout transaction: " + e.getMessage());
        }
    }

    public List<Transaction> afficherTous() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transaction ORDER BY date DESC";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur affichage transactions: " + e.getMessage());
        }
        return list;
    }

    public Transaction afficherParId(int id) {
        String sql = "SELECT * FROM transaction WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche transaction: " + e.getMessage());
        }
        return null;
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM transaction WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Transaction supprimée (ID: " + id + ")");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression transaction: " + e.getMessage());
        }
    }

    private Transaction mapResultSet(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setMontant(rs.getDouble("montant"));
        t.setDevise(rs.getString("devise"));
        t.setDate(rs.getTimestamp("date").toLocalDateTime());
        t.setType(rs.getString("type"));
        t.setStatut(rs.getString("statut"));
        int sourceId = rs.getInt("carte_source_id");
        if (!rs.wasNull()) {
            t.setCarteSourceId(sourceId);
        }
        int destId = rs.getInt("carte_dest_id");
        if (!rs.wasNull()) {
            t.setCarteDestId(destId);
        }
        t.setDescription(rs.getString("description"));
        return t;
    }
}