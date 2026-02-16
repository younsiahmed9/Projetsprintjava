package Models;

public class Categorie {
    private int id;
    private String nom;
    private String description;

    public Categorie() {}

    public Categorie(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }

    public Categorie(int id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
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


    @Override
    public String toString() {
        return nom;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Categorie categorie = (Categorie) obj;
        return id == categorie.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

