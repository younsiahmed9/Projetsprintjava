package models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Produit {
    private Integer idProduit;
    private String nomProduit;
    private String typeProduit;
    private BigDecimal montant;
    private String codeUnique;
    private String statut;
    private LocalDate dateCreation;

    // Constructeurs
    public Produit() {}

    public Produit(Integer idProduit, String nomProduit, String typeProduit, BigDecimal montant,
                   String codeUnique, String statut, LocalDate dateCreation) {
        this.idProduit = idProduit;
        // Utilisation des setters pour la validation dès la construction
        setNomProduit(nomProduit);
        setTypeProduit(typeProduit);
        setMontant(montant);
        setCodeUnique(codeUnique);
        setStatut(statut);
        setDateCreation(dateCreation);
    }

    // Getters et Setters
    public Integer getIdProduit() { return idProduit; }
    public void setIdProduit(Integer idProduit) { this.idProduit = idProduit; }

    public String getNomProduit() { return nomProduit; }
    public void setNomProduit(String nomProduit) {
        if (nomProduit == null || nomProduit.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du produit ne peut pas être vide.");
        }
        this.nomProduit = nomProduit;
    }

    public String getTypeProduit() { return typeProduit; }
    public void setTypeProduit(String typeProduit) {
        if (typeProduit == null || typeProduit.trim().isEmpty()) {
            throw new IllegalArgumentException("Le type de produit ne peut pas être vide.");
        }
        this.typeProduit = typeProduit;
    }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) {
        if (montant == null || montant.signum() < 0) {
            throw new IllegalArgumentException("Le montant ne peut pas être nul ou négatif.");
        }
        this.montant = montant;
    }

    public String getCodeUnique() { return codeUnique; }
    public void setCodeUnique(String codeUnique) {
        if (codeUnique == null || codeUnique.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code unique ne peut pas être vide.");
        }
        this.codeUnique = codeUnique;
    }

    public String getStatut() { return statut; }
    public void setStatut(String statut) {
        if (statut == null || statut.trim().isEmpty()) {
            throw new IllegalArgumentException("Le statut ne peut pas être vide.");
        }
        this.statut = statut;
    }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) {
        if (dateCreation == null) {
            throw new IllegalArgumentException("La date de création ne peut pas être nulle.");
        }
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return nomProduit + " - " + codeUnique;
    }
}