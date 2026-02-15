package Models;

import java.time.LocalDateTime;

public class Document {
    private int id;
    private String titre;
    private String description;
    private String filePath;
    private LocalDateTime uploadedAt;
    private Dossier dossier;
    private Categorie categorie;
    private Double budget;

    public Document() {}

    public Document(int id, String titre, String description, String filePath, LocalDateTime uploadedAt, Dossier dossier) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.filePath = filePath;
        this.uploadedAt = uploadedAt;
        this.dossier = dossier;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Dossier getDossier() {
        return dossier;
    }

    public void setDossier(Dossier dossier) {
        this.dossier = dossier;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }
}

