package services;

import models.Carte;
import config.PromoConfig;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestionPromo {

    private List<Carte> cartes = new ArrayList<>();
    private List<Offre> offres = new ArrayList<>();

    public static class Offre {
        private int idCarte;
        private String nomCarte;
        private BigDecimal ancienPrix;
        private BigDecimal nouveauPrix;
        private int reduction;
        private LocalDate dateFin;

        public Offre(int id, String nom, BigDecimal prix, int reduction, int duree) {
            this.idCarte = id;
            this.nomCarte = nom;
            this.ancienPrix = prix;
            this.reduction = reduction;
            this.nouveauPrix = prix.multiply(BigDecimal.valueOf(1 - reduction / 100.0));
            this.dateFin = LocalDate.now().plusDays(duree);
        }

        public String getMessageSMS() {
            return String.format("⏰ PROMO CARTE EXPIREE ⏰\n%s\n%.2f€ au lieu de %.2f€ (-%d%%)",
                    nomCarte, nouveauPrix, ancienPrix, reduction);
        }

        public String getNom() { return nomCarte; }
        public BigDecimal getAncienPrix() { return ancienPrix; }
        public BigDecimal getNouveauPrix() { return nouveauPrix; }
        public int getReduction() { return reduction; }
    }

    public GestionPromo() {
        // Données simulées
        cartes.add(new Carte(1, "Carte Amazon 50€", "carte", 50, "expire"));
        cartes.add(new Carte(2, "Carte Netflix", "carte", 30, "expire"));
        cartes.add(new Carte(3, "Carte Spotify", "carte", 25, "actif"));
        cartes.add(new Carte(4, "Carte Google Play", "carte", 20, "expire"));
        cartes.add(new Carte(5, "Carte PlayStation", "carte", 50, "inactif"));
        System.out.println("✅ " + cartes.size() + " cartes chargées");
    }

    public List<Carte> getCartesExpirees() {
        List<Carte> result = new ArrayList<>();
        for (Carte c : cartes) {
            if (c.estCarte() && c.estExpiree()) {
                result.add(c);
            }
        }
        return result;
    }

    public List<Offre> creerOffres() {
        offres.clear();
        List<Carte> expirees = getCartesExpirees();
        int reduction = PromoConfig.getReduction();
        int duree = PromoConfig.getDuree();

        System.out.println("\n📦 " + expirees.size() + " carte(s) expirée(s) trouvée(s)");

        for (Carte c : expirees) {
            Offre offre = new Offre(c.getId(), c.getNom(), c.getPrix(), reduction, duree);
            offres.add(offre);
            System.out.println("   ✅ Offre créée: " + c.getNom() + " (-" + reduction + "%)");
        }
        return offres;
    }

    public List<Offre> getOffres() {
        return offres;
    }
}