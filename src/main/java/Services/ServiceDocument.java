package Services;

import Models.Categorie;
import Models.Dossier;
import Models.Document;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDocument implements Iservice<Document> {

    @Override
    public void add(Document d) throws SQLException {
        String sql = "INSERT INTO document(titre, description, file_path, dossier_id, categorie_id, budget) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getTitre());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getFilePath());
            ps.setInt(4, d.getDossier().getId());
            if (d.getCategorie() != null) {
                ps.setInt(5, d.getCategorie().getId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            if (d.getBudget() != null) {
                ps.setDouble(6, d.getBudget());
            } else {
                ps.setNull(6, java.sql.Types.DOUBLE);
            }
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Document d) throws SQLException {
        String sql = "UPDATE document SET titre=?, description=?, file_path=?, dossier_id=?, categorie_id=?, budget=? WHERE id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getTitre());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getFilePath());
            ps.setInt(4, d.getDossier().getId());
            if (d.getCategorie() != null) {
                ps.setInt(5, d.getCategorie().getId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            if (d.getBudget() != null) {
                ps.setDouble(6, d.getBudget());
            } else {
                ps.setNull(6, java.sql.Types.DOUBLE);
            }
            ps.setInt(7, d.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM document WHERE id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Document findById(int id) throws SQLException {
        String sql = "SELECT d.id,d.titre,d.description,d.file_path,d.uploaded_at,d.budget, " +
                "ds.id AS dossier_id, ds.nom AS dossier_nom, ds.description AS dossier_desc, ds.created_at AS dossier_created, " +
                "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_desc " +
                "FROM document d JOIN dossier ds ON ds.id=d.dossier_id LEFT JOIN categorie c ON c.id=d.categorie_id WHERE d.id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<Document> findAll() throws SQLException {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT d.id,d.titre,d.description,d.file_path,d.uploaded_at,d.budget, " +
                "ds.id AS dossier_id, ds.nom AS dossier_nom, ds.description AS dossier_desc, ds.created_at AS dossier_created, " +
                "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_desc " +
                "FROM document d JOIN dossier ds ON ds.id=d.dossier_id LEFT JOIN categorie c ON c.id=d.categorie_id ORDER BY d.uploaded_at DESC";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Document> findByDossierId(int dossierId) throws SQLException {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT d.id,d.titre,d.description,d.file_path,d.uploaded_at, " +
                "ds.id AS dossier_id, ds.nom AS dossier_nom, ds.description AS dossier_desc, ds.created_at AS dossier_created, " +
                "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_desc " +
                "FROM document d JOIN dossier ds ON ds.id=d.dossier_id LEFT JOIN categorie c ON c.id=d.categorie_id WHERE ds.id=? ORDER BY d.uploaded_at DESC";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, dossierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Document mapRow(ResultSet rs) throws SQLException {
        Timestamp dsCreated = rs.getTimestamp("dossier_created");
        Dossier ds = new Dossier(rs.getInt("dossier_id"), rs.getString("dossier_nom"), rs.getString("dossier_desc"),
                dsCreated != null ? dsCreated.toLocalDateTime() : null);
        Timestamp up = rs.getTimestamp("uploaded_at");
        Document doc = new Document(rs.getInt("id"), rs.getString("titre"), rs.getString("description"),
                rs.getString("file_path"), up != null ? up.toLocalDateTime() : null, ds);

        // Gérer la catégorie (peut être null)
        if (rs.getObject("categorie_id") != null) {
            Categorie categorie = new Categorie(rs.getInt("categorie_id"), rs.getString("categorie_nom"),
                    rs.getString("categorie_desc"));
            doc.setCategorie(categorie);
        }

        // Gérer le budget (peut être null)
        if (rs.getObject("budget") != null) {
            doc.setBudget(rs.getDouble("budget"));
        }

        return doc;
    }
}

