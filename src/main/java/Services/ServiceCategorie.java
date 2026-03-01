package Services;

import Models.Categorie;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCategorie implements Iservice<Categorie> {

    @Override
    public void add(Categorie categorie) throws SQLException {
        new ServiceDocument().ensureMigration();
        String sql = "INSERT INTO categorie(nom, description, budget_max) VALUES(?,?,?)";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, categorie.getNom());
            ps.setString(2, categorie.getDescription());
            ps.setDouble(3, categorie.getBudgetMax());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Categorie categorie) throws SQLException {
        new ServiceDocument().ensureMigration();
        String sql = "UPDATE categorie SET nom=?, description=?, budget_max=? WHERE id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, categorie.getNom());
            ps.setString(2, categorie.getDescription());
            ps.setDouble(3, categorie.getBudgetMax());
            ps.setInt(4, categorie.getId());
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
        new ServiceDocument().ensureMigration();
        String sql = "SELECT id, nom, description, budget_max FROM categorie WHERE id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<Categorie> findAll() throws SQLException {
        new ServiceDocument().ensureMigration();
        List<Categorie> list = new ArrayList<>();
        String sql = "SELECT id, nom, description, budget_max FROM categorie ORDER BY nom";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(mapRow(rs));
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

    public int countCategories() throws SQLException {
        new ServiceDocument().ensureMigration();
        String sql = "SELECT COUNT(*) AS total FROM categorie";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    private Categorie mapRow(ResultSet rs) throws SQLException {
        return new Categorie(rs.getInt("id"), rs.getString("nom"), rs.getString("description"),
                rs.getDouble("budget_max"));
    }
}
