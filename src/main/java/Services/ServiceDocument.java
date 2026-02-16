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
        String sql = "INSERT INTO document(titre, description, file_path, dossier_id, categorie_id, montant) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getTitre());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getFilePath());
            ps.setInt(4, d.getDossier().getId());
            ps.setInt(5, d.getCategorie().getId());
            ps.setDouble(6, d.getMontant());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Document d) throws SQLException {
        String sql = "UPDATE document SET titre=?, description=?, file_path=?, dossier_id=?, categorie_id=?, montant=? WHERE id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getTitre());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getFilePath());
            ps.setInt(4, d.getDossier().getId());
            ps.setInt(5, d.getCategorie().getId());
            ps.setDouble(6, d.getMontant());
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
        String sql = "SELECT d.id,d.titre,d.description,d.file_path,d.uploaded_at,d.montant, " +
                "ds.id AS dossier_id, ds.nom AS dossier_nom, ds.description AS dossier_desc, ds.created_at AS dossier_created, " +
                "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_desc " +
                "FROM document d JOIN dossier ds ON ds.id=d.dossier_id JOIN categorie c ON c.id=d.categorie_id WHERE d.id=?";
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
        String sql = "SELECT d.id,d.titre,d.description,d.file_path,d.uploaded_at,d.montant, " +
                "ds.id AS dossier_id, ds.nom AS dossier_nom, ds.description AS dossier_desc, ds.created_at AS dossier_created, " +
                "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_desc " +
                "FROM document d JOIN dossier ds ON ds.id=d.dossier_id JOIN categorie c ON c.id=d.categorie_id ORDER BY d.uploaded_at DESC";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Document> findByDossierId(int dossierId) throws SQLException {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT d.id,d.titre,d.description,d.file_path,d.uploaded_at,d.montant, " +
                "ds.id AS dossier_id, ds.nom AS dossier_nom, ds.description AS dossier_desc, ds.created_at AS dossier_created, " +
                "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_desc " +
                "FROM document d JOIN dossier ds ON ds.id=d.dossier_id JOIN categorie c ON c.id=d.categorie_id WHERE ds.id=? ORDER BY d.uploaded_at DESC";
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

        Categorie categorie = new Categorie(rs.getInt("categorie_id"), rs.getString("categorie_nom"),
                rs.getString("categorie_desc"));
        doc.setCategorie(categorie);

        doc.setMontant(rs.getDouble("montant"));
        return doc;
    }

    public int countDocuments() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM document";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    public int countDocumentsThisMonth() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM document WHERE MONTH(uploaded_at) = MONTH(CURRENT_DATE()) AND YEAR(uploaded_at) = YEAR(CURRENT_DATE())";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    public List<Document> getRecentDocuments(int limit) throws SQLException {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT d.id,d.titre,d.description,d.file_path,d.uploaded_at,d.montant, " +
                "ds.id AS dossier_id, ds.nom AS dossier_nom, ds.description AS dossier_desc, ds.created_at AS dossier_created, " +
                "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_desc " +
                "FROM document d JOIN dossier ds ON ds.id=d.dossier_id JOIN categorie c ON c.id=d.categorie_id " +
                "ORDER BY d.uploaded_at DESC LIMIT ?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public java.util.Map<String, Integer> getStatsDocsByCategorie() throws SQLException {
        java.util.Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        String sql = "SELECT COALESCE(c.nom, 'Non catégorisé') AS categorie_nom, COUNT(d.id) AS total " +
                "FROM document d LEFT JOIN categorie c ON d.categorie_id = c.id " +
                "GROUP BY c.nom ORDER BY total DESC";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("categorie_nom"), rs.getInt("total"));
            }
        }
        return stats;
    }

    public java.util.Map<String, Integer> getStatsDocsByDossier() throws SQLException {
        java.util.Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        String sql = "SELECT ds.nom AS dossier_nom, COUNT(d.id) AS total " +
                "FROM document d JOIN dossier ds ON d.dossier_id = ds.id " +
                "GROUP BY ds.nom ORDER BY total DESC";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("dossier_nom"), rs.getInt("total"));
            }
        }
        return stats;
    }

    public double getMontantTotal() throws SQLException {
        String sql = "SELECT COALESCE(SUM(montant), 0) AS total FROM document";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("total") : 0;
        }
    }

    public double getMontantAverage() throws SQLException {
        String sql = "SELECT COALESCE(AVG(montant), 0) AS avg_montant FROM document";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("avg_montant") : 0;
        }
    }

    public double getMontantMax() throws SQLException {
        String sql = "SELECT COALESCE(MAX(montant), 0) AS max_montant FROM document";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("max_montant") : 0;
        }
    }

    public double getMontantMin() throws SQLException {
        String sql = "SELECT COALESCE(MIN(montant), 0) AS min_montant FROM document";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("min_montant") : 0;
        }
    }

    public java.util.Map<String, Double> getMontantByCategorie() throws SQLException {
        java.util.Map<String, Double> stats = new java.util.LinkedHashMap<>();
        String sql = "SELECT c.nom AS categorie_nom, COALESCE(SUM(d.montant),0) AS total_montant " +
                "FROM document d JOIN categorie c ON d.categorie_id = c.id " +
                "GROUP BY c.nom ORDER BY total_montant DESC";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("categorie_nom"), rs.getDouble("total_montant"));
            }
        }
        return stats;
    }

    public java.util.Map<String, Double> getMontantByDossier() throws SQLException {
        java.util.Map<String, Double> stats = new java.util.LinkedHashMap<>();
        String sql = "SELECT ds.nom AS dossier_nom, COALESCE(SUM(d.montant),0) AS total_montant " +
                "FROM document d JOIN dossier ds ON d.dossier_id = ds.id " +
                "GROUP BY ds.nom ORDER BY total_montant DESC";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("dossier_nom"), rs.getDouble("total_montant"));
            }
        }
        return stats;
    }

    public List<Document> getTopDocumentsByMontant(int limit) throws SQLException {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT d.id,d.titre,d.description,d.file_path,d.uploaded_at,d.montant, " +
                "ds.id AS dossier_id, ds.nom AS dossier_nom, ds.description AS dossier_desc, ds.created_at AS dossier_created, " +
                "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_desc " +
                "FROM document d JOIN dossier ds ON ds.id=d.dossier_id JOIN categorie c ON c.id=d.categorie_id " +
                "ORDER BY d.montant DESC LIMIT ?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }
}
