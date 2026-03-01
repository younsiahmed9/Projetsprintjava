package models;

import java.math.BigDecimal;

public class Carte {
    private int id;
    private String nom;
    private String type;
    private BigDecimal prix;
    private String statut;

    public Carte(int id, String nom, String type, double prix, String statut) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.prix = BigDecimal.valueOf(prix);
        this.statut = statut;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getType() { return type; }
    public BigDecimal getPrix() { return prix; }
    public String getStatut() { return statut; }

    public boolean estExpiree() {
        return statut != null && (statut.equalsIgnoreCase("expire") ||
                statut.equalsIgnoreCase("inactif"));
    }

    public boolean estCarte() {
        return type != null && type.toLowerCase().contains("carte");
    }
}