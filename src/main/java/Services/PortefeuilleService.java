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

    // CREATE
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
}