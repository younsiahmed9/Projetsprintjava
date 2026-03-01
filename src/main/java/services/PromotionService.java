package services;

import models.Produit;
import models.Facture;
import config.APIConfig.ApiConfig;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PromotionService {

    private ServicePersonne servicePersonne;
    private Map<Integer, Promotion> promotionsActives = new HashMap<>();

    public static class Promotion {
        private int idProduit;
        private String nomProduit;
        private String typeProduit;
        private BigDecimal ancienPrix;
        private BigDecimal nouveauPrix;
        private double pourcentageReduction;
        private LocalDate dateDebut;
        private LocalDate dateFin;
        private String typePromo;
        private String message;

        public Promotion(int idProduit, String nomProduit, String typeProduit,
                         BigDecimal ancienPrix, double pourcentageReduction,
                         int dureeJours, String typePromo) {
            this.idProduit = idProduit;
            this.nomProduit = nomProduit;
            this.typeProduit = typeProduit;
            this.ancienPrix = ancienPrix;
            this.pourcentageReduction = pourcentageReduction;
            this.nouveauPrix = ancienPrix.multiply(BigDecimal.valueOf(1 - pourcentageReduction / 100));
            this.dateDebut = LocalDate.now();
            this.dateFin = LocalDate.now().plusDays(dureeJours);
            this.typePromo = typePromo;
            this.message = genererMessage();
        }

        private String genererMessage() {
            String emoji = "expiree".equals(typePromo) ? "⏰" : "🔥";
            return String.format("%s %s %s\n%s\n%.2f € au lieu de %.2f € (-%.0f%%)",
                    emoji, typePromo.toUpperCase(), emoji,
                    nomProduit, nouveauPrix, ancienPrix, pourcentageReduction);
        }

        // Getters
        public int getIdProduit() { return idProduit; }
        public String getNomProduit() { return nomProduit; }
        public String getTypeProduit() { return typeProduit; }
        public BigDecimal getAncienPrix() { return ancienPrix; }
        public BigDecimal getNouveauPrix() { return nouveauPrix; }
        public double getPourcentageReduction() { return pourcentageReduction; }
        public LocalDate getDateDebut() { return dateDebut; }
        public LocalDate getDateFin() { return dateFin; }
        public String getTypePromo() { return typePromo; }
        public String getMessage() { return message; }
        public boolean isActive() {
            return LocalDate.now().isBefore(dateFin) || LocalDate.now().isEqual(dateFin);
        }
    }

    public PromotionService() {
        this.servicePersonne = new ServicePersonne();
        System.out.println("✅ Service de promotion initialisé");
    }

    /**
     * Détecte les cartes expirées
     */
    public List<Produit> detecterCartesExpirees() {
        try {
            List<Produit> tousProduits = servicePersonne.getAllProduits();

            return tousProduits.stream()
                    .filter(p -> p.getTypeProduit() != null &&
                            p.getTypeProduit().toLowerCase().contains("carte") &&
                            p.getStatut() != null &&
                            (p.getStatut().equalsIgnoreCase("expire") ||
                                    p.getStatut().equalsIgnoreCase("inactif") ||
                                    p.getStatut().equalsIgnoreCase("expiré")))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("❌ Erreur détection cartes expirées: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Détecte les cartes non vendues depuis X jours
     */
    public List<Produit> detecterCartesNonVendues(int jours) {
        try {
            List<Produit> tousProduits = servicePersonne.getAllProduits();
            List<Facture> toutesFactures = servicePersonne.getAllFactures();
            LocalDate dateLimite = LocalDate.now().minusDays(jours);

            List<Produit> cartes = tousProduits.stream()
                    .filter(p -> p.getTypeProduit() != null &&
                            p.getTypeProduit().toLowerCase().contains("carte"))
                    .collect(Collectors.toList());

            return cartes.stream()
                    .filter(carte -> {
                        boolean vendue = toutesFactures.stream()
                                .anyMatch(f -> f.getIdProduit() != null &&
                                        f.getIdProduit().equals(carte.getIdProduit()) &&
                                        f.getDateFacture() != null &&
                                        f.getDateFacture().isAfter(dateLimite));
                        return !vendue;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("❌ Erreur détection cartes non vendues: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Crée des promotions pour les cartes expirées
     */
    public List<Promotion> promouvoirCartesExpirees() {
        List<Promotion> nouvellesPromos = new ArrayList<>();

        System.out.println("\n🔍 RECHERCHE DES CARTES EXPIRÉES...");

        List<Produit> cartesExpirees = detecterCartesExpirees();

        if (cartesExpirees.isEmpty()) {
            System.out.println("✅ Aucune carte expirée trouvée");
            return nouvellesPromos;
        }

        System.out.println("📦 " + cartesExpirees.size() + " carte(s) expirée(s) trouvée(s)");

        double reduction = ApiConfig.getPromotionReductionExpiree();
        int duree = ApiConfig.getPromotionDureeJours();

        for (Produit carte : cartesExpirees) {
            if (promotionsActives.containsKey(carte.getIdProduit())) {
                continue;
            }

            Promotion promo = new Promotion(
                    carte.getIdProduit(),
                    carte.getNomProduit(),
                    carte.getTypeProduit(),
                    carte.getMontant(),
                    reduction,
                    duree,
                    "expiree"
            );

            promotionsActives.put(carte.getIdProduit(), promo);
            nouvellesPromos.add(promo);

            System.out.println("   ✅ Promotion créée: " + carte.getNomProduit() + " (-" + reduction + "%)");
        }

        return nouvellesPromos;
    }

    /**
     * Lance toutes les promotions
     */
    public Map<Integer, Promotion> lancerPromotionsCompletes() {
        Map<Integer, Promotion> toutesPromos = new HashMap<>();

        var promosExpirees = promouvoirCartesExpirees();
        for (Promotion p : promosExpirees) {
            toutesPromos.put(p.getIdProduit(), p);
        }

        return toutesPromos;
    }

    public List<Promotion> getPromotionsActives() {
        return promotionsActives.values().stream()
                .filter(Promotion::isActive)
                .collect(Collectors.toList());
    }
}