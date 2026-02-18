package services;

import models.Service;
import models.Produit;
import java.sql.SQLException;
import java.util.List;

public class ServicePersonne {
    private ServiceService serviceService;
    private ServiceProduit produitService;

    public ServicePersonne() {
        this.serviceService = new ServiceService();
        this.produitService = new ServiceProduit();
    }

    // ===== SERVICES =====
    public void ajouterService(Service service) throws SQLException {
        serviceService.ajouter(service);
    }

    public List<Service> afficherTousServices() throws SQLException {
        return serviceService.recuperer();
    }

    public void modifierService(Service service) throws SQLException {
        serviceService.modifier(service);
    }

    public void supprimerService(int id) throws SQLException {
        serviceService.supprimer(id);
    }

    public Service getServiceById(int id) throws SQLException {
        return serviceService.getById(id);
    }

    // ===== PRODUITS =====
    public void ajouterProduit(Produit produit) throws SQLException {
        produitService.ajouter(produit);
    }

    public List<Produit> afficherTousProduits() throws SQLException {
        return produitService.recuperer();
    }

    public List<Produit> getProduitsDisponibles() throws SQLException {
        return produitService.getProduitsDisponibles();
    }

    public void modifierProduit(Produit produit) throws SQLException {
        produitService.modifier(produit);
    }

    public void supprimerProduit(int id) throws SQLException {
        produitService.supprimer(id);
    }

    public Produit getProduitById(int id) throws SQLException {
        return produitService.getById(id);
    }
}