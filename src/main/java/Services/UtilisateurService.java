package Services;

import Models.Utilisateur;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService implements IService<Utilisateur> {
    private Connection connection;

    public UtilisateurService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public Utilisateur ajouter(Utilisateur utilisateur) {
        String req = "INSERT INTO utilisateur (email, nom, prenom, solde) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, utilisateur.getEmail());
            ps.setString(2, utilisateur.getNom());
            ps.setString(3, utilisateur.getPrenom());
            ps.setDouble(4, utilisateur.getSolde());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                utilisateur.setId(rs.getInt(1));
            }

            System.out.println("✅ Utilisateur ajouté: " + utilisateur.getEmail());
            return utilisateur;
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout utilisateur: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Utilisateur modifier(Utilisateur utilisateur) {
        String req = "UPDATE utilisateur SET email=?, nom=?, prenom=?, solde=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, utilisateur.getEmail());
            ps.setString(2, utilisateur.getNom());
            ps.setString(3, utilisateur.getPrenom());
            ps.setDouble(4, utilisateur.getSolde());
            ps.setInt(5, utilisateur.getId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Utilisateur modifié: " + utilisateur.getEmail());
                return utilisateur;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur modification: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void supprimer(int id) {
        String req = "DELETE FROM utilisateur WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Utilisateur supprimé (ID: " + id + ")");
            } else {
                System.out.println("❌ Utilisateur non trouvé");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression: " + e.getMessage());
        }
    }

    @Override
    public List<Utilisateur> afficherTous() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String req = "SELECT * FROM utilisateur";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setSolde(rs.getDouble("solde"));
                u.setCreatedAt(rs.getTimestamp("created_at") != null ?
                        rs.getTimestamp("created_at").toLocalDateTime() : null);
                u.setUpdatedAt(rs.getTimestamp("updated_at") != null ?
                        rs.getTimestamp("updated_at").toLocalDateTime() : null);
                utilisateurs.add(u);
            }

            System.out.println("📋 Liste des utilisateurs (" + utilisateurs.size() + "):");
            for (Utilisateur u : utilisateurs) {
                System.out.println("   " + u);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur affichage: " + e.getMessage());
        }
        return utilisateurs;
    }

    @Override
    public Utilisateur afficherParId(int id) {
        String req = "SELECT * FROM utilisateur WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setSolde(rs.getDouble("solde"));
                u.setCreatedAt(rs.getTimestamp("created_at") != null ?
                        rs.getTimestamp("created_at").toLocalDateTime() : null);
                u.setUpdatedAt(rs.getTimestamp("updated_at") != null ?
                        rs.getTimestamp("updated_at").toLocalDateTime() : null);
                System.out.println("🔍 Utilisateur trouvé: " + u);
                return u;
            } else {
                System.out.println("❌ Aucun utilisateur avec ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche: " + e.getMessage());
        }
        return null;
    }

    // Méthodes spécifiques
    public Utilisateur login(String email) {
        String req = "SELECT * FROM utilisateur WHERE email=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setSolde(rs.getDouble("solde"));
                System.out.println("🔑 Connexion réussie: " + u.getPrenom() + " " + u.getNom());
                return u;
            } else {
                System.out.println("❌ Email non trouvé: " + email);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur connexion: " + e.getMessage());
        }
        return null;
    }

    public double getSoldeTotalUtilisateur(int utilisateurId) {
        String req = "SELECT SUM(solde_total) as total FROM portefeuille WHERE utilisateur_id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, utilisateurId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur calcul solde: " + e.getMessage());
        }
        return 0;
    }

    public long countUtilisateurs() {
        String req = "SELECT COUNT(*) as total FROM utilisateur";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            if (rs.next()) {
                return rs.getLong("total");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage: " + e.getMessage());
        }
        return 0;
    }
}