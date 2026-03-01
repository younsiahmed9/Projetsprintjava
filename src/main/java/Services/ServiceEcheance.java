package Services;

import Models.Document;
import Models.Echeance;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des échéances et notifications
 */
public class ServiceEcheance implements Iservice<Echeance> {

    private final Connection connection;
    private final ServiceDocument documentService;

    public ServiceEcheance() throws SQLException {
        connection = utils.DataSource.getInstance().getCnx();
        documentService = new ServiceDocument();
    }

    @Override
    public void add(Echeance echeance) throws SQLException {
        String query = "INSERT INTO echeance (document_id, date_echeance, type_echeance, description, statut, notifie, date_creation_alerte) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, echeance.getDocument().getId());
            ps.setDate(2, Date.valueOf(echeance.getDateEcheance()));
            ps.setString(3, echeance.getTypeEcheance());
            ps.setString(4, echeance.getDescription());
            ps.setString(5, echeance.getStatut());
            ps.setBoolean(6, echeance.isNotifie());
            ps.setDate(7, Date.valueOf(echeance.getDateCreationAlerte()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    echeance.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Echeance echeance) throws SQLException {
        String query = "UPDATE echeance SET document_id=?, date_echeance=?, type_echeance=?, description=?, statut=?, notifie=?, date_creation_alerte=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, echeance.getDocument().getId());
            ps.setDate(2, Date.valueOf(echeance.getDateEcheance()));
            ps.setString(3, echeance.getTypeEcheance());
            ps.setString(4, echeance.getDescription());
            ps.setString(5, echeance.getStatut());
            ps.setBoolean(6, echeance.isNotifie());
            ps.setDate(7, Date.valueOf(echeance.getDateCreationAlerte()));
            ps.setInt(8, echeance.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM echeance WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Echeance findById(int id) throws SQLException {
        String query = "SELECT * FROM echeance WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEcheance(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Echeance> findAll() throws SQLException {
        List<Echeance> echeances = new ArrayList<>();
        String query = "SELECT * FROM echeance ORDER BY date_echeance ASC";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                echeances.add(mapResultSetToEcheance(rs));
            }
        }
        return echeances;
    }

    /**
     * Trouve toutes les échéances pour un document
     */
    public List<Echeance> findByDocument(int documentId) throws SQLException {
        List<Echeance> echeances = new ArrayList<>();
        String query = "SELECT * FROM echeance WHERE document_id=? ORDER BY date_echeance ASC";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    echeances.add(mapResultSetToEcheance(rs));
                }
            }
        }
        return echeances;
    }

    /**
     * Trouve les échéances à venir (non échues)
     */
    public List<Echeance> findUpcoming() throws SQLException {
        List<Echeance> echeances = new ArrayList<>();
        String query = "SELECT * FROM echeance WHERE date_echeance >= CURDATE() AND statut != 'VUE' ORDER BY date_echeance ASC";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                echeances.add(mapResultSetToEcheance(rs));
            }
        }
        return echeances;
    }

    /**
     * Trouve les échéances échues (passées et non terminées)
     */
    public List<Echeance> findOverdue() throws SQLException {
        List<Echeance> echeances = new ArrayList<>();
        String query = "SELECT * FROM echeance WHERE date_echeance < CURDATE() AND statut != 'VUE' ORDER BY date_echeance ASC";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                echeances.add(mapResultSetToEcheance(rs));
            }
        }
        return echeances;
    }

    /**
     * Trouve les échéances dans les X prochains jours
     */
    public List<Echeance> findInNextDays(int days) throws SQLException {
        List<Echeance> echeances = new ArrayList<>();
        String query = "SELECT * FROM echeance WHERE date_echeance BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY) AND statut != 'VUE' ORDER BY date_echeance ASC";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    echeances.add(mapResultSetToEcheance(rs));
                }
            }
        }
        return echeances;
    }

    /**
     * Trouve les échéances qui nécessitent une notification
     */
    public List<Echeance> findRequiringNotification(int daysBeforeAlert) throws SQLException {
        return findAll().stream()
                .filter(e -> !e.isNotifie())
                .filter(e -> !e.getStatut().equals("VUE"))
                .filter(e -> {
                    long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), e.getDateEcheance());
                    return daysUntil <= daysBeforeAlert && daysUntil >= 0;
                })
                .collect(Collectors.toList());
    }

    /**
     * Marque une échéance comme notifiée
     */
    public void markAsNotified(int echeanceId) throws SQLException {
        String query = "UPDATE echeance SET notifie=true WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, echeanceId);
            ps.executeUpdate();
        }
    }

    /**
     * Marque une échéance comme terminée
     */
    public void markAsCompleted(int echeanceId) throws SQLException {
        String query = "UPDATE echeance SET statut='VUE' WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, echeanceId);
            ps.executeUpdate();
        }
    }

    /**
     * Compte les échéances par statut
     */
    public EcheanceStats getStatistics() throws SQLException {
        int total = 0;
        int upcoming = 0;
        int overdue = 0;
        int completed = 0;

        String query = "SELECT statut, COUNT(*) as count FROM echeance GROUP BY statut";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                String statut = rs.getString("statut");
                int count = rs.getInt("count");
                total += count;

                if (statut.equals("VUE")) {
                    completed = count;
                }
            }
        }

        upcoming = findUpcoming().size();
        overdue = findOverdue().size();

        return new EcheanceStats(total, upcoming, overdue, completed);
    }

    /**
     * Map un ResultSet vers un objet Echeance
     */
    private Echeance mapResultSetToEcheance(ResultSet rs) throws SQLException {
        Echeance echeance = new Echeance();
        echeance.setId(rs.getInt("id"));

        int documentId = rs.getInt("document_id");
        Document document = documentService.findById(documentId);
        echeance.setDocument(document);

        echeance.setDateEcheance(rs.getDate("date_echeance").toLocalDate());
        echeance.setTypeEcheance(rs.getString("type_echeance"));
        echeance.setDescription(rs.getString("description"));
        echeance.setStatut(rs.getString("statut"));
        echeance.setNotifie(rs.getBoolean("notifie"));
        if (rs.getDate("date_creation_alerte") != null) {
            echeance.setDateCreationAlerte(rs.getDate("date_creation_alerte").toLocalDate());
        }

        return echeance;
    }

    /**
     * Classe pour les statistiques d'échéances
     */
    public static class EcheanceStats {
        private final int total;
        private final int upcoming;
        private final int overdue;
        private final int completed;

        public EcheanceStats(int total, int upcoming, int overdue, int completed) {
            this.total = total;
            this.upcoming = upcoming;
            this.overdue = overdue;
            this.completed = completed;
        }

        public int getTotal() {
            return total;
        }

        public int getUpcoming() {
            return upcoming;
        }

        public int getOverdue() {
            return overdue;
        }

        public int getCompleted() {
            return completed;
        }
    }
}
