package services;

import models.Facture;
import models.Service;
import models.Produit;
import utils.MyDataBase;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceFacture {
    private Connection conn;
    private ServiceService serviceService;
    private ServiceProduit produitService;

    public ServiceFacture() {
        this.conn = MyDataBase.getInstance().getConnection();
        this.serviceService = new ServiceService();
        this.produitService = new ServiceProduit();
    }

    // ==================== CRUD PRINCIPAL ====================

    // CREATE - Ajouter une facture
    public void ajouter(Facture facture) throws SQLException {
        // ✅ CORRECTION: Utilisez date_echeance (avec deux 'e')
        String query = "INSERT INTO facture (numero_facture, montant, date_facture, date_echeance, id_service, id_produit, statut) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, facture.getNumeroFacture());
            pstmt.setBigDecimal(2, facture.getMontant());
            pstmt.setDate(3, Date.valueOf(facture.getDateFacture()));
            pstmt.setDate(4, Date.valueOf(facture.getDateEcheance()));

            if (facture.getIdService() != null) {
                pstmt.setInt(5, facture.getIdService());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            if (facture.getIdProduit() != null) {
                pstmt.setInt(6, facture.getIdProduit());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            pstmt.setString(7, facture.getStatut());

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                facture.setIdFacture(rs.getInt(1));
            }
            System.out.println("✅ Facture ajoutée: " + facture.getNumeroFacture());
        }
    }

    // READ ALL - Récupérer toutes les factures
    public List<Facture> recupererToutes() throws SQLException {
        List<Facture> factures = new ArrayList<>();
        String query = "SELECT * FROM facture ORDER BY date_facture DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                factures.add(mapResultSetToFacture(rs));
            }
        }

        for (Facture f : factures) {
            if (f.getIdService() != null) {
                f.setService(serviceService.getById(f.getIdService()));
            }
            if (f.getIdProduit() != null) {
                f.setProduit(produitService.getById(f.getIdProduit()));
            }
        }
        return factures;
    }

    // READ BY ID - Récupérer une facture par son ID
    public Facture getById(int id) throws SQLException {
        String query = "SELECT * FROM facture WHERE id_facture = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Facture facture = mapResultSetToFacture(rs);

                if (facture.getIdService() != null) {
                    facture.setService(serviceService.getById(facture.getIdService()));
                }
                if (facture.getIdProduit() != null) {
                    facture.setProduit(produitService.getById(facture.getIdProduit()));
                }

                return facture;
            }
        }
        return null;
    }

    // UPDATE - Mettre à jour une facture
    public void modifier(Facture facture) throws SQLException {
        // ✅ CORRECTION: Utilisez date_echeance (avec deux 'e')
        String query = "UPDATE facture SET numero_facture=?, montant=?, date_facture=?, date_echeance=?, id_service=?, id_produit=?, statut=? WHERE id_facture=?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, facture.getNumeroFacture());
            pstmt.setBigDecimal(2, facture.getMontant());
            pstmt.setDate(3, Date.valueOf(facture.getDateFacture()));
            pstmt.setDate(4, Date.valueOf(facture.getDateEcheance()));

            if (facture.getIdService() != null) {
                pstmt.setInt(5, facture.getIdService());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            if (facture.getIdProduit() != null) {
                pstmt.setInt(6, facture.getIdProduit());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            pstmt.setString(7, facture.getStatut());
            pstmt.setInt(8, facture.getIdFacture());

            pstmt.executeUpdate();
            System.out.println("✅ Facture modifiée: " + facture.getNumeroFacture());
        }
    }

    // DELETE - Supprimer une facture
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM facture WHERE id_facture = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("✅ Facture supprimée: ID " + id);
        }
    }

    // ==================== GESTION DES STATUTS ====================

    public void updateStatut(int idFacture, String statut) throws SQLException {
        String query = "UPDATE facture SET statut = ? WHERE id_facture = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, statut);
            pstmt.setInt(2, idFacture);
            pstmt.executeUpdate();
            System.out.println("✅ Statut mis à jour: " + statut);
        }
    }

    public void payer(int idFacture) throws SQLException {
        updateStatut(idFacture, "payee");
    }

    public void annuler(int idFacture) throws SQLException {
        updateStatut(idFacture, "annulee");
    }

    public void mettreEnAttente(int idFacture) throws SQLException {
        updateStatut(idFacture, "en_attente");
    }

    // ==================== MÉTHODES DE RECHERCHE ====================

    public List<Facture> getFacturesByStatut(String statut) throws SQLException {
        List<Facture> factures = new ArrayList<>();
        String query = "SELECT * FROM facture WHERE statut = ? ORDER BY date_facture DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, statut);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                factures.add(mapResultSetToFacture(rs));
            }
        }
        return factures;
    }

    public List<Facture> getFacturesImpayees() throws SQLException {
        return getFacturesByStatut("impayee");
    }

    public List<Facture> getFacturesPayees() throws SQLException {
        return getFacturesByStatut("payee");
    }

    public List<Facture> getFacturesEnAttente() throws SQLException {
        return getFacturesByStatut("en_attente");
    }

    public List<Facture> getFacturesAnnulees() throws SQLException {
        return getFacturesByStatut("annulee");
    }

    public List<Facture> getFacturesParPeriode(LocalDate debut, LocalDate fin) throws SQLException {
        List<Facture> factures = new ArrayList<>();
        String query = "SELECT * FROM facture WHERE date_facture BETWEEN ? AND ? ORDER BY date_facture DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(debut));
            pstmt.setDate(2, Date.valueOf(fin));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                factures.add(mapResultSetToFacture(rs));
            }
        }
        return factures;
    }

    public List<Facture> getFacturesEnRetard() throws SQLException {
        // ✅ CORRECTION: Utilisez date_echeance (avec deux 'e')
        List<Facture> factures = new ArrayList<>();
        String query = "SELECT * FROM facture WHERE date_echeance < CURRENT_DATE AND statut IN ('impayee', 'en_attente') ORDER BY date_echeance";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                factures.add(mapResultSetToFacture(rs));
            }
        }
        return factures;
    }

    // ==================== MÉTHODES DE CALCUL ====================

    public BigDecimal getTotalImpayees() throws SQLException {
        String query = "SELECT COALESCE(SUM(montant), 0) as total FROM facture WHERE statut IN ('impayee', 'en_attente')";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTotalPayees() throws SQLException {
        String query = "SELECT COALESCE(SUM(montant), 0) as total FROM facture WHERE statut = 'payee'";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTotalGeneral() throws SQLException {
        String query = "SELECT COALESCE(SUM(montant), 0) as total FROM facture";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        }
        return BigDecimal.ZERO;
    }

    // ==================== MÉTHODES DE COMPTAGE ====================

    public int compterParStatut(String statut) throws SQLException {
        String query = "SELECT COUNT(*) as total FROM facture WHERE statut = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, statut);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    public int compterToutes() throws SQLException {
        String query = "SELECT COUNT(*) as total FROM facture";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    // ==================== MÉTHODES DE VÉRIFICATION ====================

    public boolean existe(int id) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM facture WHERE id_facture = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        }
        return false;
    }

    public boolean numeroExiste(String numero) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM facture WHERE numero_facture = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, numero);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        }
        return false;
    }

    // ==================== MÉTHODE DE MAPPING ====================

    private Facture mapResultSetToFacture(ResultSet rs) throws SQLException {
        Facture f = new Facture();
        f.setIdFacture(rs.getInt("id_facture"));
        f.setNumeroFacture(rs.getString("numero_facture"));
        f.setMontant(rs.getBigDecimal("montant"));

        Date dateFacture = rs.getDate("date_facture");
        if (dateFacture != null) {
            f.setDateFacture(dateFacture.toLocalDate());
        }

        Date dateEcheance = rs.getDate("date_echeance");
        if (dateEcheance != null) {
            f.setDateEcheance(dateEcheance.toLocalDate());
        }

        f.setIdService(rs.getInt("id_service"));
        if (rs.wasNull()) f.setIdService(null);

        f.setIdProduit(rs.getInt("id_produit"));
        if (rs.wasNull()) f.setIdProduit(null);

        f.setStatut(rs.getString("statut"));

        return f;
    }
    /**
     * Récupère toutes les factures (version corrigée)
     * @return Liste de toutes les factures
     * @throws SQLException en cas d'erreur de base de données
     */
    public List<Facture> recupererToutesLesFactures() throws SQLException {
        List<Facture> factures = new ArrayList<>();

        // ✅ CORRECTION: Utilisez les noms de colonnes corrects (avec underscores)
        String query = "SELECT id_facture, numero_facture, montant, date_facture, date_echeance, statut, " +
                "id_service, id_produit FROM facture ORDER BY date_facture DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Facture facture = new Facture();

                // ✅ CORRECTION: Utilisez les bons noms de colonnes
                facture.setIdFacture(rs.getInt("id_facture"));
                facture.setNumeroFacture(rs.getString("numero_facture"));
                facture.setMontant(rs.getBigDecimal("montant"));
                facture.setStatut(rs.getString("statut"));

                Date dateFacture = rs.getDate("date_facture");
                if (dateFacture != null) {
                    facture.setDateFacture(dateFacture.toLocalDate());
                }

                Date dateEcheance = rs.getDate("date_echeance");
                if (dateEcheance != null) {
                    facture.setDateEcheance(dateEcheance.toLocalDate());
                }

                // Récupérer les IDs des relations
                int idService = rs.getInt("id_service");
                if (!rs.wasNull()) {
                    facture.setIdService(idService);
                }

                int idProduit = rs.getInt("id_produit");
                if (!rs.wasNull()) {
                    facture.setIdProduit(idProduit);
                }

                factures.add(facture);
            }
        }

        // Charger les services et produits associés
        for (Facture f : factures) {
            if (f.getIdService() != null) {
                try {
                    f.setService(serviceService.getById(f.getIdService()));
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur chargement service pour facture " + f.getIdFacture());
                }
            }
            if (f.getIdProduit() != null) {
                try {
                    f.setProduit(produitService.getById(f.getIdProduit()));
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur chargement produit pour facture " + f.getIdFacture());
                }
            }
        }

        return factures;
    }
}