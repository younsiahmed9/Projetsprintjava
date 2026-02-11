package Models;

import java.util.Date;

public class Depense {
    private int idDepense;
    private int idUtilisateur;
    private int idBudget; // lien vers Budget
    private String categorie;
    private double montant;
    private Date dateDepense;
    private String description;
    private String modePaiement; // "carte", "virement", "cash"

    // --- Constructeur par défaut
    public Depense() {}

    // --- Constructeur paramétré complet
    public Depense(int idDepense, int idUtilisateur, int idBudget,
                   String categorie, double montant, Date dateDepense,
                   String description, String modePaiement) {
        this.idDepense = idDepense;
        this.idUtilisateur = idUtilisateur;
        this.idBudget = idBudget;
        this.categorie = categorie;
        this.montant = montant;
        this.dateDepense = dateDepense;   // ⚠️ bien initialisé
        this.description = description;
        this.modePaiement = modePaiement;
    }

    // --- Getters & Setters ---
    public int getIdDepense() { return idDepense; }
    public void setIdDepense(int idDepense) { this.idDepense = idDepense; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public int getIdBudget() { return idBudget; }
    public void setIdBudget(int idBudget) { this.idBudget = idBudget; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public Date getDateDepense() { return dateDepense; }
    public void setDateDepense(Date dateDepense) { this.dateDepense = dateDepense; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }

    // --- toString() pour affichage
    @Override
    public String toString() {
        return "Depense{" +
                "idDepense=" + idDepense +
                ", idUtilisateur=" + idUtilisateur +
                ", idBudget=" + idBudget +
                ", categorie='" + categorie + '\'' +
                ", montant=" + montant +
                ", dateDepense=" + dateDepense +
                ", description='" + description + '\'' +
                ", modePaiement='" + modePaiement + '\'' +
                '}';
    }
}
