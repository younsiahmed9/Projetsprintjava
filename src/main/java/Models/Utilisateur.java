package Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Utilisateur {
    private int id;
    private String email;
    private String nom;
    private String prenom;
    private String password;
    private String role; // "admin" ou "user"
    private double solde;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<Portefeuille> portefeuilles = new ArrayList<>();

    // Constructeurs
    public Utilisateur() {
        this.role = "user"; // Par défaut
    }

    public Utilisateur(String email, String nom, String prenom, String password, String role, double solde) {
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.password = password;
        this.role = role != null ? role : "user";
        this.solde = solde;
    }

    // Backward-compatible constructor
    public Utilisateur(String email, String nom, String prenom, String password, double solde) {
        this(email, nom, prenom, password, "user", solde);
    }

    // Backward-compatible constructor
    public Utilisateur(String email, String nom, String prenom, double solde) {
        this(email, nom, prenom, null, "user", solde);
    }

    // Vérifie si l'utilisateur est admin
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(this.role);
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

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

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
                ", role='" + role + '\'' +
                ", solde=" + solde +
                '}';
    }
}