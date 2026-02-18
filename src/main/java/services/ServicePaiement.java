package services;

import models.*;
import utils.MyDataBase;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServicePaiement {
    private Connection conn;
    private ServiceService serviceService;
    private ServiceProduit produitService;

    public ServicePaiement() {
        this.conn = MyDataBase.getInstance().getConnection();
        this.serviceService = new ServiceService();
        this.produitService = new ServiceProduit();
    }

    // Ajouter un paiement
    public void ajouter(Paiement paiement) throws SQLException {
        String query = "INSERT INTO paiement (montant, date_paiement, id_service, id_produit, mode_paiement, statut, reference_transaction) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setBigDecimal(1, paiement.getMontant());
            pstmt.setDate(2, Date.valueOf(paiement.getDatePaiement()));
            pstmt.setObject(3, paiement.getIdService());
            pstmt.setObject(4, paiement.getIdProduit());
            pstmt.setString(5, paiement.getModePaiement().name());
            pstmt.setString(6, paiement.getStatut().name());
            pstmt.setString(7, paiement.getReferenceTransaction());

            pstmt.executeUpdate();

            // Récupérer l'ID généré
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                paiement.setIdPaiement(rs.getInt(1));
            }

            // Si c'est une carte d'abonnement, créer automatiquement le service associé
            if (paiement.getModePaiement() == Paiement.ModePaiement.carte_abonnement && paiement.getIdProduit() != null) {
                creerServiceDepuisCarte(paiement);
            }
        }
    }

    // Créer un service à partir d'une carte d'abonnement
    private void creerServiceDepuisCarte(Paiement paiement) throws SQLException {
        Produit produit = produitService.getById(paiement.getIdProduit());
        if (produit != null && produit.getTypeProduit() == Produit.TypeProduit.carte_abonnement) {

            Service service = new Service();
            service.setNomService("Abonnement " + produit.getNomProduit());
            service.setTypeService(Service.TypeService.abonnement);
            service.setTarif(produit.getMontant());
            service.setFrequence(Service.Frequence.mensuel);
            service.setDateDebut(LocalDate.now());
            service.setDateFin(LocalDate.now().plusMonths(1));
            service.setStatut(Service.StatutService.actif);

            // Générer un ID
            List<Service> services = serviceService.recuperer();
            int newId = services.stream().mapToInt(Service::getIdService).max().orElse(0) + 1;
            service.setIdService(newId);

            serviceService.ajouter(service);

            // Mettre à jour le paiement avec l'ID du service créé
            paiement.setIdService(service.getIdService());
            updateServiceId(paiement.getIdPaiement(), service.getIdService());

            // Marquer la carte comme vendue
            produit.setStatut(Produit.StatutProduit.vendu);
            produitService.modifier(produit);
        }
    }

    // Mettre à jour l'ID service dans un paiement
    private void updateServiceId(int idPaiement, int idService) throws SQLException {
        String query = "UPDATE paiement SET id_service = ? WHERE id_paiement = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idService);
            pstmt.setInt(2, idPaiement);
            pstmt.executeUpdate();
        }
    }

    // Récupérer tous les paiements
    public List<Paiement> recupererTous() throws SQLException {
        List<Paiement> paiements = new ArrayList<>();
        String query = "SELECT * FROM paiement ORDER BY date_paiement DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                paiements.add(mapResultSetToPaiement(rs));
            }
        }

        // Charger les services et produits associés
        for (Paiement p : paiements) {
            if (p.getIdService() != null) {
                p.setService(serviceService.getById(p.getIdService()));
            }
            if (p.getIdProduit() != null) {
                p.setProduit(produitService.getById(p.getIdProduit()));
            }
        }

        return paiements;
    }

    // Récupérer les paiements par service
    public List<Paiement> getByService(int idService) throws SQLException {
        List<Paiement> paiements = new ArrayList<>();
        String query = "SELECT * FROM paiement WHERE id_service = ? ORDER BY date_paiement DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idService);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                paiements.add(mapResultSetToPaiement(rs));
            }
        }

        return paiements;
    }

    // Récupérer les paiements par produit
    public List<Paiement> getByProduit(int idProduit) throws SQLException {
        List<Paiement> paiements = new ArrayList<>();
        String query = "SELECT * FROM paiement WHERE id_produit = ? ORDER BY date_paiement DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idProduit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                paiements.add(mapResultSetToPaiement(rs));
            }
        }

        return paiements;
    }

    // Récupérer un paiement par ID
    public Paiement getById(int id) throws SQLException {
        String query = "SELECT * FROM paiement WHERE id_paiement = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPaiement(rs);
            }
        }
        return null;
    }

    // Mettre à jour le statut
    public void updateStatut(int idPaiement, Paiement.StatutPaiement statut) throws SQLException {
        String query = "UPDATE paiement SET statut = ? WHERE id_paiement = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, statut.name());
            pstmt.setInt(2, idPaiement);
            pstmt.executeUpdate();
        }
    }

    // Mapping ResultSet vers Paiement
    private Paiement mapResultSetToPaiement(ResultSet rs) throws SQLException {
        Paiement p = new Paiement();
        p.setIdPaiement(rs.getInt("id_paiement"));
        p.setMontant(rs.getBigDecimal("montant"));
        p.setDatePaiement(rs.getDate("date_paiement").toLocalDate());
        p.setIdService(rs.getInt("id_service"));
        if (rs.wasNull()) p.setIdService(null);
        p.setIdProduit(rs.getInt("id_produit"));
        if (rs.wasNull()) p.setIdProduit(null);
        p.setModePaiement(Paiement.ModePaiement.valueOf(rs.getString("mode_paiement")));
        p.setStatut(Paiement.StatutPaiement.valueOf(rs.getString("statut")));
        p.setReferenceTransaction(rs.getString("reference_transaction"));
        return p;
    }

    public BigDecimal getTotalPaiements(LocalDate debutMois, LocalDate finMois) {
        String query = "SELECT SUM(montant) as total FROM paiement WHERE date_paiement BETWEEN ? AND ? AND statut = 'effectue'";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(debutMois));
            pstmt.setDate(2, Date.valueOf(finMois));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("total") != null ? rs.getBigDecimal("total") : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            e.printStackTrace();
    }
        return BigDecimal.ZERO;
    }
}