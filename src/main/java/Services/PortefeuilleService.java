package Services;

import Models.Portefeuille;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PortefeuilleService {

    // Plus de champ connection global – chaque méthode ouvre sa propre connexion

    // CREATE (sans utilisateur – pour tests)
    public void ajouter(Portefeuille p) {
        String req = "INSERT INTO portefeuille (nom, solde_total, devise_principale) VALUES (?, ?, ?)";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getSoldeTotal());
            ps.setString(3, p.getDevisePrincipale());
            ps.executeUpdate();
            System.out.println("✅ Portefeuille ajouté (sans utilisateur)");
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout: " + e.getMessage());
        }
    }

    // CREATE avec utilisateur_id
    public void ajouterPourUtilisateur(Portefeuille p, int utilisateurId) {
        String req = "INSERT INTO portefeuille (nom, solde_total, devise_principale, utilisateur_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getSoldeTotal());
            ps.setString(3, p.getDevisePrincipale());
            ps.setInt(4, utilisateurId);
            ps.executeUpdate();
            System.out.println("✅ Portefeuille ajouté (user_id=" + utilisateurId + ")");
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout pour utilisateur: " + e.getMessage());
        }
    }

    // READ ALL
    public List<Portefeuille> afficherTous() {
        List<Portefeuille> list = new ArrayList<>();
        String req = "SELECT * FROM portefeuille";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Portefeuille p = mapResultSetToPortefeuille(rs);
                list.add(p);
                System.out.println("   " + p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur affichage: " + e.getMessage());
        }
        return list;
    }

    // READ BY ID
    public Portefeuille afficherParId(int id) {
        String req = "SELECT * FROM portefeuille WHERE id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Portefeuille p = mapResultSetToPortefeuille(rs);
                    System.out.println("🔍 " + p);
                    return p;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche: " + e.getMessage());
        }
        return null;
    }

    // READ BY UTILISATEUR
    public List<Portefeuille> getPortefeuillesByUtilisateur(int utilisateurId) {
        List<Portefeuille> list = new ArrayList<>();
        String req = "SELECT * FROM portefeuille WHERE utilisateur_id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPortefeuille(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getPortefeuillesByUtilisateur: " + e.getMessage());
        }
        return list;
    }

    // UPDATE
    public void modifier(Portefeuille p) {
        String req = "UPDATE portefeuille SET nom=?, solde_total=?, devise_principale=? WHERE id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getSoldeTotal());
            ps.setString(3, p.getDevisePrincipale());
            ps.setInt(4, p.getId());
            ps.executeUpdate();
            System.out.println("✅ Portefeuille modifié");
        } catch (SQLException e) {
            System.err.println("❌ Erreur modification: " + e.getMessage());
        }
    }

    // DELETE
    public void supprimer(int id) {
        String req = "DELETE FROM portefeuille WHERE id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Portefeuille supprimé (ID: " + id + ")");
            } else {
                System.out.println("❌ Aucun portefeuille avec l'ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression: " + e.getMessage());
        }
    }

    // Recalculer et mettre à jour le solde total d'un portefeuille
    public double recomputeAndUpdateSoldeTotal(int portefeuilleId) {
        double total = 0;
        String sumReq = "SELECT COALESCE(SUM(solde), 0) AS total FROM carte_virtuelle WHERE portefeuille_id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sumReq)) {
            ps.setInt(1, portefeuilleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur calcul solde_total: " + e.getMessage());
            return total;
        }

        String updateReq = "UPDATE portefeuille SET solde_total=? WHERE id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(updateReq)) {
            ps.setDouble(1, total);
            ps.setInt(2, portefeuilleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur update solde_total: " + e.getMessage());
        }

        return total;
    }

    // Incrémenter le solde total (utile pour les recharges)
    public void incrementSoldeTotal(int portefeuilleId, double delta) {
        String req = "UPDATE portefeuille SET solde_total = COALESCE(solde_total, 0) + ? WHERE id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setDouble(1, delta);
            ps.setInt(2, portefeuilleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur increment solde_total: " + e.getMessage());
        }
    }

    // Méthode privée pour mapper un ResultSet à un objet Portefeuille
    private Portefeuille mapResultSetToPortefeuille(ResultSet rs) throws SQLException {
        Portefeuille p = new Portefeuille();
        p.setId(rs.getInt("id"));
        p.setNom(rs.getString("nom"));
        p.setSoldeTotal(rs.getDouble("solde_total"));
        p.setDevisePrincipale(rs.getString("devise_principale"));
        // Gestion de utilisateur_id (peut être NULL)
        int utilisateurId = rs.getInt("utilisateur_id");
        if (!rs.wasNull()) {
            p.setUtilisateurId(utilisateurId);
        }
        // On ignore les dates pour l'instant, mais on pourrait les ajouter si besoin
        return p;
    }

    // Pour compatibilité avec l'ancien nom (si vous voulez conserver les deux)
    public List<Portefeuille> afficherParUtilisateur(int utilisateurId) {
        return getPortefeuillesByUtilisateur(utilisateurId);
    }
}