package Models;

public class Categorie {
    private int id;
    private String nom;
    private String description;
    private double budgetMax;

    public Categorie() {
    }

    public Categorie(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }

    public Categorie(int id, String nom, String description, double budgetMax) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.budgetMax = budgetMax;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(double budgetMax) {
        this.budgetMax = budgetMax;
    }

    @Override
    public String toString() {
        return nom;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Categorie categorie = (Categorie) obj;
        return id == categorie.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
