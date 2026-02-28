package models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Service {
    private Integer idService;
    private String nomService;
    private String typeService;
    private BigDecimal tarif;
    private String frequence;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statut;

    // Constructeurs
    public Service() {}

    public Service(Integer idService, String nomService, String typeService, BigDecimal tarif,
                   String frequence, LocalDate dateDebut, LocalDate dateFin, String statut) {
        this.idService = idService;
        // Utilisation des setters pour la validation dès la construction
        setNomService(nomService);
        setTypeService(typeService);
        setTarif(tarif);
        setFrequence(frequence);
        setDateDebut(dateDebut);
        setDateFin(dateFin);
        setStatut(statut);
    }

    // Getters et Setters
    public Integer getIdService() { return idService; }
    public void setIdService(Integer idService) { this.idService = idService; }

    public String getNomService() { return nomService; }
    public void setNomService(String nomService) {
        if (nomService == null || nomService.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du service ne peut pas être vide.");
        }
        this.nomService = nomService;
    }

    public String getTypeService() { return typeService; }
    public void setTypeService(String typeService) {
        if (typeService == null || typeService.trim().isEmpty()) {
            throw new IllegalArgumentException("Le type de service ne peut pas être vide.");
        }
        this.typeService = typeService;
    }

    public BigDecimal getTarif() { return tarif; }
    public void setTarif(BigDecimal tarif) {
        if (tarif == null || tarif.signum() < 0) {
            throw new IllegalArgumentException("Le tarif ne peut pas être nul ou négatif.");
        }
        this.tarif = tarif;
    }

    public String getFrequence() { return frequence; }
    public void setFrequence(String frequence) {
        if (frequence == null || frequence.trim().isEmpty()) {
            throw new IllegalArgumentException("La fréquence ne peut pas être vide.");
        }
        this.frequence = frequence;
    }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) {
        if (dateDebut == null) {
            throw new IllegalArgumentException("La date de début ne peut pas être nulle.");
        }
        if (this.dateFin != null && dateDebut.isAfter(this.dateFin)) {
            throw new IllegalArgumentException("La date de début ne peut pas être postérieure à la date de fin.");
        }
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) {
        if (dateFin == null) {
            throw new IllegalArgumentException("La date de fin ne peut pas être nulle.");
        }
        if (this.dateDebut != null && dateFin.isBefore(this.dateDebut)) {
            throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début.");
        }
        this.dateFin = dateFin;
    }

    public String getStatut() { return statut; }
    public void setStatut(String statut) {
        if (statut == null || statut.trim().isEmpty()) {
            throw new IllegalArgumentException("Le statut ne peut pas être vide.");
        }
        this.statut = statut;
    }

    @Override
    public String toString() {
        return nomService;
    }
}