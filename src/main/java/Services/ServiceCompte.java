package Services;

import Models.Compte;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCompte implements Iservice<Compte> {

    private Connection connection;

    public ServiceCompte() {
        connection = MyDatabase.getInstance().getConn();
    }

    @Override
    public void ajouter(Compte compte) throws SQLDataException {

        String sql = "INSERT INTO compte (numero_compte, solde, type_compte, taux_interet, " +
                "plafond_decouvert, date_creation, etat) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, compte.getNumeroCompte());
            ps.setDouble(2, compte.getSolde());
            ps.setString(3, compte.getTypeCompte());

            if ("EPARGNE".equals(compte.getTypeCompte())) {
                ps.setDouble(4, compte.getTauxInteret());
                ps.setNull(5, Types.DECIMAL);
            } else { // COURANT
                ps.setNull(4, Types.DECIMAL);
                ps.setDouble(5, compte.getPlafondDecouvert());
            }

            ps.setDate(6, Date.valueOf(compte.getDateCreation()));
            ps.setString(7, compte.getEtat());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(Compte compte) throws SQLDataException {

        String sql = "DELETE FROM compte WHERE id_compte = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, compte.getIdCompte());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Compte compte) throws SQLDataException {

        String sql = "UPDATE compte SET numero_compte = ?, solde = ?, type_compte = ?, " +
                "taux_interet = ?, plafond_decouvert = ?, date_creation = ?, etat = ? " +
                "WHERE id_compte = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, compte.getNumeroCompte());
            ps.setDouble(2, compte.getSolde());
            ps.setString(3, compte.getTypeCompte());

            if ("EPARGNE".equals(compte.getTypeCompte())) {
                ps.setDouble(4, compte.getTauxInteret());
                ps.setNull(5, Types.DECIMAL);
            } else { // COURANT
                ps.setNull(4, Types.DECIMAL);
                ps.setDouble(5, compte.getPlafondDecouvert());
            }

            ps.setDate(6, Date.valueOf(compte.getDateCreation()));
            ps.setString(7, compte.getEtat());
            ps.setInt(8, compte.getIdCompte());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Compte> recuperer() throws SQLDataException {

        String sql = "SELECT * FROM compte";
        List<Compte> compteList = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {

                Compte c = new Compte();

                c.setIdCompte(rs.getInt("id_compte"));
                c.setNumeroCompte(rs.getString("numero_compte"));
                c.setSolde(rs.getDouble("solde"));
                c.setTypeCompte(rs.getString("type_compte"));

                if ("EPARGNE".equals(rs.getString("type_compte"))) {
                    c.setTauxInteret(rs.getDouble("taux_interet"));
                    c.setPlafondDecouvert(null);
                } else {
                    c.setPlafondDecouvert(rs.getDouble("plafond_decouvert"));
                    c.setTauxInteret(null);
                }

                c.setDateCreation(rs.getDate("date_creation").toLocalDate());
                c.setEtat(rs.getString("etat"));

                compteList.add(c);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return compteList;
    }
}
