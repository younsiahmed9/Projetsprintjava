package Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Utilisateur {
    private int id;
    private String email;
    private String nom;
    private String prenom;
    private double solde;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<Portefeuille> portefeuilles = new ArrayList<>();

    // Constructeurs
    public Utilisateur() {}

    public Utilisateur(String email, String nom, String prenom, double solde) {
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.solde = solde;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public double getSolde() { return solde; }
    public void setSolde(double solde) { this.solde = solde; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Portefeuille> getPortefeuilles() { return portefeuilles; }
    public void setPortefeuilles(List<Portefeuille> portefeuilles) { this.portefeuilles = portefeuilles; }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", solde=" + solde +
                '}';
    }
}