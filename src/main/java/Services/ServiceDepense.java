package Services;

import Models.Depense;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDepense {
    private Connection cnx;

    public ServiceDepense() {
        try {
            cnx = DriverManager.getConnection("jdbc:mysql://localhost:3306/fintrack", "root", "");
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de se connecter à la base", e);
        }
    }

    // --- CREATE ---
    public void ajouter(Depense d) throws SQLDataException {
        String req = "INSERT INTO depense (id_utilisateur, id_budget, categorie, montant, date_depense, description, mode_paiement) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, d.getIdUtilisateur());
            ps.setInt(2, d.getIdBudget());
            ps.setString(3, d.getCategorie());
            ps.setDouble(4, d.getMontant());
            ps.setDate(5, new java.sql.Date(d.getDateDepense().getTime()));
            ps.setString(6, d.getDescription());
            ps.setString(7, d.getModePaiement());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    // --- READ ---
    public List<Depense> recuperer() throws SQLDataException {
        List<Depense> depenses = new ArrayList<>();
        String req = "SELECT * FROM depense";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Depense d = new Depense(
                        rs.getInt("id_depense"),
                        rs.getInt("id_utilisateur"),
                        rs.getInt("id_budget"),
                        rs.getString("categorie"),
                        rs.getDouble("montant"),
                        rs.getDate("date_depense"),
                        rs.getString("description"),
                        rs.getString("mode_paiement")
                );
                depenses.add(d);
            }
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
        return depenses;
    }

    // --- UPDATE ---
    public void modifier(Depense d) throws SQLDataException {
        String req = "UPDATE depense SET id_utilisateur=?, id_budget=?, categorie=?, montant=?, date_depense=?, description=?, mode_paiement=? WHERE id_depense=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, d.getIdUtilisateur());
            ps.setInt(2, d.getIdBudget());
            ps.setString(3, d.getCategorie());
            ps.setDouble(4, d.getMontant());
            ps.setDate(5, new java.sql.Date(d.getDateDepense().getTime()));
            ps.setString(6, d.getDescription());
            ps.setString(7, d.getModePaiement());
            ps.setInt(8, d.getIdDepense());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    // --- DELETE ---
    public void supprimer(int idDepense) throws SQLDataException {
        String req = "DELETE FROM depense WHERE id_depense=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idDepense);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }
}
