package services;

import models.Service;
import models.Produit;
import models.Facture;
import utils.MyDataBase;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ServicePersonne {
    private Connection conn;
    private ServiceService serviceService;
    private ServiceProduit produitService;
    private ServiceFacture factureService;

    public ServicePersonne() {
        this.conn = MyDataBase.getInstance().getConnection();
        this.serviceService = new ServiceService();
        this.produitService = new ServiceProduit();
        this.factureService = new ServiceFacture();
        System.out.println("✅ ServicePersonne initialisé");
    }

    // ==================== SERVICES ====================
    public void ajouterService(Service service) throws SQLException {
        serviceService.ajouter(service);
    }

    public List<Service> getAllServices() throws SQLException {
        return serviceService.recupererTous();
    }

    public Service getServiceById(int id) throws SQLException {
        return serviceService.getById(id);
    }

    public void modifierService(Service service) throws SQLException {
        serviceService.modifier(service);
    }

    public void supprimerService(int id) throws SQLException {
        serviceService.supprimer(id);
    }

    public int compterServices() throws SQLException {
        return serviceService.recupererTous().size();
    }

    // ==================== PRODUITS ====================
    public void ajouterProduit(Produit produit) throws SQLException {
        produitService.ajouter(produit);
    }

    public List<Produit> getAllProduits() throws SQLException {
        return produitService.recupererTous();
    }

    public Produit getProduitById(int id) throws SQLException {
        return produitService.getById(id);
    }

    public void modifierProduit(Produit produit) throws SQLException {
        produitService.modifier(produit);
    }

    public void supprimerProduit(int id) throws SQLException {
        produitService.supprimer(id);
    }

    public int compterProduits() throws SQLException {
        return produitService.recupererTous().size();
    }

    public List<Produit> getProduitsDisponibles() throws SQLException {
        return produitService.getProduitsDisponibles();
    }

    // ==================== FACTURES ====================
    public void ajouterFacture(Facture facture) throws SQLException {
        factureService.ajouter(facture);
    }

    public List<Facture> getAllFactures() throws SQLException {
        return factureService.recupererToutes();
    }

    public Facture getFactureById(int id) throws SQLException {
        return factureService.getById(id);
    }

    public void modifierFacture(Facture facture) throws SQLException {
        factureService.modifier(facture);
    }

    public void supprimerFacture(int id) throws SQLException {
        factureService.supprimer(id);
    }

    public void payerFacture(int id) throws SQLException {
        factureService.payer(id);
    }

    public void annulerFacture(int id) throws SQLException {
        factureService.annuler(id);
    }

    public List<Facture> getFacturesImpayees() throws SQLException {
        return factureService.getFacturesImpayees();
    }

    public List<Facture> getFacturesPayees() throws SQLException {
        return factureService.getFacturesPayees();
    }

    public List<Facture> getFacturesEnAttente() throws SQLException {
        return factureService.getFacturesEnAttente();
    }

    public List<Facture> getFacturesAnnulees() throws SQLException {
        return factureService.getFacturesAnnulees();
    }

    public List<Facture> getFacturesParPeriode(LocalDate debut, LocalDate fin) throws SQLException {
        return factureService.getFacturesParPeriode(debut, fin);
    }

    public List<Facture> getFacturesEnRetard() throws SQLException {
        return factureService.getFacturesEnRetard();
    }

    public int compterFactures() throws SQLException {
        return factureService.compterToutes();
    }

    public int compterFacturesParStatut(String statut) throws SQLException {
        return factureService.compterParStatut(statut);
    }

    // ==================== CALCULS ====================
    public BigDecimal getTotalImpayees() throws SQLException {
        return factureService.getTotalImpayees();
    }

    public BigDecimal getTotalPayees() throws SQLException {
        return factureService.getTotalPayees();
    }

    public BigDecimal getTotalGeneral() throws SQLException {
        return factureService.getTotalGeneral();
    }

    // ==================== STATISTIQUES ====================
    public String getResume() throws SQLException {
        return "📊 " + compterServices() + " services | " +
                compterProduits() + " produits | " +
                compterFactures() + " factures";
    }

    public String getStatistiquesCompletes() throws SQLException {
        return String.format(
                "📦 SERVICES: %d\n" +
                        "🏷️ PRODUITS: %d\n" +
                        "📄 FACTURES: %d (Payées: %d, Impayées: %d, Attente: %d, Annulées: %d)\n" +
                        "💰 Total: %.2f DT (Payé: %.2f DT, Impayé: %.2f DT)",
                compterServices(), compterProduits(),
                compterFactures(),
                compterFacturesParStatut("payee"),
                compterFacturesParStatut("impayee"),
                compterFacturesParStatut("en_attente"),
                compterFacturesParStatut("annulee"),
                getTotalGeneral(), getTotalPayees(), getTotalImpayees()
        );
    }
}