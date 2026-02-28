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

    public void ajouter(Service service) throws SQLException {
        String query = "INSERT INTO service (nom_service, type_service, tarif, frequence, date_debut, date_fin, statut) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, service.getNomService());
            pstmt.setString(2, service.getTypeService());
            pstmt.setBigDecimal(3, service.getTarif());
            pstmt.setString(4, service.getFrequence());
            pstmt.setDate(5, service.getDateDebut() != null ? Date.valueOf(service.getDateDebut()) : null);
            pstmt.setDate(6, service.getDateFin() != null ? Date.valueOf(service.getDateFin()) : null);
            pstmt.setString(7, service.getStatut());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                service.setIdService(rs.getInt(1));
            }
        }
    }

    public List<Service> recupererTous() throws SQLException {
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

    public void modifier(Service service) throws SQLException {
        String query = "UPDATE service SET nom_service=?, type_service=?, tarif=?, frequence=?, date_debut=?, date_fin=?, statut=? WHERE id_service=?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, service.getNomService());
            pstmt.setString(2, service.getTypeService());
            pstmt.setBigDecimal(3, service.getTarif());
            pstmt.setString(4, service.getFrequence());
            pstmt.setDate(5, service.getDateDebut() != null ? Date.valueOf(service.getDateDebut()) : null);
            pstmt.setDate(6, service.getDateFin() != null ? Date.valueOf(service.getDateFin()) : null);
            pstmt.setString(7, service.getStatut());
            pstmt.setInt(8, service.getIdService());
            pstmt.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM service WHERE id_service = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private Service mapResultSetToService(ResultSet rs) throws SQLException {
        Service service = new Service();
        service.setIdService(rs.getInt("id_service"));
        service.setNomService(rs.getString("nom_service"));
        service.setTypeService(rs.getString("type_service"));
        service.setTarif(rs.getBigDecimal("tarif"));
        service.setFrequence(rs.getString("frequence"));

        Date dateDebut = rs.getDate("date_debut");
        if (dateDebut != null) service.setDateDebut(dateDebut.toLocalDate());

        Date dateFin = rs.getDate("date_fin");
        if (dateFin != null) service.setDateFin(dateFin.toLocalDate());

        service.setStatut(rs.getString("statut"));
        return service;
    }
}