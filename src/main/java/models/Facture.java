package models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Facture {
    private Integer idFacture;
    private String numeroFacture;
    private BigDecimal montant;
    private LocalDate dateFacture;
    private LocalDate dateEcheance;
    private Integer idService;
    private Integer idProduit;
    private String statut;

    // Champs pour l'envoi (ajoutés une seule fois)
    private String emailClient;
    private String nomClient;
    private String adresseClient;
    private String telephoneClient;
    private LocalDateTime dateEnvoi;
    private String cheminPDF;

    private Service service;
    private Produit produit;

    // Constructeurs
    public Facture() {}

    public Facture(Integer idFacture, String numeroFacture, BigDecimal montant, LocalDate dateFacture,
                   LocalDate dateEcheance, Integer idService, Integer idProduit, String statut) {
        this.idFacture = idFacture;
        this.numeroFacture = numeroFacture;
        this.montant = montant;
        this.dateFacture = dateFacture;
        this.dateEcheance = dateEcheance;
        this.idService = idService;
        this.idProduit = idProduit;
        this.statut = statut;
    }

    // Getters et Setters
    public Integer getIdFacture() { return idFacture; }
    public void setIdFacture(Integer idFacture) { this.idFacture = idFacture; }

    public String getNumeroFacture() { return numeroFacture; }
    public void setNumeroFacture(String numeroFacture) { this.numeroFacture = numeroFacture; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public LocalDate getDateFacture() { return dateFacture; }
    public void setDateFacture(LocalDate dateFacture) { this.dateFacture = dateFacture; }

    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.dateEcheance = dateEcheance; }

    public Integer getIdService() { return idService; }
    public void setIdService(Integer idService) { this.idService = idService; }

    public Integer getIdProduit() { return idProduit; }
    public void setIdProduit(Integer idProduit) { this.idProduit = idProduit; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    // Nouveaux champs
    public String getEmailClient() { return emailClient; }
    public void setEmailClient(String emailClient) { this.emailClient = emailClient; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public String getAdresseClient() { return adresseClient; }
    public void setAdresseClient(String adresseClient) { this.adresseClient = adresseClient; }

    public String getTelephoneClient() { return telephoneClient; }
    public void setTelephoneClient(String telephoneClient) { this.telephoneClient = telephoneClient; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public String getCheminPDF() { return cheminPDF; }
    public void setCheminPDF(String cheminPDF) { this.cheminPDF = cheminPDF; }

    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }

    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }
}