package services;

import models.*;
import utils.MyDataBase;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ServiceDashboard {

    private Connection conn;
    private ServiceService serviceService;
    private ServiceProduit produitService;
    private ServicePaiement paiementService;

    public ServiceDashboard() {
        this.conn = MyDataBase.getInstance().getConnection();
        this.serviceService = new ServiceService();
        this.produitService = new ServiceProduit();
        this.paiementService = new ServicePaiement();
    }

    public DashboardStats getStats() throws SQLException {
        DashboardStats stats = new DashboardStats();

        // Récupérer toutes les données
        List<Service> services = serviceService.recuperer();
        List<Produit> produits = produitService.recuperer();
        List<Paiement> paiements = paiementService.recupererTous();

        // Statistiques générales
        stats.setTotalServices(services.size());
        stats.setTotalProduits(produits.size());
        stats.setTotalPaiements(paiements.size());

        // Chiffre d'affaires
        BigDecimal caTotal = paiements.stream()
                .filter(p -> p.getStatut() == Paiement.StatutPaiement.effectue)
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setChiffreAffaires(caTotal);

        // CA du mois
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        LocalDate finMois = LocalDate.now();
        BigDecimal caMois = paiementService.getTotalPaiements(debutMois, finMois);
        stats.setChiffreAffairesMois(caMois);

        // Répartition des services par type
        Map<String, Integer> servicesParType = new HashMap<>();
        servicesParType.put("Abonnements",
                (int) services.stream().filter(s -> s.getTypeService() == Service.TypeService.abonnement).count());
        servicesParType.put("Factures",
                (int) services.stream().filter(s -> s.getTypeService() == Service.TypeService.facture).count());
        stats.setServicesParType(servicesParType);

        // Répartition des produits par type
        Map<String, Integer> produitsParType = new HashMap<>();
        for (Produit.TypeProduit type : Produit.TypeProduit.values()) {
            produitsParType.put(type.name(),
                    (int) produits.stream().filter(p -> p.getTypeProduit() == type).count());
        }
        stats.setProduitsParType(produitsParType);

        // Répartition des paiements par mode
        Map<String, Integer> paiementsParMode = new HashMap<>();
        for (Paiement.ModePaiement mode : Paiement.ModePaiement.values()) {
            paiementsParMode.put(mode.name(),
                    (int) paiements.stream().filter(p -> p.getModePaiement() == mode).count());
        }
        stats.setPaiementsParMode(paiementsParMode);

        // Alertes
        stats.setServicesExpires(
                (int) services.stream().filter(s -> s.getStatut() == Service.StatutService.expire).count());
        stats.setServicesSuspendus(
                (int) services.stream().filter(s -> s.getStatut() == Service.StatutService.suspendu).count());
        stats.setProduitsVendus(
                (int) produits.stream().filter(p -> p.getStatut() == Produit.StatutProduit.vendu).count());
        stats.setProduitsExpires(
                (int) produits.stream().filter(p -> p.getStatut() == Produit.StatutProduit.expire).count());

        // Derniers éléments (limité à 5)
        stats.setDerniersServices(services.stream()
                .sorted((s1, s2) -> Integer.compare(s2.getIdService(), s1.getIdService()))
                .limit(5)
                .collect(Collectors.toList()));

        stats.setDerniersProduits(produits.stream()
                .sorted((p1, p2) -> Integer.compare(p2.getIdProduit(), p1.getIdProduit()))
                .limit(5)
                .collect(Collectors.toList()));

        stats.setDerniersPaiements(paiements.stream()
                .sorted((p1, p2) -> p2.getDatePaiement().compareTo(p1.getDatePaiement()))
                .limit(5)
                .collect(Collectors.toList()));

        return stats;
    }

    public Map<String, BigDecimal> getRevenusParMois(int annee) throws SQLException {
        Map<String, BigDecimal> revenus = new LinkedHashMap<>();
        String query = "SELECT MONTH(date_paiement) as mois, SUM(montant) as total " +
                "FROM paiement WHERE YEAR(date_paiement) = ? AND statut = 'effectue' " +
                "GROUP BY MONTH(date_paiement) ORDER BY mois";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, annee);
            ResultSet rs = pstmt.executeQuery();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM");

            while (rs.next()) {
                int mois = rs.getInt("mois");
                BigDecimal total = rs.getBigDecimal("total");
                String nomMois = LocalDate.of(annee, mois, 1).format(formatter);
                revenus.put(nomMois, total != null ? total : BigDecimal.ZERO);
            }
        }

        // Remplir les mois manquants avec 0
        for (int i = 1; i <= 12; i++) {
            String nomMois = LocalDate.of(annee, i, 1).format(DateTimeFormatter.ofPattern("MMMM"));
            revenus.putIfAbsent(nomMois, BigDecimal.ZERO);
        }

        return revenus;
    }
}