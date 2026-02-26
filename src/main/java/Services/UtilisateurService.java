package Services;

import Models.Utilisateur;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {

    public UtilisateurService() {}

    // LOGIN
    public Utilisateur login(String email, String password) {
        String sql = "SELECT * FROM utilisateur WHERE email = ? AND password = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur login: " + e.getMessage());
        }
        return null;
    }

    // READ BY ID
    public Utilisateur afficherParId(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur afficherParId: " + e.getMessage());
        }
        return null;
    }

    public Utilisateur getUserByCardId(int cardId) {
        String sql = "SELECT u.* FROM utilisateur u " +
                "INNER JOIN portefeuille p ON u.id = p.utilisateur_id " +
                "INNER JOIN carte_virtuelle c ON p.id = c.portefeuille_id " +
                "WHERE c.id = ?";

        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cardId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getUserByCardId: " + e.getMessage());
        }
        return null;
    }

    // Mettre à jour l'adresse d'un utilisateur (version simplifiée - sans API)
    public void updateAddress(int userId, String addressLine1, String addressLine2,
                              String city, String postalCode, String country) {
        String sql = "UPDATE utilisateur SET address_line1=?, address_line2=?, city=?, " +
                "postal_code=?, country=?, address_verified=?, address_confidence_score=? WHERE id=?";

        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Version simplifiée - on considère l'adresse comme vérifiée
            // Pas d'appel API complexe
            ps.setString(1, addressLine1);
            ps.setString(2, addressLine2);
            ps.setString(3, city);
            ps.setString(4, postalCode);
            ps.setString(5, country);
            ps.setBoolean(6, true); // Considérée comme vérifiée
            ps.setInt(7, 100); // Score de confiance maximal
            ps.setInt(8, userId);

            ps.executeUpdate();

            System.out.println("✅ Adresse mise à jour pour l'utilisateur ID: " + userId);

        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour adresse: " + e.getMessage());
        }
    }

    // Récupérer l'adresse complète d'un utilisateur
    public String getUserFullAddress(int userId) {
        Utilisateur user = afficherParId(userId);
        return user != null ? user.getFullAddress() : null;
    }

    // READ ALL
    public List<Utilisateur> afficherTous() {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur afficherTous: " + e.getMessage());
        }
        return list;
    }

    // CREATE
    public void ajouter(Utilisateur utilisateur) {
        String sql = "INSERT INTO utilisateur (email, nom, prenom, password, role, solde) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, utilisateur.getEmail());
            ps.setString(2, utilisateur.getNom());
            ps.setString(3, utilisateur.getPrenom());
            ps.setString(4, utilisateur.getPassword());
            ps.setString(5, utilisateur.getRole());
            ps.setDouble(6, utilisateur.getSolde());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                utilisateur.setId(rs.getInt(1));
            }
            System.out.println("✅ Utilisateur ajouté: " + utilisateur.getEmail());
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout utilisateur: " + e.getMessage());
        }
    }

    // UPDATE
    public void modifier(Utilisateur utilisateur) {
        String sql = "UPDATE utilisateur SET email = ?, nom = ?, prenom = ?, password = ?, role = ?, solde = ? WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, utilisateur.getEmail());
            ps.setString(2, utilisateur.getNom());
            ps.setString(3, utilisateur.getPrenom());
            ps.setString(4, utilisateur.getPassword());
            ps.setString(5, utilisateur.getRole());
            ps.setDouble(6, utilisateur.getSolde());
            ps.setInt(7, utilisateur.getId());
            ps.executeUpdate();
            System.out.println("✅ Utilisateur modifié: " + utilisateur.getEmail());
        } catch (SQLException e) {
            System.err.println("❌ Erreur modification: " + e.getMessage());
        }
    }

    // DELETE
    public void supprimer(int id) {
        String sql = "DELETE FROM utilisateur WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Utilisateur supprimé (ID: " + id + ")");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression: " + e.getMessage());
        }
    }

    // Helper method to map ResultSet to Utilisateur
    private Utilisateur mapResultSet(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setSolde(rs.getDouble("solde"));

        // Address fields
        u.setAddressLine1(rs.getString("address_line1"));
        u.setAddressLine2(rs.getString("address_line2"));
        u.setCity(rs.getString("city"));
        u.setPostalCode(rs.getString("postal_code"));
        u.setCountry(rs.getString("country"));
        u.setAddressVerified(rs.getBoolean("address_verified"));
        u.setAddressConfidenceScore(rs.getInt("address_confidence_score"));

        // Handle timestamps
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            u.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            u.setUpdatedAt(updated.toLocalDateTime());
        }

        return u;
    }
}