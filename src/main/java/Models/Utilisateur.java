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

    // Full address fields (matching your database)
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postalCode;
    private String country;
    private boolean addressVerified;
    private int addressConfidenceScore;

    private List<Portefeuille> portefeuilles = new ArrayList<>();

    // Constructeurs
    public Utilisateur() {
        this.role = "user";
        this.addressVerified = false;
        this.addressConfidenceScore = 0;
    }

    public Utilisateur(String email, String nom, String prenom, String password, String role, double solde) {
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.password = password;
        this.role = role != null ? role : "user";
        this.solde = solde;
        this.addressVerified = false;
        this.addressConfidenceScore = 0;
    }

    // Backward-compatible constructors
    public Utilisateur(String email, String nom, String prenom, String password, double solde) {
        this(email, nom, prenom, password, "user", solde);
    }

    public Utilisateur(String email, String nom, String prenom, double solde) {
        this(email, nom, prenom, null, "user", solde);
    }

    // Vérifie si l'utilisateur est admin
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(this.role);
    }

    // Getters et Setters pour tous les champs
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

    // Address fields getters/setters
    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public boolean isAddressVerified() { return addressVerified; }
    public void setAddressVerified(boolean addressVerified) { this.addressVerified = addressVerified; }

    public int getAddressConfidenceScore() { return addressConfidenceScore; }
    public void setAddressConfidenceScore(int addressConfidenceScore) { this.addressConfidenceScore = addressConfidenceScore; }

    public List<Portefeuille> getPortefeuilles() { return portefeuilles; }
    public void setPortefeuilles(List<Portefeuille> portefeuilles) { this.portefeuilles = portefeuilles; }

    // Helper method to get full address
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressLine1 != null) sb.append(addressLine1);
        if (addressLine2 != null) sb.append(", ").append(addressLine2);
        if (city != null) sb.append(", ").append(city);
        if (postalCode != null) sb.append(" ").append(postalCode);
        if (country != null) sb.append(", ").append(country);
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", solde=" + solde +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}