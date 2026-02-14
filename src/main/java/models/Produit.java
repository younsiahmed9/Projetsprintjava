package models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Produit {
    private int idProduit;
    private String nomProduit;
    private TypeProduit typeProduit;
    private BigDecimal montant;
    private String codeUnique;
    private StatutProduit statut;
    private LocalDate dateCreation;

    public enum TypeProduit {
        carte_cadeau, carte_abonnement, carte_prepayee
    }

    public enum StatutProduit {
        disponible, vendu, expire
    }

    // Constructeurs
    public Produit() {}

    public Produit(int idProduit, String nomProduit, TypeProduit typeProduit,
                   BigDecimal montant, String codeUnique, StatutProduit statut,
                   LocalDate dateCreation) {
        this.idProduit = idProduit;
        this.nomProduit = nomProduit;
        this.typeProduit = typeProduit;
        this.montant = montant;
        this.codeUnique = codeUnique;
        this.statut = statut;
        this.dateCreation = dateCreation;
    }

    // Getters et Setters
    public int getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public TypeProduit getTypeProduit() {
        return typeProduit;
    }

    public void setTypeProduit(TypeProduit typeProduit) {
        this.typeProduit = typeProduit;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getCodeUnique() {
        return codeUnique;
    }

    public void setCodeUnique(String codeUnique) {
        this.codeUnique = codeUnique;
    }

    public StatutProduit getStatut() {
        return statut;
    }

    public void setStatut(StatutProduit statut) {
        this.statut = statut;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return nomProduit + " - " + codeUnique;
    }
}