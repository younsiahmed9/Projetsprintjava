package Models;

import java.time.LocalDateTime;

public class Transaction {
    private int id;
    private double montant;
    private String devise;
    private LocalDateTime date;
    private TypeTransaction type;
    private StatutTransaction statut;
    private String description;

    private Integer carteSourceId;
    private Integer carteDestId;
    private CarteVirtuelle carteSource;
    private CarteVirtuelle carteDest;

    public Transaction() {
        this.date = LocalDateTime.now();
        this.statut = StatutTransaction.SUCCESS;
    }

    public Transaction(double montant, String devise, TypeTransaction type,
                       Integer carteSourceId, Integer carteDestId, String description) {
        this.montant = montant;
        this.devise = devise;
        this.type = type;
        this.carteSourceId = carteSourceId;
        this.carteDestId = carteDestId;
        this.description = description;
        this.date = LocalDateTime.now();
        this.statut = StatutTransaction.SUCCESS;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public TypeTransaction getType() { return type; }
    public void setType(TypeTransaction type) { this.type = type; }
    public void setType(String type) {
        this.type = TypeTransaction.valueOf(type);
    }

    public StatutTransaction getStatut() { return statut; }
    public void setStatut(StatutTransaction statut) { this.statut = statut; }
    public void setStatut(String statut) {
        this.statut = StatutTransaction.valueOf(statut);
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCarteSourceId() { return carteSourceId; }
    public void setCarteSourceId(Integer carteSourceId) { this.carteSourceId = carteSourceId; }

    public Integer getCarteDestId() { return carteDestId; }
    public void setCarteDestId(Integer carteDestId) { this.carteDestId = carteDestId; }

    public CarteVirtuelle getCarteSource() { return carteSource; }
    public void setCarteSource(CarteVirtuelle carteSource) { this.carteSource = carteSource; }

    public CarteVirtuelle getCarteDest() { return carteDest; }
    public void setCarteDest(CarteVirtuelle carteDest) { this.carteDest = carteDest; }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", montant=" + montant +
                ", devise='" + devise + '\'' +
                ", date=" + date +
                ", type=" + type +
                ", statut=" + statut +
                '}';
    }
}