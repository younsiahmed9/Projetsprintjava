package Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Portefeuille {
    private int id;
    private String nom;
    private double soldeTotal;
    private String devisePrincipale;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<CarteVirtuelle> cartes = new ArrayList<>();

    public Portefeuille() {}

    public Portefeuille(String nom, double soldeTotal, String devisePrincipale) {
        this.nom = nom;
        this.soldeTotal = soldeTotal;
        this.devisePrincipale = devisePrincipale;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public double getSoldeTotal() { return soldeTotal; }
    public void setSoldeTotal(double soldeTotal) { this.soldeTotal = soldeTotal; }

    public String getDevisePrincipale() { return devisePrincipale; }
    public void setDevisePrincipale(String devisePrincipale) { this.devisePrincipale = devisePrincipale; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<CarteVirtuelle> getCartes() { return cartes; }
    public void setCartes(List<CarteVirtuelle> cartes) { this.cartes = cartes; }

    @Override
    public String toString() {
        return "Portefeuille{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", soldeTotal=" + soldeTotal +
                ", devisePrincipale='" + devisePrincipale + '\'' +
                '}';
    }
}