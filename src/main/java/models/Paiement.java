package models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Paiement {
    private int idPaiement;
    private BigDecimal montant;
    private LocalDate datePaiement;
    private Integer idService;
    private Integer idProduit;
    private ModePaiement modePaiement;
    private StatutPaiement statut;
    private String referenceTransaction;

    // Service et Produit associés (pour affichage)
    private Service service;
    private Produit produit;

    public enum ModePaiement {
        carte_bancaire, carte_cadeau, carte_abonnement, carte_prepayee, especes
    }

    public enum StatutPaiement {
        effectue, en_attente, echoue, rembourse
    }

    // Constructeurs
    public Paiement() {}

    public Paiement(BigDecimal montant, ModePaiement modePaiement, Integer idService, Integer idProduit) {
        this.montant = montant;
        this.modePaiement = modePaiement;
        this.idService = idService;
        this.idProduit = idProduit;
        this.datePaiement = LocalDate.now();
        this.statut = StatutPaiement.effectue;
        this.referenceTransaction = generateReference();
    }

    public Paiement(int idPaiement, BigDecimal montant, LocalDate datePaiement,
                    Integer idService, Integer idProduit, ModePaiement modePaiement,
                    StatutPaiement statut, String referenceTransaction) {
        this.idPaiement = idPaiement;
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.idService = idService;
        this.idProduit = idProduit;
        this.modePaiement = modePaiement;
        this.statut = statut;
        this.referenceTransaction = referenceTransaction;
    }

    private String generateReference() {
        return "PAY-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    // Getters et Setters
    public int getIdPaiement() { return idPaiement; }
    public void setIdPaiement(int idPaiement) { this.idPaiement = idPaiement; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public LocalDate getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDate datePaiement) { this.datePaiement = datePaiement; }

    public Integer getIdService() { return idService; }
    public void setIdService(Integer idService) { this.idService = idService; }

    public Integer getIdProduit() { return idProduit; }
    public void setIdProduit(Integer idProduit) { this.idProduit = idProduit; }

    public ModePaiement getModePaiement() { return modePaiement; }
    public void setModePaiement(ModePaiement modePaiement) { this.modePaiement = modePaiement; }

    public StatutPaiement getStatut() { return statut; }
    public void setStatut(StatutPaiement statut) { this.statut = statut; }

    public String getReferenceTransaction() { return referenceTransaction; }
    public void setReferenceTransaction(String referenceTransaction) { this.referenceTransaction = referenceTransaction; }

    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }

    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }

    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + idPaiement +
                ", montant=" + montant +
                ", date=" + datePaiement +
                ", mode=" + modePaiement +
                ", statut=" + statut +
                ", ref='" + referenceTransaction + '\'' +
                '}';
    }
}