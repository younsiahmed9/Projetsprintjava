package Services;

import Models.Credit;
import Models.Compte;
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
        String sql = "INSERT INTO credit (montant, taux_interet, duree_mois, mensualite, date_debut, statut, id_compte) VALUES ("
                + credit.getMontant() + ", "
                + credit.getTauxInteret() + ", "
                + credit.getDureeMois() + ", "
                + credit.getMensualite() + ", '"
                + Date.valueOf(credit.getDateDebut()) + "', '"
                + credit.getStatut() + "', "
                + credit.getCompte().getIdCompte() + ")";
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("Erreur ajout credit : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(Credit credit) throws SQLDataException {
        String sql = "DELETE FROM credit WHERE id_credit = " + credit.getIdCredit();
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
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
            ps.setInt(7, credit.getCompte().getIdCompte());
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
                Credit c = new Credit();
                c.setIdCredit(rs.getInt("id_credit"));
                c.setMontant(rs.getDouble("montant"));
                c.setTauxInteret(rs.getDouble("taux_interet"));
                c.setDureeMois(rs.getInt("duree_mois"));
                c.setMensualite(rs.getDouble("mensualite"));
                c.setDateDebut(rs.getDate("date_debut").toLocalDate());
                c.setStatut(rs.getString("statut"));

                Compte compte = new Compte();
                compte.setIdCompte(rs.getInt("id_compte"));
                c.setCompte(compte);

                creditList.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Erreur récupération credit : " + e.getMessage());
        }
        return creditList;
    }
}
