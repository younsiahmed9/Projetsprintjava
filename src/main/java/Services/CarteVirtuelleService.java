package Services;

import Models.*;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarteVirtuelleService {
    private Connection connection;
    private final PortefeuilleService portefeuilleService;

    public CarteVirtuelleService() {
        connection = MyDataBase.getInstance().getConnection();
        portefeuilleService = new PortefeuilleService();
    }

    // CREATE
    public void ajouter(CarteVirtuelle c) {
        String req = "INSERT INTO carte_virtuelle (numero_carte, solde, plafond, type, devise, portefeuille_id, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, c.getNumeroCarte());
            ps.setDouble(2, c.getSolde());
            ps.setDouble(3, c.getPlafond());
            ps.setString(4, c.getType().name());
            ps.setString(5, c.getDevise().name());
            ps.setInt(6, c.getPortefeuilleId());
            ps.setBoolean(7, c.isActiver());
            ps.executeUpdate();
            System.out.println("✅ Carte ajoutée");

            // Keep portefeuille total in sync
            portefeuilleService.recomputeAndUpdateSoldeTotal(c.getPortefeuilleId());
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    // READ ALL
    public List<CarteVirtuelle> afficherTous() {
        List<CarteVirtuelle> list = new ArrayList<>();
        String req = "SELECT * FROM carte_virtuelle";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                CarteVirtuelle c = new CarteVirtuelle();
                c.setId(rs.getInt("id"));
                c.setNumeroCarte(rs.getString("numero_carte"));
                c.setSolde(rs.getDouble("solde"));
                c.setPlafond(rs.getDouble("plafond"));
                c.setType(rs.getString("type"));
                c.setDevise(rs.getString("devise"));
                c.setPortefeuilleId(rs.getInt("portefeuille_id"));
                c.setActiver(rs.getBoolean("is_active"));
                list.add(c);
                System.out.println("   " + c);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
        return list;
    }

    // READ BY ID
    public CarteVirtuelle afficherParId(int id) {
        String req = "SELECT * FROM carte_virtuelle WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                CarteVirtuelle c = new CarteVirtuelle();
                c.setId(rs.getInt("id"));
                c.setNumeroCarte(rs.getString("numero_carte"));
                c.setSolde(rs.getDouble("solde"));
                c.setPlafond(rs.getDouble("plafond"));
                c.setType(rs.getString("type"));
                c.setDevise(rs.getString("devise"));
                c.setPortefeuilleId(rs.getInt("portefeuille_id"));
                c.setActiver(rs.getBoolean("is_active"));
                System.out.println("🔍 " + c);
                return c;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
        return null;
    }

    // UPDATE
    public void modifier(CarteVirtuelle c) {
        // We must know the previous portefeuille_id in case it changes
        Integer oldPortefeuilleId = getPortefeuilleIdForCarte(c.getId());

        String req = "UPDATE carte_virtuelle SET solde=?, plafond=?, type=?, devise=?, is_active=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setDouble(1, c.getSolde());
            ps.setDouble(2, c.getPlafond());
            ps.setString(3, c.getType().name());
            ps.setString(4, c.getDevise().name());
            ps.setBoolean(5, c.isActiver());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
            System.out.println("✅ Carte modifiée");

            // Keep totals in sync
            if (oldPortefeuilleId != null) {
                portefeuilleService.recomputeAndUpdateSoldeTotal(oldPortefeuilleId);
            }
            if (c.getPortefeuilleId() > 0 && (oldPortefeuilleId == null || oldPortefeuilleId != c.getPortefeuilleId())) {
                portefeuilleService.recomputeAndUpdateSoldeTotal(c.getPortefeuilleId());
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    // DELETE
    public void supprimer(int id) {
        Integer portefeuilleId = getPortefeuilleIdForCarte(id);

        String req = "DELETE FROM carte_virtuelle WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Carte supprimée");

            if (portefeuilleId != null) {
                portefeuilleService.recomputeAndUpdateSoldeTotal(portefeuilleId);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    // RECHARGER
    public void recharger(int carteId, double montant) {
        Integer portefeuilleId = getPortefeuilleIdForCarte(carteId);

        String req = "UPDATE carte_virtuelle SET solde = solde + ? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setDouble(1, montant);
            ps.setInt(2, carteId);
            ps.executeUpdate();
            System.out.println("✅ Recharge de " + montant + " effectuée");

            if (portefeuilleId != null) {
                // Fast path
                portefeuilleService.incrementSoldeTotal(portefeuilleId, montant);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    // CARTES PAR PORTEFEUILLE
    public List<CarteVirtuelle> getCartesByPortefeuille(int portefeuilleId) {
        List<CarteVirtuelle> list = new ArrayList<>();
        String req = "SELECT * FROM carte_virtuelle WHERE portefeuille_id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, portefeuilleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CarteVirtuelle c = new CarteVirtuelle();
                c.setId(rs.getInt("id"));
                c.setNumeroCarte(rs.getString("numero_carte"));
                c.setSolde(rs.getDouble("solde"));
                c.setPlafond(rs.getDouble("plafond"));
                c.setType(rs.getString("type"));
                c.setDevise(rs.getString("devise"));
                c.setPortefeuilleId(rs.getInt("portefeuille_id"));
                c.setActiver(rs.getBoolean("is_active"));
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
        return list;
    }

    private Integer getPortefeuilleIdForCarte(int carteId) {
        String req = "SELECT portefeuille_id FROM carte_virtuelle WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
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
}