package models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DashboardStats {

    // Statistiques globales
    private int totalServices;
    private int totalProduits;
    private int totalPaiements;
    private BigDecimal chiffreAffaires;
    private BigDecimal chiffreAffairesMois;

    // Répartition par type
    private Map<String, Integer> servicesParType;
    private Map<String, Integer> produitsParType;
    private Map<String, Integer> paiementsParMode;
    private Map<String, BigDecimal> revenusParMois;

    // Alertes
    private int servicesExpires;
    private int servicesSuspendus;
    private int produitsVendus;
    private int produitsExpires;

    // Derniers éléments
    private List<Service> derniersServices;
    private List<Produit> derniersProduits;
    private List<Paiement> derniersPaiements;

    // Getters et Setters
    public int getTotalServices() { return totalServices; }
    public void setTotalServices(int totalServices) { this.totalServices = totalServices; }

    public int getTotalProduits() { return totalProduits; }
    public void setTotalProduits(int totalProduits) { this.totalProduits = totalProduits; }

    public int getTotalPaiements() { return totalPaiements; }
    public void setTotalPaiements(int totalPaiements) { this.totalPaiements = totalPaiements; }

    public BigDecimal getChiffreAffaires() { return chiffreAffaires; }
    public void setChiffreAffaires(BigDecimal chiffreAffaires) { this.chiffreAffaires = chiffreAffaires; }

    public BigDecimal getChiffreAffairesMois() { return chiffreAffairesMois; }
    public void setChiffreAffairesMois(BigDecimal chiffreAffairesMois) { this.chiffreAffairesMois = chiffreAffairesMois; }

    public Map<String, Integer> getServicesParType() { return servicesParType; }
    public void setServicesParType(Map<String, Integer> servicesParType) { this.servicesParType = servicesParType; }

    public Map<String, Integer> getProduitsParType() { return produitsParType; }
    public void setProduitsParType(Map<String, Integer> produitsParType) { this.produitsParType = produitsParType; }

    public Map<String, Integer> getPaiementsParMode() { return paiementsParMode; }
    public void setPaiementsParMode(Map<String, Integer> paiementsParMode) { this.paiementsParMode = paiementsParMode; }

    public Map<String, BigDecimal> getRevenusParMois() { return revenusParMois; }
    public void setRevenusParMois(Map<String, BigDecimal> revenusParMois) { this.revenusParMois = revenusParMois; }

    public int getServicesExpires() { return servicesExpires; }
    public void setServicesExpires(int servicesExpires) { this.servicesExpires = servicesExpires; }

    public int getServicesSuspendus() { return servicesSuspendus; }
    public void setServicesSuspendus(int servicesSuspendus) { this.servicesSuspendus = servicesSuspendus; }

    public int getProduitsVendus() { return produitsVendus; }
    public void setProduitsVendus(int produitsVendus) { this.produitsVendus = produitsVendus; }

    public int getProduitsExpires() { return produitsExpires; }
    public void setProduitsExpires(int produitsExpires) { this.produitsExpires = produitsExpires; }

    public List<Service> getDerniersServices() { return derniersServices; }
    public void setDerniersServices(List<Service> derniersServices) { this.derniersServices = derniersServices; }

    public List<Produit> getDerniersProduits() { return derniersProduits; }
    public void setDerniersProduits(List<Produit> derniersProduits) { this.derniersProduits = derniersProduits; }

    public List<Paiement> getDerniersPaiements() { return derniersPaiements; }
    public void setDerniersPaiements(List<Paiement> derniersPaiements) { this.derniersPaiements = derniersPaiements; }
}