package Services;

import Models.*;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {
    private Connection connection;

    public TransactionService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    // CREATE
    public void ajouter(Transaction t) {
        String req = "INSERT INTO transaction (montant, devise, type, statut, carte_source_id, carte_dest_id, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setDouble(1, t.getMontant());
            ps.setString(2, t.getDevise());
            ps.setString(3, t.getType().name());
            ps.setString(4, t.getStatut().name());
            ps.setObject(5, t.getCarteSourceId());
            ps.setObject(6, t.getCarteDestId());
            ps.setString(7, t.getDescription());
            ps.executeUpdate();
            System.out.println("✅ Transaction enregistrée");
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    // READ ALL
    public List<Transaction> afficherTous() {
        List<Transaction> list = new ArrayList<>();
        String req = "SELECT * FROM transaction ORDER BY date DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setMontant(rs.getDouble("montant"));
                t.setDevise(rs.getString("devise"));
                t.setDate(rs.getTimestamp("date").toLocalDateTime());
                t.setType(rs.getString("type"));
                t.setStatut(rs.getString("statut"));
                t.setCarteSourceId(rs.getInt("carte_source_id"));
                t.setCarteDestId(rs.getInt("carte_dest_id"));
                t.setDescription(rs.getString("description"));
                list.add(t);
                System.out.println("   " + t);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
        return list;
    }

    // DELETE
    public void supprimer(int id) {
        String req = "DELETE FROM transaction WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Transaction supprimée");
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }
}