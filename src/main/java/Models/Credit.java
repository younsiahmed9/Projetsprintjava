package Models;

import java.time.LocalDate;

public class Credit {
    private int idCredit;
    private double montant;
    private double tauxInteret;
    private int dureeMois;
    private double mensualite;
    private LocalDate dateDebut;
    private String statut;
    private Compte compte;

    public Credit() {
    }

    public Credit(double montant, double tauxInteret, int dureeMois, double mensualite, LocalDate dateDebut, String statut, Compte compte) {
        this.montant = montant;
        this.tauxInteret = tauxInteret;
        this.dureeMois = dureeMois;
        this.mensualite = mensualite;
        this.dateDebut = dateDebut;
        this.statut = statut;
        this.compte = compte;
    }

    public Credit(double montant, double tauxInteret, int dureeMois, double mensualite, LocalDate dateDebut, String statut, Compte compte, int idCredit) {
        this.montant = montant;
        this.tauxInteret = tauxInteret;
        this.dureeMois = dureeMois;
        this.mensualite = mensualite;
        this.dateDebut = dateDebut;
        this.statut = statut;
        this.compte = compte;
        this.idCredit = idCredit;
    }

    public Credit(int idCredit) {
        this.idCredit = idCredit;
    }

    public int getIdCredit() {
        return idCredit;
    }

    public void setIdCredit(int idCredit) {
        this.idCredit = idCredit;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public double getTauxInteret() {
        return tauxInteret;
    }

    public void setTauxInteret(double tauxInteret) {
        this.tauxInteret = tauxInteret;
    }

    public int getDureeMois() {
        return dureeMois;
    }

    public void setDureeMois(int dureeMois) {
        this.dureeMois = dureeMois;
    }

    public double getMensualite() {
        return mensualite;
    }

    public void setMensualite(double mensualite) {
        this.mensualite = mensualite;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
    }
    @Override
    public String toString() {
        return "Credit{" +
                "idCredit=" + idCredit +
                ", montant=" + montant +
                ", tauxInteret=" + tauxInteret +
                ", dureeMois=" + dureeMois +
                ", mensualite=" + mensualite +
                ", dateDebut=" + dateDebut +
                ", statut='" + statut + '\'' +
                ", compte=" + compte +
                '}';
    }
}
