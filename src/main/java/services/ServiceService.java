package services;

import models.Service;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceService {
    private Connection conn;

    public ServiceService() {
        conn = MyDataBase.getInstance().getConnection();
    }

    // CREATE - Sans id_produit
    public void ajouter(Service service) throws SQLException {
        String query = "INSERT INTO service (id_service, nom_service, type_service, tarif, frequence, date_debut, date_fin, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, service.getIdService());
            pstmt.setString(2, service.getNomService());
            pstmt.setString(3, service.getTypeService().name());
            pstmt.setBigDecimal(4, service.getTarif());
            pstmt.setString(5, service.getFrequence() != null ? service.getFrequence().name() : null);
            pstmt.setDate(6, service.getDateDebut() != null ? Date.valueOf(service.getDateDebut()) : null);
            pstmt.setDate(7, service.getDateFin() != null ? Date.valueOf(service.getDateFin()) : null);
            pstmt.setString(8, service.getStatut().name());

            pstmt.executeUpdate();
            System.out.println("Service ajouté avec succès: " + service.getNomService());
        }
    }

    // READ - Tous
    public List<Service> recuperer() throws SQLException {
        List<Service> services = new ArrayList<>();
        String query = "SELECT * FROM service ORDER BY id_service DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        }

        return services;
    }

    // READ - Par ID
    public Service getById(int id) throws SQLException {
        String query = "SELECT * FROM service WHERE id_service = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToService(rs);
            }
        }
        return null;
    }

    // UPDATE - Sans id_produit
    public void modifier(Service service) throws SQLException {
        String query = "UPDATE service SET nom_service=?, type_service=?, tarif=?, frequence=?, date_debut=?, date_fin=?, statut=? WHERE id_service=?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, service.getNomService());
            pstmt.setString(2, service.getTypeService().name());
            pstmt.setBigDecimal(3, service.getTarif());
            pstmt.setString(4, service.getFrequence() != null ? service.getFrequence().name() : null);
            pstmt.setDate(5, service.getDateDebut() != null ? Date.valueOf(service.getDateDebut()) : null);
            pstmt.setDate(6, service.getDateFin() != null ? Date.valueOf(service.getDateFin()) : null);
            pstmt.setString(7, service.getStatut().name());
            pstmt.setInt(8, service.getIdService());

            int rows = pstmt.executeUpdate();
            System.out.println(rows + " service(s) modifié(s)");
        }
    }

    // DELETE
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM service WHERE id_service = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            System.out.println(rows + " service(s) supprimé(s)");
        }
    }

    // Mapping ResultSet -> Service (sans id_produit)
    private Service mapResultSetToService(ResultSet rs) throws SQLException {
        Service service = new Service();
        service.setIdService(rs.getInt("id_service"));
        service.setNomService(rs.getString("nom_service"));
        service.setTypeService(Service.TypeService.valueOf(rs.getString("type_service")));
        service.setTarif(rs.getBigDecimal("tarif"));

        String frequence = rs.getString("frequence");
        if (frequence != null && !frequence.isEmpty()) {
            service.setFrequence(Service.Frequence.valueOf(frequence));
        }

        Date dateDebut = rs.getDate("date_debut");
        if (dateDebut != null) {
            service.setDateDebut(dateDebut.toLocalDate());
        }

        Date dateFin = rs.getDate("date_fin");
        if (dateFin != null) {
            service.setDateFin(dateFin.toLocalDate());
        }

        service.setStatut(Service.StatutService.valueOf(rs.getString("statut")));

        return service;
    }
}