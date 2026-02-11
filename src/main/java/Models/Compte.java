package Models;

import java.time.LocalDate;

public class Compte {

    protected int idCompte;
    protected String numeroCompte;
    protected double solde;
    protected String typeCompte; // EPARGNE ou COURANT
    protected Double tauxInteret; // utilisé si EPARGNE
    protected Double plafondDecouvert; // utilisé si COURANT
    protected LocalDate dateCreation;
    protected String etat; // ACTIF ou BLOQUE

    public Compte() {
    }

    public Compte(String numeroCompte, double solde, String typeCompte,
                  Double tauxInteret, Double plafondDecouvert,
                  LocalDate dateCreation, String etat) {

        this.numeroCompte = numeroCompte;
        this.solde = solde;
        this.typeCompte = typeCompte;
        this.tauxInteret = tauxInteret;
        this.plafondDecouvert = plafondDecouvert;
        this.dateCreation = dateCreation;
        this.etat = etat;
    }

    public Compte(int idCompte, String numeroCompte, double solde, String typeCompte,
                  Double tauxInteret, Double plafondDecouvert,
                  LocalDate dateCreation, String etat) {

        this(numeroCompte, solde, typeCompte, tauxInteret, plafondDecouvert, dateCreation, etat);
        this.idCompte = idCompte;
    }

    public Compte(int idCompte){
        this.idCompte=idCompte;
    }
    // ================= GETTERS & SETTERS =================

    public int getIdCompte() {
        return idCompte;
    }

    public void setIdCompte(int idCompte) {
        this.idCompte = idCompte;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public double getSolde() {
        return solde;
    }

    public void setSolde(double solde) {
        this.solde = solde;
    }

    public String getTypeCompte() {
        return typeCompte;
    }

    public void setTypeCompte(String typeCompte) {
        this.typeCompte = typeCompte;
    }

    public Double getTauxInteret() {
        return tauxInteret;
    }

    public void setTauxInteret(Double tauxInteret) {
        this.tauxInteret = tauxInteret;
    }

    public Double getPlafondDecouvert() {
        return plafondDecouvert;
    }

    public void setPlafondDecouvert(Double plafondDecouvert) {
        this.plafondDecouvert = plafondDecouvert;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    @Override
    public String toString() {
        return "Compte{" +
                "idCompte=" + idCompte +
                ", numeroCompte='" + numeroCompte + '\'' +
                ", solde=" + solde +
                ", typeCompte='" + typeCompte + '\'' +
                ", tauxInteret=" + tauxInteret +
                ", plafondDecouvert=" + plafondDecouvert +
                ", dateCreation=" + dateCreation +
                ", etat='" + etat + '\'' +
                '}';
    }
}
