package Models;

import java.util.Date;

public class Budget {
    private int idBudget;
    private int idUtilisateur;
    private String nomBudget;
    private double montantTotal;
    private String periode; // "mensuel" ou "annuel"
    private String statut;  // "actif" ou "cloture"
    private Date dateCreation;

    public Budget() {
    }
    public Budget(int idUtilisateur, String nomBudget, double montantTotal, String periode, Date dateCreation, String statut) {
        this.idUtilisateur = idUtilisateur;
        this.nomBudget = nomBudget;
        this.montantTotal = montantTotal;
        this.periode = periode;
        this.dateCreation = dateCreation;
        this.statut = statut;
    }

    // --- Getters & Setters ---
    public int getIdBudget() { return idBudget; }
    public void setIdBudget(int idBudget) { this.idBudget = idBudget; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getNomBudget() { return nomBudget; }
    public void setNomBudget(String nomBudget) { this.nomBudget = nomBudget; }

    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }

    public String getPeriode() { return periode; }
    public void setPeriode(String periode) { this.periode = periode; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }

    @Override public String toString() { return "Budget{" + "idBudget=" + idBudget + ", idUtilisateur=" + idUtilisateur + ", nomBudget='" + nomBudget + '\'' + ", montantTotal=" + montantTotal + ", periode='" + periode + '\'' + ", statut='" + statut + '\'' + ", dateCreation=" + dateCreation + '}'; }
}
