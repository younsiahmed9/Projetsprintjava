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
    public Compte recupererParId(int id) throws SQLDataException {
        String sql = "SELECT * FROM compte WHERE id_compte = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Compte c = new Compte();
                c.setIdCompte(rs.getInt("id_compte"));
                c.setNumeroCompte(rs.getString("numero_compte"));
                c.setSolde(rs.getDouble("solde"));
                c.setTypeCompte(rs.getString("type_compte"));
                c.setTauxInteret(rs.getObject("taux_interet") != null ? rs.getDouble("taux_interet") : null);
                c.setPlafondDecouvert(rs.getObject("plafond_decouvert") != null ? rs.getDouble("plafond_decouvert") : null);
                c.setDateCreation(rs.getDate("date_creation").toLocalDate());
                c.setEtat(rs.getString("etat"));
                return c;
            } else {
                System.out.println("Aucun compte trouvé pour l'ID : " + id);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL recupererParId: " + e.getMessage());
        }
        return null;
    }
    public String getEtatParId(int idCompte) {
        String sql = "SELECT etat FROM compte WHERE id_compte = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idCompte);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("etat");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Compte getCompteById(int id) {
        Compte compte = null;
        String sql = "SELECT * FROM compte WHERE id_compte = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                compte = new Compte();
                compte.setIdCompte(rs.getInt("id_compte"));
                compte.setNumeroCompte(rs.getString("numero_compte"));
                compte.setSolde(rs.getDouble("solde"));
                compte.setEtat(rs.getString("etat"));
                compte.setTypeCompte(rs.getString("type_compte"));
                // Ajoute d'autres setters selon tes colonnes (date_creation, etc.)
            }
        } catch (SQLException e) {
            System.out.println("Erreur getCompteById : " + e.getMessage());
        }
        return compte;
    }
    // Dans ServiceCompte.java
    public double calculerScoreSolvabilite(Compte c) {
        // Algorithme simple : Score basé sur le solde et l'état
        double scoreBase = (c.getSolde() > 5000) ? 90.0 : 70.0;
        if (c.getEtat().equalsIgnoreCase("INACTIF")) {
            scoreBase -= 30.0;
        }
        return Math.max(0, Math.min(100, scoreBase)); // Toujours entre 0 et 100
    }
}
