package models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Service {
    private int idService;
    private String nomService;
    private TypeService typeService;
    private BigDecimal tarif;
    private Frequence frequence;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private StatutService statut;
    private Integer idProduit;  // AJOUTEZ CETTE LIGNE

    public enum TypeService {
        abonnement, facture
    }

    public enum Frequence {
        mensuel, annuel
    }

    public enum StatutService {
        actif, suspendu, expire
    }

    // Constructeur vide
    public Service() {}

    // Constructeur complet avec idProduit
    public Service(int idService, String nomService, TypeService typeService,
                   BigDecimal tarif, Frequence frequence, LocalDate dateDebut,
                   LocalDate dateFin, StatutService statut, Integer idProduit) {
        this.idService = idService;
        this.nomService = nomService;
        this.typeService = typeService;
        this.tarif = tarif;
        this.frequence = frequence;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.idProduit = idProduit;  // AJOUTEZ CETTE LIGNE
    }

    // Constructeur sans idProduit
    public Service(int idService, String nomService, TypeService typeService,
                   BigDecimal tarif, Frequence frequence, LocalDate dateDebut,
                   LocalDate dateFin, StatutService statut) {
        this(idService, nomService, typeService, tarif, frequence, dateDebut, dateFin, statut, null);
    }

    // Getters et Setters
    public int getIdService() { return idService; }
    public void setIdService(int idService) { this.idService = idService; }

    public String getNomService() { return nomService; }
    public void setNomService(String nomService) { this.nomService = nomService; }

    public TypeService getTypeService() { return typeService; }
    public void setTypeService(TypeService typeService) { this.typeService = typeService; }

    public BigDecimal getTarif() { return tarif; }
    public void setTarif(BigDecimal tarif) { this.tarif = tarif; }

    public Frequence getFrequence() { return frequence; }
    public void setFrequence(Frequence frequence) { this.frequence = frequence; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public StatutService getStatut() { return statut; }
    public void setStatut(StatutService statut) { this.statut = statut; }

    // AJOUTEZ CES DEUX MÉTHODES
    public Integer getIdProduit() { return idProduit; }
    public void setIdProduit(Integer idProduit) { this.idProduit = idProduit; }

    @Override
    public String toString() {
        return "Service{" + "id=" + idService + ", nom='" + nomService + "', type=" + typeService + "}";
    }
}