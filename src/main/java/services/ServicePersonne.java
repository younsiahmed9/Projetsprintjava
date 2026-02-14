package services;
import utils.MyDataBase;
import models.Service;
import models.Produit;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePersonne {
    private Connection conn;

    public ServicePersonne() {
        conn = MyDataBase.getInstance().getConnection();
    }

    // ========== MÉTHODES POUR LES SERVICES ==========

    public void ajouterService(Service service) throws SQLException {
        String query = "INSERT INTO service (id_service, nom_service, type_service, tarif, frequence, date_debut, date_fin, statut, id_produit) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, service.getIdService());
            pstmt.setString(2, service.getNomService());
            pstmt.setString(3, service.getTypeService().name());
            pstmt.setBigDecimal(4, service.getTarif());

            if (service.getFrequence() != null) {
                pstmt.setString(5, service.getFrequence().name());
            } else {
                pstmt.setNull(5, Types.VARCHAR);
            }

            if (service.getDateDebut() != null) {
                pstmt.setDate(6, Date.valueOf(service.getDateDebut()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }

            if (service.getDateFin() != null) {
                pstmt.setDate(7, Date.valueOf(service.getDateFin()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }

            pstmt.setString(8, service.getStatut().name());

            if (service.getIdProduit() != null) {
                pstmt.setInt(9, service.getIdProduit());
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }

            pstmt.executeUpdate();
            System.out.println("Service ajouté avec succès!");
        }
    }

    public List<Service> afficherTousServices() throws SQLException {
        List<Service> services = new ArrayList<>();
        String query = "SELECT * FROM service";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        }

        return services;
    }

    public void modifierService(Service service) throws SQLException {
        String query = "UPDATE service SET nom_service=?, type_service=?, tarif=?, frequence=?, date_debut=?, date_fin=?, statut=?, id_produit=? WHERE id_service=?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, service.getNomService());
            pstmt.setString(2, service.getTypeService().name());
            pstmt.setBigDecimal(3, service.getTarif());
            pstmt.setString(4, service.getFrequence() != null ? service.getFrequence().name() : null);
            pstmt.setDate(5, service.getDateDebut() != null ? Date.valueOf(service.getDateDebut()) : null);
            pstmt.setDate(6, service.getDateFin() != null ? Date.valueOf(service.getDateFin()) : null);
            pstmt.setString(7, service.getStatut().name());
            pstmt.setObject(8, service.getIdProduit());
            pstmt.setInt(9, service.getIdService());

            pstmt.executeUpdate();
            System.out.println("Service modifié avec succès!");
        }
    }

    public void supprimerService(int id) throws SQLException {
        String query = "DELETE FROM service WHERE id_service = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Service supprimé avec succès!");
        }
    }

    public Service getServiceById(int id) throws SQLException {
        String query = "SELECT * FROM service WHERE id_service = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToService(rs);
                }
            }
        }

        return null;
    }

    // ========== MÉTHODES POUR LES PRODUITS ==========

    public void ajouterProduit(Produit produit) throws SQLException {
        String query = "INSERT INTO produit (id_produit, nom_produit, type_produit, montant, code_unique, statut, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, produit.getIdProduit());
            pstmt.setString(2, produit.getNomProduit());
            pstmt.setString(3, produit.getTypeProduit().name());
            pstmt.setBigDecimal(4, produit.getMontant());
            pstmt.setString(5, produit.getCodeUnique());
            pstmt.setString(6, produit.getStatut().name());
            pstmt.setDate(7, Date.valueOf(produit.getDateCreation()));

            pstmt.executeUpdate();
            System.out.println("Produit ajouté avec succès!");
        }
    }

    public List<Produit> afficherTousProduits() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Produit produit = new Produit();
                produit.setIdProduit(rs.getInt("id_produit"));
                produit.setNomProduit(rs.getString("nom_produit"));
                produit.setTypeProduit(Produit.TypeProduit.valueOf(rs.getString("type_produit")));
                produit.setMontant(rs.getBigDecimal("montant"));
                produit.setCodeUnique(rs.getString("code_unique"));
                produit.setStatut(Produit.StatutProduit.valueOf(rs.getString("statut")));

                Date dateCreation = rs.getDate("date_creation");
                if (dateCreation != null) {
                    produit.setDateCreation(dateCreation.toLocalDate());
                }

                produits.add(produit);
            }
        }

        return produits;
    }

    public List<Produit> getProduitsDisponibles() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit WHERE statut = 'disponible'";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Produit produit = new Produit();
                produit.setIdProduit(rs.getInt("id_produit"));
                produit.setNomProduit(rs.getString("nom_produit"));
                produit.setTypeProduit(Produit.TypeProduit.valueOf(rs.getString("type_produit")));
                produit.setMontant(rs.getBigDecimal("montant"));
                produit.setCodeUnique(rs.getString("code_unique"));
                produit.setStatut(Produit.StatutProduit.valueOf(rs.getString("statut")));

                Date dateCreation = rs.getDate("date_creation");
                if (dateCreation != null) {
                    produit.setDateCreation(dateCreation.toLocalDate());
                }

                produits.add(produit);
            }
        }

        return produits;
    }

    public Produit getProduitById(int id) throws SQLException {
        String query = "SELECT * FROM produit WHERE id_produit = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Produit produit = new Produit();
                    produit.setIdProduit(rs.getInt("id_produit"));
                    produit.setNomProduit(rs.getString("nom_produit"));
                    produit.setTypeProduit(Produit.TypeProduit.valueOf(rs.getString("type_produit")));
                    produit.setMontant(rs.getBigDecimal("montant"));
                    produit.setCodeUnique(rs.getString("code_unique"));
                    produit.setStatut(Produit.StatutProduit.valueOf(rs.getString("statut")));

                    Date dateCreation = rs.getDate("date_creation");
                    if (dateCreation != null) {
                        produit.setDateCreation(dateCreation.toLocalDate());
                    }

                    return produit;
                }
            }
        }

        return null;
    }

    public void modifierProduit(Produit produit) throws SQLException {
        String query = "UPDATE produit SET nom_produit=?, type_produit=?, montant=?, code_unique=?, statut=? WHERE id_produit=?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, produit.getNomProduit());
            pstmt.setString(2, produit.getTypeProduit().name());
            pstmt.setBigDecimal(3, produit.getMontant());
            pstmt.setString(4, produit.getCodeUnique());
            pstmt.setString(5, produit.getStatut().name());
            pstmt.setInt(6, produit.getIdProduit());

            pstmt.executeUpdate();
            System.out.println("Produit modifié avec succès!");
        }
    }

    public void supprimerProduit(int id) throws SQLException {
        String query = "DELETE FROM produit WHERE id_produit = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Produit supprimé avec succès!");
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private Service mapResultSetToService(ResultSet rs) throws SQLException {
        Service service = new Service();
        service.setIdService(rs.getInt("id_service"));
        service.setNomService(rs.getString("nom_service"));
        service.setTypeService(Service.TypeService.valueOf(rs.getString("type_service")));
        service.setTarif(rs.getBigDecimal("tarif"));

        String frequence = rs.getString("frequence");
        if (frequence != null) {
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

        int idProduit = rs.getInt("id_produit");
        if (!rs.wasNull()) {
            service.setIdProduit(idProduit);
        }

        return service;
    }
}