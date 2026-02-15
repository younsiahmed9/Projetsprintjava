package Models;

import java.time.LocalDateTime;

public class Dossier {
    private int id;
    private String nom;
    private String description;
    private LocalDateTime createdAt;

    public Dossier() {}

    public Dossier(int id, String nom, String description, LocalDateTime createdAt) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Dossier(String nom, String description) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return nom;
    }
}

