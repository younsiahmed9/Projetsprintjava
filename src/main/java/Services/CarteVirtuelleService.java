package Services;

import Models.*;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarteVirtuelleService {

    private final PortefeuilleService portefeuilleService = new PortefeuilleService();

    // --- Utilitaires internes ---

    private Integer getPortefeuilleIdForCarte(int carteId) {
        String sql = "SELECT portefeuille_id FROM carte_virtuelle WHERE id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, carteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("portefeuille_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lecture portefeuille_id: " + e.getMessage());
        }
        return null;
    }

    private void mettreAJourSoldePortefeuille(int portefeuilleId) {
        portefeuilleService.recomputeAndUpdateSoldeTotal(portefeuilleId);
    }

    // --- CRUD ---

    public void ajouter(CarteVirtuelle c) {
        String sql = "INSERT INTO carte_virtuelle (numero_carte, solde, plafond, type, devise, portefeuille_id, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNumeroCarte());
            ps.setDouble(2, c.getSolde());
            ps.setDouble(3, c.getPlafond());
            ps.setString(4, c.getType().name());
            ps.setString(5, c.getDevise().name());
            ps.setInt(6, c.getPortefeuilleId());
            ps.setBoolean(7, c.isActiver());
            ps.executeUpdate();
            System.out.println("✅ Carte ajoutée");

            portefeuilleService.recomputeAndUpdateSoldeTotal(c.getPortefeuilleId());
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout: " + e.getMessage());
        }
    }

    public List<CarteVirtuelle> afficherTous() {
        List<CarteVirtuelle> list = new ArrayList<>();
        String sql = "SELECT * FROM carte_virtuelle";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                CarteVirtuelle c = mapResultSet(rs);
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur affichage: " + e.getMessage());
        }
        return list;
    }

    public CarteVirtuelle afficherParId(int id) {
        String sql = "SELECT * FROM carte_virtuelle WHERE id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche: " + e.getMessage());
        }
        return null;
    }

    public void modifier(CarteVirtuelle c) {
        Integer oldPortefeuilleId = getPortefeuilleIdForCarte(c.getId());

        String sql = "UPDATE carte_virtuelle SET solde=?, plafond=?, type=?, devise=?, is_active=? WHERE id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, c.getSolde());
            ps.setDouble(2, c.getPlafond());
            ps.setString(3, c.getType().name());
            ps.setString(4, c.getDevise().name());
            ps.setBoolean(5, c.isActiver());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
            System.out.println("✅ Carte modifiée");

            if (oldPortefeuilleId != null) {
                portefeuilleService.recomputeAndUpdateSoldeTotal(oldPortefeuilleId);
            }
            if (c.getPortefeuilleId() > 0 && (oldPortefeuilleId == null || oldPortefeuilleId != c.getPortefeuilleId())) {
                portefeuilleService.recomputeAndUpdateSoldeTotal(c.getPortefeuilleId());
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur modification: " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        Integer portefeuilleId = getPortefeuilleIdForCarte(id);

        String sql = "DELETE FROM carte_virtuelle WHERE id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Carte supprimée");
                if (portefeuilleId != null) {
                    portefeuilleService.recomputeAndUpdateSoldeTotal(portefeuilleId);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression: " + e.getMessage());
        }
    }

    public void recharger(int carteId, double montant) {
        Integer portefeuilleId = getPortefeuilleIdForCarte(carteId);

        String sql = "UPDATE carte_virtuelle SET solde = solde + ? WHERE id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, montant);
            ps.setInt(2, carteId);
            ps.executeUpdate();
            System.out.println("✅ Recharge de " + montant + " effectuée");

            if (portefeuilleId != null) {
                portefeuilleService.incrementSoldeTotal(portefeuilleId, montant);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recharge: " + e.getMessage());
        }
    }

    // --- Recherches spécifiques ---

    public List<CarteVirtuelle> getCartesByPortefeuille(int portefeuilleId) {
        List<CarteVirtuelle> list = new ArrayList<>();
        String sql = "SELECT * FROM carte_virtuelle WHERE portefeuille_id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, portefeuilleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getCartesByPortefeuille: " + e.getMessage());
        }
        return list;
    }

    public List<CarteVirtuelle> getCartesByUtilisateur(int utilisateurId) {
        List<CarteVirtuelle> list = new ArrayList<>();
        String sql = "SELECT c.* FROM carte_virtuelle c " +
                "INNER JOIN portefeuille p ON c.portefeuille_id = p.id " +
                "WHERE p.utilisateur_id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getCartesByUtilisateur: " + e.getMessage());
        }
        return list;
    }

    // --- SEARCH METHOD ---
    public List<CarteVirtuelle> rechercherCartes(String query) {
        List<CarteVirtuelle> list = new ArrayList<>();

        String sql = "SELECT DISTINCT c.* FROM carte_virtuelle c " +
                "LEFT JOIN portefeuille p ON c.portefeuille_id = p.id " +
                "LEFT JOIN utilisateur u ON p.utilisateur_id = u.id " +
                "WHERE c.numero_carte LIKE ? " +
                "OR u.email LIKE ? " +
                "OR CONCAT(u.prenom, ' ', u.nom) LIKE ? " +
                "OR c.id = ?";

        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";

            int idSearch = -1;
            try {
                idSearch = Integer.parseInt(query);
            } catch (NumberFormatException e) {
                // Not a number, ignore
            }

            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ps.setInt(4, idSearch > 0 ? idSearch : -1);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche cartes: " + e.getMessage());
        }
        return list;
    }

    // --- Transfert ---
    public boolean transferer(int carteSourceId, int carteDestId, double montant) {
        // Vérification d'adresse avant transfert
        try {
            Utilisateur currentUser = Models.Session.getUtilisateur();
            if (currentUser != null) {
                // Appel avec 2 paramètres (country, city)
                if (!AddressVerificationService.isAddressValidForTransfer(
                        currentUser.getCountry(),
                        currentUser.getCity())) {
                    System.err.println("❌ Transfert bloqué: adresse invalide");
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la vérification d'adresse: " + e.getMessage());
        }

        Connection conn = null;
        try {
            conn = MyDataBase.getInstance().getConnection();
            conn.setAutoCommit(false);

            String selectSql = "SELECT id, solde, devise, portefeuille_id FROM carte_virtuelle WHERE id = ? FOR UPDATE";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                // Carte source
                selectStmt.setInt(1, carteSourceId);
                ResultSet rsSource = selectStmt.executeQuery();
                if (!rsSource.next()) {
                    throw new SQLException("Carte source introuvable");
                }
                double soldeSource = rsSource.getDouble("solde");
                String deviseSource = rsSource.getString("devise");
                int portefeuilleSourceId = rsSource.getInt("portefeuille_id");

                // Carte destination
                selectStmt.setInt(1, carteDestId);
                ResultSet rsDest = selectStmt.executeQuery();
                if (!rsDest.next()) {
                    throw new SQLException("Carte destination introuvable");
                }
                String deviseDest = rsDest.getString("devise");
                int portefeuilleDestId = rsDest.getInt("portefeuille_id");

                if (soldeSource < montant) {
                    throw new SQLException("Solde insuffisant sur la carte source");
                }

                double montantConverti;
                if (deviseSource.equals(deviseDest)) {
                    montantConverti = montant;
                } else {
                    montantConverti = ConversionDevise.convertir(montant, deviseSource, deviseDest);
                }

                // Débiter la source
                String updateSourceSql = "UPDATE carte_virtuelle SET solde = solde - ? WHERE id = ?";
                try (PreparedStatement updateSource = conn.prepareStatement(updateSourceSql)) {
                    updateSource.setDouble(1, montant);
                    updateSource.setInt(2, carteSourceId);
                    updateSource.executeUpdate();
                }

                // Créditer la destination
                String updateDestSql = "UPDATE carte_virtuelle SET solde = solde + ? WHERE id = ?";
                try (PreparedStatement updateDest = conn.prepareStatement(updateDestSql)) {
                    updateDest.setDouble(1, montantConverti);
                    updateDest.setInt(2, carteDestId);
                    updateDest.executeUpdate();
                }

                // Enregistrer la transaction
                String insertTransactionSql = "INSERT INTO transaction (montant, devise, type, statut, carte_source_id, carte_dest_id, description) VALUES (?, ?, 'TRANSFERT', 'SUCCESS', ?, ?, ?)";
                try (PreparedStatement insertTrans = conn.prepareStatement(insertTransactionSql)) {
                    insertTrans.setDouble(1, montant);
                    insertTrans.setString(2, deviseSource);
                    insertTrans.setInt(3, carteSourceId);
                    insertTrans.setInt(4, carteDestId);
                    insertTrans.setString(5, "Transfert entre cartes");
                    insertTrans.executeUpdate();
                }

                // Mettre à jour les soldes des portefeuilles
                mettreAJourSoldePortefeuille(portefeuilleSourceId);
                if (portefeuilleSourceId != portefeuilleDestId) {
                    mettreAJourSoldePortefeuille(portefeuilleDestId);
                }

                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    String insertFailSql = "INSERT INTO transaction (montant, devise, type, statut, carte_source_id, carte_dest_id, description) VALUES (?, ?, 'TRANSFERT', 'FAILED', ?, ?, ?)";
                    try (PreparedStatement insertFail = conn.prepareStatement(insertFailSql)) {
                        insertFail.setDouble(1, montant);
                        insertFail.setString(2, "DT");
                        insertFail.setInt(3, carteSourceId);
                        insertFail.setInt(4, carteDestId);
                        insertFail.setString(5, "Échec: " + e.getMessage());
                        insertFail.executeUpdate();
                    }
                    conn.commit();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // --- Méthode utilitaire de mapping ---
    private CarteVirtuelle mapResultSet(ResultSet rs) throws SQLException {
        CarteVirtuelle c = new CarteVirtuelle();
        c.setId(rs.getInt("id"));
        c.setNumeroCarte(rs.getString("numero_carte"));
        c.setSolde(rs.getDouble("solde"));
        c.setPlafond(rs.getDouble("plafond"));
        c.setType(rs.getString("type"));
        c.setDevise(rs.getString("devise"));
        c.setPortefeuilleId(rs.getInt("portefeuille_id"));
        c.setActiver(rs.getBoolean("is_active"));

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) c.setCreatedAt(createdTs.toLocalDateTime());
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) c.setUpdatedAt(updatedTs.toLocalDateTime());
        return c;
    }
}