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
    private double montant;
    private String contenuTexte; // Contenu texte extrait par OCR
    private java.time.LocalDate dateFacture;
    private java.time.LocalDate dateLimitePaiement;
    private String status = "DRAFT"; // DRAFT, PENDING, VALIDATED, REJECTED
    private boolean isDeleted = false;
    private LocalDateTime deletedAt;
    private String currency = "TND";
    private double originalAmount;

    public Document() {
    }

    public Document(int id, String titre, String description, String filePath, LocalDateTime uploadedAt,
            Dossier dossier) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.filePath = filePath;
        this.uploadedAt = uploadedAt;
        this.dossier = dossier;
        this.montant = 0.0;
        this.dateFacture = null;
        this.dateLimitePaiement = null;
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
        if (categorie == null) {
            throw new IllegalArgumentException("La catégorie est obligatoire");
        }
        this.categorie = categorie;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getContenuTexte() {
        return contenuTexte;
    }

    public void setContenuTexte(String contenuTexte) {
        this.contenuTexte = contenuTexte;
    }

    // Alias pour compatibilité avec les services
    public String getCheminFichier() {
        return filePath;
    }

    public void setCheminFichier(String cheminFichier) {
        this.filePath = cheminFichier;
    }

    public java.time.LocalDate getDateFacture() {
        return dateFacture;
    }

    public void setDateFacture(java.time.LocalDate dateFacture) {
        this.dateFacture = dateFacture;
    }

    public java.time.LocalDate getDateLimitePaiement() {
        return dateLimitePaiement;
    }

    public void setDateLimitePaiement(java.time.LocalDate dateLimitePaiement) {
        this.dateLimitePaiement = dateLimitePaiement;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(double originalAmount) {
        this.originalAmount = originalAmount;
    }
}
