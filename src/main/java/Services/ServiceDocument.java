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
        ensureMigration();
        String sql = "INSERT INTO document(titre, description, file_path, dossier_id, categorie_id, montant, date_facture, date_limite_paiement, status, currency, original_amount) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getTitre());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getFilePath());
            ps.setInt(4, d.getDossier().getId());
            ps.setInt(5, d.getCategorie().getId());
            ps.setDouble(6, d.getMontant());
            ps.setDate(7, d.getDateFacture() != null ? java.sql.Date.valueOf(d.getDateFacture()) : null);
            ps.setDate(8, d.getDateLimitePaiement() != null ? java.sql.Date.valueOf(d.getDateLimitePaiement()) : null);
            ps.setString(9, d.getStatus());
            ps.setString(10, d.getCurrency());
            ps.setDouble(11, d.getOriginalAmount());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Document d) throws SQLException {
        ensureMigration();
        String sql = "UPDATE document SET titre=?, description=?, file_path=?, dossier_id=?, categorie_id=?, montant=?, date_facture=?, date_limite_paiement=?, status=?, currency=?, original_amount=? WHERE id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getTitre());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getFilePath());
            ps.setInt(4, d.getDossier().getId());
            ps.setInt(5, d.getCategorie().getId());
            ps.setDouble(6, d.getMontant());
            ps.setDate(7, d.getDateFacture() != null ? java.sql.Date.valueOf(d.getDateFacture()) : null);
            ps.setDate(8, d.getDateLimitePaiement() != null ? java.sql.Date.valueOf(d.getDateLimitePaiement()) : null);
            ps.setString(9, d.getStatus());
            ps.setString(10, d.getCurrency());
            ps.setDouble(11, d.getOriginalAmount());
            ps.setInt(12, d.getId());
            ps.executeUpdate();
        }
    }

    private static boolean colsChecked = false;

    public void ensureMigration() {
        if (colsChecked)
            return;
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement()) {
            st.execute("ALTER TABLE document ADD COLUMN status VARCHAR(20) DEFAULT 'DRAFT';");
        } catch (Exception e) {
        }
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement()) {
            st.execute("ALTER TABLE categorie ADD COLUMN budget_max DOUBLE DEFAULT 0;");
        } catch (Exception e) {
        }

        try (Statement st = MyDatabase.getInstance().getConnection().createStatement()) {
            st.execute("ALTER TABLE document ADD COLUMN date_facture DATE;");
        } catch (Exception e) {
        }
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement()) {
            st.execute("ALTER TABLE document ADD COLUMN date_limite_paiement DATE;");
        } catch (Exception e) {
        }
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement()) {
            st.execute("ALTER TABLE document ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;");
        } catch (Exception e) {
        }
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement()) {
            st.execute("ALTER TABLE document ADD COLUMN deleted_at TIMESTAMP NULL;");
        } catch (Exception e) {
        }
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement()) {
            st.execute("ALTER TABLE document ADD COLUMN currency VARCHAR(10) DEFAULT 'TND';");
        } catch (Exception e) {
        }
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement()) {
            st.execute("ALTER TABLE document ADD COLUMN original_amount DOUBLE DEFAULT 0;");
        } catch (Exception e) {
        }
        colsChecked = true;
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "UPDATE document SET is_deleted = 1, deleted_at = CURRENT_TIMESTAMP WHERE id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void restore(int id) throws SQLException {
        String sql = "UPDATE document SET is_deleted = 0, deleted_at = NULL WHERE id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void hardDelete(int id) throws SQLException {
        String sql = "DELETE FROM document WHERE id=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void purgeOldDeletedDocuments() throws SQLException {
        String sql = "DELETE FROM document WHERE is_deleted = 1 AND deleted_at < DATE_SUB(NOW(), INTERVAL 30 DAY)";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement()) {
            st.executeUpdate(sql);
        }
    }

    @Override
    public Document findById(int id) throws SQLException {
        String sql = "SELECT d.id,d.titre,d.description,d.file_path,d.uploaded_at,d.montant,d.date_facture,d.date_limite_paiement,d.status,d.is_deleted,d.deleted_at,d.currency,d.original_amount, "
                + "ds.id AS dossier_id, ds.nom AS dossier_nom, ds.description AS dossier_desc, ds.created_at AS dossier_created, "
                + "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_desc, c.budget_max AS categorie_budget "
                + "FROM document d JOIN dossier ds ON ds.id=d.dossier_id JOIN categorie c ON c.id=d.categorie_id WHERE d.id=?";
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
    public List<Document> findAll() throws SQLException {
        ensureMigration();
        List<Document> list = new ArrayList<>();
        String sql = "SELECT d.id,d.titre,d.description,d.file_path,d.uploaded_at,d.montant,d.date_facture,d.date_limite_paiement,d.status,d.is_deleted,d.deleted_at,d.currency,d.original_amount, "
                + "ds.id AS dossier_id, ds.nom AS dossier_nom, ds.description AS dossier_desc, ds.created_at AS dossier_created, "
                + "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_desc, c.budget_max AS categorie_budget "
                + "FROM document d JOIN dossier ds ON ds.id=d.dossier_id JOIN categorie c ON c.id=d.categorie_id WHERE d.is_deleted = 0 ORDER BY d.uploaded_at DESC";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(mapRow(rs));
        }
        return list;
    }

    public List<Document> findByDossierId(int dossierId) throws SQLException {
        ensureMigration();
        List<Document> list = new ArrayList<>();
        String sql = "SELECT d.id,d.titre,d.description,d.file_path,d.uploaded_at,d.montant,d.date_facture,d.date_limite_paiement,d.status,d.is_deleted,d.deleted_at,d.currency,d.original_amount, "
                + "ds.id AS dossier_id, ds.nom AS dossier_nom, ds.description AS dossier_desc, ds.created_at AS dossier_created, "
                + "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_desc, c.budget_max AS categorie_budget "
                + "FROM document d JOIN dossier ds ON ds.id=d.dossier_id JOIN categorie c ON c.id=d.categorie_id WHERE ds.id=? AND d.is_deleted = 0 ORDER BY d.uploaded_at DESC";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, dossierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
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
                rs.getString("categorie_desc"), rs.getDouble("categorie_budget"));
        doc.setCategorie(categorie);
        doc.setStatus(rs.getString("status"));

        doc.setMontant(rs.getDouble("montant"));

        doc.setDeleted(rs.getBoolean("is_deleted"));
        Timestamp delAt = rs.getTimestamp("deleted_at");
        if (delAt != null)
            doc.setDeletedAt(delAt.toLocalDateTime());

        doc.setCurrency(rs.getString("currency"));
        doc.setOriginalAmount(rs.getDouble("original_amount"));

        java.sql.Date dFacture = rs.getDate("date_facture");
        if (dFacture != null)
            doc.setDateFacture(dFacture.toLocalDate());

        java.sql.Date dLimite = rs.getDate("date_limite_paiement");
        if (dLimite != null)
            doc.setDateLimitePaiement(dLimite.toLocalDate());

        return doc;
    }

    public int countDocuments() throws SQLException {
        ensureMigration();
        String sql = "SELECT COUNT(*) AS total FROM document WHERE is_deleted = 0";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    public int countDocumentsThisMonth() throws SQLException {
        ensureMigration();
        String sql = "SELECT COUNT(*) AS total FROM document WHERE is_deleted = 0 AND MONTH(uploaded_at) = MONTH(CURRENT_DATE()) AND YEAR(uploaded_at) = YEAR(CURRENT_DATE())";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    public List<Document> getRecentDocuments(int limit) throws SQLException {
        ensureMigration();
        List<Document> list = new ArrayList<>();
        String sql = "SELECT d.id,d.titre,d.description,d.file_path,d.uploaded_at,d.montant,d.date_facture,d.date_limite_paiement,d.status,d.is_deleted,d.deleted_at,d.currency,d.original_amount, "
                + "ds.id AS dossier_id, ds.nom AS dossier_nom, ds.description AS dossier_desc, ds.created_at AS dossier_created, "
                + "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_desc, c.budget_max AS categorie_budget "
                + "FROM document d JOIN dossier ds ON ds.id=d.dossier_id JOIN categorie c ON c.id=d.categorie_id "
                + "WHERE d.is_deleted = 0 ORDER BY d.uploaded_at DESC LIMIT ?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        }
        return list;
    }

    public java.util.Map<String, Integer> getStatsDocsByCategorie() throws SQLException {
        ensureMigration();
        java.util.Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        String sql = "SELECT COALESCE(c.nom, 'Non catégorisé') AS categorie_nom, COUNT(d.id) AS total " +
                "FROM document d LEFT JOIN categorie c ON d.categorie_id = c.id " +
                "WHERE d.is_deleted = 0 " +
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
        ensureMigration();
        java.util.Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        String sql = "SELECT ds.nom AS dossier_nom, COUNT(d.id) AS total " +
                "FROM document d JOIN dossier ds ON d.dossier_id = ds.id " +
                "WHERE d.is_deleted = 0 " +
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
        ensureMigration();
        String sql = "SELECT COALESCE(SUM(montant), 0) AS total FROM document WHERE is_deleted = 0";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("total") : 0;
        }
    }

    public double getMontantAverage() throws SQLException {
        ensureMigration();
        String sql = "SELECT COALESCE(AVG(montant), 0) AS avg_montant FROM document WHERE is_deleted = 0";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("avg_montant") : 0;
        }
    }

    public double getMontantMax() throws SQLException {
        ensureMigration();
        String sql = "SELECT COALESCE(MAX(montant), 0) AS max_montant FROM document WHERE is_deleted = 0";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("max_montant") : 0;
        }
    }

    public double getMontantMin() throws SQLException {
        ensureMigration();
        String sql = "SELECT COALESCE(MIN(montant), 0) AS min_montant FROM document WHERE is_deleted = 0";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("min_montant") : 0;
        }
    }

    public java.util.Map<String, Double> getMontantByCategorie() throws SQLException {
        ensureMigration();
        java.util.Map<String, Double> stats = new java.util.LinkedHashMap<>();
        String sql = "SELECT c.nom AS categorie_nom, COALESCE(SUM(d.montant),0) AS total_montant " +
                "FROM document d JOIN categorie c ON d.categorie_id = c.id " +
                "WHERE d.is_deleted = 0 " +
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
        ensureMigration();
        java.util.Map<String, Double> stats = new java.util.LinkedHashMap<>();
        String sql = "SELECT ds.nom AS dossier_nom, COALESCE(SUM(d.montant),0) AS total_montant " +
                "FROM document d JOIN dossier ds ON d.dossier_id = ds.id " +
                "WHERE d.is_deleted = 0 " +
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
        ensureMigration();
        List<Document> list = new ArrayList<>();
        String sql = "SELECT d.id,d.titre,d.description,d.file_path,d.uploaded_at,d.montant,d.date_facture,d.date_limite_paiement,d.status,d.is_deleted,d.deleted_at,d.currency,d.original_amount, "
                + "ds.id AS dossier_id, ds.nom AS dossier_nom, ds.description AS dossier_desc, ds.created_at AS dossier_created, "
                + "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_desc, c.budget_max AS categorie_budget "
                + "FROM document d JOIN dossier ds ON ds.id=d.dossier_id JOIN categorie c ON c.id=d.categorie_id "
                + "WHERE d.is_deleted = 0 ORDER BY d.montant DESC LIMIT ?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean existsByFilePath(String filePath) throws SQLException {
        String sql = "SELECT 1 FROM document WHERE file_path=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, filePath);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existsByFilePathExceptId(String filePath, int id) throws SQLException {
        String sql = "SELECT 1 FROM document WHERE file_path=? AND id!=?";
        try (PreparedStatement ps = MyDatabase.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, filePath);
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Document> findAllDeleted() throws SQLException {
        ensureMigration();
        List<Document> list = new ArrayList<>();
        String sql = "SELECT d.id,d.titre,d.description,d.file_path,d.uploaded_at,d.montant,d.date_facture,d.date_limite_paiement,d.status,d.is_deleted,d.deleted_at,d.currency,d.original_amount, "
                + "ds.id AS dossier_id, ds.nom AS dossier_nom, ds.description AS dossier_desc, ds.created_at AS dossier_created, "
                + "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_desc, c.budget_max AS categorie_budget "
                + "FROM document d JOIN dossier ds ON ds.id=d.dossier_id JOIN categorie c ON c.id=d.categorie_id "
                + "WHERE d.is_deleted = 1 ORDER BY d.deleted_at DESC";
        try (Statement st = MyDatabase.getInstance().getConnection().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(mapRow(rs));
        }
        return list;
    }
}
