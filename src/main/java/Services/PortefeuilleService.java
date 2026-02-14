package Services;

import Models.Portefeuille;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PortefeuilleService {
    private Connection connection;

    public PortefeuilleService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    // CREATE (legacy)
    public void ajouter(Portefeuille p) {
        String req = "INSERT INTO portefeuille (nom, solde_total, devise_principale) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getSoldeTotal());
            ps.setString(3, p.getDevisePrincipale());
            ps.executeUpdate();
            System.out.println("✅ Portefeuille ajouté");
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    /**
     * CREATE portefeuille rattaché à un utilisateur (Option A).
     */
    public void ajouterPourUtilisateur(Portefeuille p, int utilisateurId) {
        String req = "INSERT INTO portefeuille (nom, solde_total, devise_principale, utilisateur_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getSoldeTotal());
            ps.setString(3, p.getDevisePrincipale());
            ps.setInt(4, utilisateurId);
            ps.executeUpdate();
            System.out.println("✅ Portefeuille ajouté (user_id=" + utilisateurId + ")");
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    // READ ALL
    public List<Portefeuille> afficherTous() {
        List<Portefeuille> list = new ArrayList<>();
        String req = "SELECT * FROM portefeuille";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Portefeuille p = new Portefeuille();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setSoldeTotal(rs.getDouble("solde_total"));
                p.setDevisePrincipale(rs.getString("devise_principale"));
                try { p.setUtilisateurId(rs.getObject("utilisateur_id", Integer.class)); } catch (SQLException ignored) { }
                list.add(p);
                System.out.println("   " + p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
        return list;
    }

    // READ BY ID
    public Portefeuille afficherParId(int id) {
        String req = "SELECT * FROM portefeuille WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Portefeuille p = new Portefeuille();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setSoldeTotal(rs.getDouble("solde_total"));
                p.setDevisePrincipale(rs.getString("devise_principale"));
                try { p.setUtilisateurId(rs.getObject("utilisateur_id", Integer.class)); } catch (SQLException ignored) { }
                System.out.println("🔍 " + p);
                return p;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
        return null;
    }

    // UPDATE
    public void modifier(Portefeuille p) {
        String req = "UPDATE portefeuille SET nom=?, solde_total=?, devise_principale=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getSoldeTotal());
            ps.setString(3, p.getDevisePrincipale());
            ps.setInt(4, p.getId());
            ps.executeUpdate();
            System.out.println("✅ Portefeuille modifié");
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    // DELETE
    public void supprimer(int id) {
        String req = "DELETE FROM portefeuille WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Portefeuille supprimé");
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    /**
     * Recalcule le solde_total d'un portefeuille à partir de la somme des soldes
     * des cartes qui lui appartiennent, puis le persist en base.
     *
     * @return le nouveau solde_total calculé
     */
    public double recomputeAndUpdateSoldeTotal(int portefeuilleId) {
        double total = 0;

        String sumReq = "SELECT COALESCE(SUM(solde), 0) AS total FROM carte_virtuelle WHERE portefeuille_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sumReq)) {
            ps.setInt(1, portefeuilleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur calcul solde_total portefeuille: " + e.getMessage());
            return total;
        }

        String updateReq = "UPDATE portefeuille SET solde_total=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(updateReq)) {
            ps.setDouble(1, total);
            ps.setInt(2, portefeuilleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur update solde_total portefeuille: " + e.getMessage());
        }

        return total;
    }

    /**
     * Ajuste le solde_total en ajoutant un delta (ex: +montant lors d'une recharge).
     * Plus rapide que le recalcul complet, mais nécessite que la base soit cohérente.
     */
    public void incrementSoldeTotal(int portefeuilleId, double delta) {
        String req = "UPDATE portefeuille SET solde_total = COALESCE(solde_total, 0) + ? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setDouble(1, delta);
            ps.setInt(2, portefeuilleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur increment solde_total: " + e.getMessage());
        }
    }

    /**
     * Retourne les portefeuilles d'un utilisateur.
     * Nécessite une colonne portefeuille.utilisateur_id. Si la colonne n'existe pas,
     * cette méthode retournera une liste vide et loguera l'erreur SQL.
     */
    public List<Portefeuille> afficherParUtilisateur(int utilisateurId) {
        List<Portefeuille> list = new ArrayList<>();
        String req = "SELECT * FROM portefeuille WHERE utilisateur_id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Portefeuille p = new Portefeuille();
                    p.setId(rs.getInt("id"));
                    p.setNom(rs.getString("nom"));
                    p.setSoldeTotal(rs.getDouble("solde_total"));
                    p.setDevisePrincipale(rs.getString("devise_principale"));
                    try { p.setUtilisateurId(rs.getObject("utilisateur_id", Integer.class)); } catch (SQLException ignored) { }
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur afficherParUtilisateur: " + e.getMessage());
        }
        return list;
    }
}