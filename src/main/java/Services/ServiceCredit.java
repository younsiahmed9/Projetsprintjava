package Services;

import Models.Credit;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCredit implements Iservice<Credit> {
    private Connection connection;

    public ServiceCredit() {
        connection = MyDatabase.getInstance().getConn();
    }

    @Override
    public void ajouter(Credit credit) throws SQLDataException {
        String sql = "INSERT INTO credit (montant, taux_interet, duree_mois, mensualite, date_debut, statut, id_compte) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) { // Try-with-resources pour fermer le PS
            ps.setDouble(1, credit.getMontant());
            ps.setDouble(2, credit.getTauxInteret());
            ps.setInt(3, credit.getDureeMois());
            ps.setDouble(4, credit.getMensualite());
            ps.setDate(5, Date.valueOf(credit.getDateDebut()));
            ps.setString(6, credit.getStatut());
            ps.setInt(7, credit.getCompte());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("⚠️ Aucune ligne ajoutée, vérifiez vos contraintes DB.");
            }
        } catch (SQLException e) {
            // TRÈS IMPORTANT : Afficher l'erreur complète pour savoir si c'est un nom de colonne faux
            System.err.println("❌ Erreur SQL détaillée : " + e.getSQLState() + " - " + e.getMessage());
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void supprimer(Credit credit) throws SQLDataException {
        String sql = "DELETE FROM credit WHERE id_credit = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, credit.getIdCredit());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur suppression credit : " + e.getMessage());
        }
    }

    @Override
    public void modifier(Credit credit) throws SQLDataException {
        String sql = "UPDATE credit SET montant = ?, taux_interet = ?, duree_mois = ?, mensualite = ?, date_debut = ?, statut = ?, id_compte = ? WHERE id_credit = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setDouble(1, credit.getMontant());
            ps.setDouble(2, credit.getTauxInteret());
            ps.setInt(3, credit.getDureeMois());
            ps.setDouble(4, credit.getMensualite());
            ps.setDate(5, Date.valueOf(credit.getDateDebut()));
            ps.setString(6, credit.getStatut());
            ps.setInt(7, credit.getCompte());
            ps.setInt(8, credit.getIdCredit());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur modification credit : " + e.getMessage());
        }
    }

    @Override
    public List<Credit> recuperer() throws SQLDataException {
        String sql = "SELECT * FROM credit";
        List<Credit> creditList = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                creditList.add(mapResultSetToCredit(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur récupération credit : " + e.getMessage());
        }
        return creditList;
    }

    public List<Credit> recupererParCompte(int idCompte) {
        String sql = "SELECT * FROM credit WHERE id_compte = ?";
        List<Credit> creditList = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, idCompte);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                creditList.add(mapResultSetToCredit(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur récupération crédits par compte : " + e.getMessage());
        }
        return creditList;
    }

    // Méthode utilitaire interne pour éviter la répétition de code
    private Credit mapResultSetToCredit(ResultSet rs) throws SQLException {
        Credit c = new Credit();
        c.setIdCredit(rs.getInt("id_credit"));
        c.setMontant(rs.getDouble("montant"));
        c.setTauxInteret(rs.getDouble("taux_interet"));
        c.setDureeMois(rs.getInt("duree_mois"));
        c.setMensualite(rs.getDouble("mensualite"));
        c.setDateDebut(rs.getDate("date_debut").toLocalDate());
        c.setStatut(rs.getString("statut"));
        c.setCompte(rs.getInt("id_compte")); // On assigne directement l'int
        return c;
    }
}