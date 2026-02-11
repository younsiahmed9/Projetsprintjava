package Models;

import java.time.LocalDateTime;

public class CarteVirtuelle {
    private int id;
    private String numeroCarte;
    private double solde;
    private double plafond;
    private TypeCarte type;
    private Devise devise;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private int portefeuilleId;
    private Portefeuille portefeuille;

    public CarteVirtuelle() {
        this.numeroCarte = genererNumeroCarte();
        this.isActive = true;
    }

    public CarteVirtuelle(double solde, double plafond, TypeCarte type, Devise devise, int portefeuilleId) {
        this.numeroCarte = genererNumeroCarte();
        this.solde = solde;
        this.plafond = plafond;
        this.type = type;
        this.devise = devise;
        this.portefeuilleId = portefeuilleId;
        this.isActive = true;
    }

    private String genererNumeroCarte() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroCarte() { return numeroCarte; }
    public void setNumeroCarte(String numeroCarte) { this.numeroCarte = numeroCarte; }

    public double getSolde() { return solde; }
    public void setSolde(double solde) { this.solde = solde; }

    public double getPlafond() { return plafond; }
    public void setPlafond(double plafond) { this.plafond = plafond; }

    public TypeCarte getType() { return type; }
    public void setType(TypeCarte type) { this.type = type; }
    public void setType(String type) {
        this.type = TypeCarte.valueOf(type);
    }

    public Devise getDevise() { return devise; }
    public void setDevise(Devise devise) { this.devise = devise; }
    public void setDevise(String devise) {
        this.devise = Devise.valueOf(devise);
    }

    public boolean isActiver() { return isActive; }
    public void setActiver(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getPortefeuilleId() { return portefeuilleId; }
    public void setPortefeuilleId(int portefeuilleId) { this.portefeuilleId = portefeuilleId; }

    public Portefeuille getPortefeuille() { return portefeuille; }
    public void setPortefeuille(Portefeuille portefeuille) { this.portefeuille = portefeuille; }

    @Override
    public String toString() {
        return "CarteVirtuelle{" +
                "id=" + id +
                ", numeroCarte='" + numeroCarte + '\'' +
                ", solde=" + solde +
                ", type=" + type +
                ", devise=" + devise +
                ", isActive=" + isActive +
                '}';
    }
}