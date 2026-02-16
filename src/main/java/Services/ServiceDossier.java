package Services;

import Models.Dossier;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDossier implements Iservice<Dossier> {

    @Override
    public void add(Dossier d) throws SQLException {
        String sql = "INSERT INTO dossier(nom, description) VALUES(?,?)";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getNom());
            ps.setString(2, d.getDescription());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Dossier d) throws SQLException {
        String sql = "UPDATE dossier SET nom=?, description=? WHERE id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getNom());
            ps.setString(2, d.getDescription());
            ps.setInt(3, d.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM dossier WHERE id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Dossier findById(int id) throws SQLException {
        String sql = "SELECT id, nom, description, created_at FROM dossier WHERE id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    return new Dossier(rs.getInt("id"), rs.getString("nom"), rs.getString("description"),
                            ts != null ? ts.toLocalDateTime() : null);
                }
            }
        }
        return null;
    }

    @Override
    public List<Dossier> findAll() throws SQLException {
        List<Dossier> list = new ArrayList<>();
        String sql = "SELECT id, nom, description, created_at FROM dossier ORDER BY created_at DESC";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("created_at");
                list.add(new Dossier(rs.getInt("id"), rs.getString("nom"), rs.getString("description"),
                        ts != null ? ts.toLocalDateTime() : null));
            }
        }
        return list;
    }

    public int countDossiers() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM dossier";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }
}

