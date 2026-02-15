package Services;

import Models.Categorie;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCategorie implements Iservice<Categorie> {

    @Override
    public void add(Categorie categorie) throws SQLException {
        String sql = "INSERT INTO categorie(nom, description) VALUES(?,?)";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, categorie.getNom());
            ps.setString(2, categorie.getDescription());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Categorie categorie) throws SQLException {
        String sql = "UPDATE categorie SET nom=?, description=? WHERE id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, categorie.getNom());
            ps.setString(2, categorie.getDescription());
            ps.setInt(3, categorie.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM categorie WHERE id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Categorie findById(int id) throws SQLException {
        String sql = "SELECT id, nom, description FROM categorie WHERE id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<Categorie> findAll() throws SQLException {
        List<Categorie> list = new ArrayList<>();
        String sql = "SELECT id, nom, description FROM categorie ORDER BY nom";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean existsByNom(String nom) throws SQLException {
        String sql = "SELECT 1 FROM categorie WHERE nom=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, nom);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Categorie mapRow(ResultSet rs) throws SQLException {
        return new Categorie(rs.getInt("id"), rs.getString("nom"), rs.getString("description"));
    }
}

