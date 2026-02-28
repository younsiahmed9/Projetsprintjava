package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import models.Facture;
import models.Produit;
import models.Service;
import services.ServiceFacture;
import services.ServiceProduit;
import services.ServiceService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    // ==================== BOUTONS ====================
    @FXML private Button btnActualiser;
    @FXML private Button btnExporter;

    // ==================== CARTES DE RÉSUMÉ ====================
    @FXML private Label totalProduitsLabel;
    @FXML private Label totalServicesLabel;
    @FXML private Label totalFacturesLabel;
    @FXML private Label montantTotalFacturesLabel;

    // ==================== STATISTIQUES AVANCÉES FACTURES ====================
    @FXML private Label facturesMoisEnCours;
    @FXML private Label facturesMoisPrecedent;
    @FXML private Label montantMoyenFacture;
    @FXML private Label facturesEcheance;
    @FXML private Label facturesEnRetard;

    // ==================== STATISTIQUES FACTURES (barres) ====================
    @FXML private ProgressBar progressEnAttente;
    @FXML private Label labelEnAttente;
    @FXML private ProgressBar progressPayees;
    @FXML private Label labelPayees;
    @FXML private ProgressBar progressImpayees;
    @FXML private Label labelImpayees;
    @FXML private ProgressBar progressAnnulees;
    @FXML private Label labelAnnulees;

    // ==================== GRAPHIQUES ====================
    @FXML private PieChart produitsPieChart;
    @FXML private PieChart servicesPieChart;
    @FXML private PieChart facturesPieChart;

    // ==================== SERVICES ====================
    private ServiceProduit produitService;
    private ServiceService serviceService;
    private ServiceFacture factureService;

    // ==================== DONNÉES ====================
    private ObservableList<Produit> produitList = FXCollections.observableArrayList();
    private ObservableList<Service> serviceList = FXCollections.observableArrayList();
    private ObservableList<Facture> factureList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialisation des services
            produitService = new ServiceProduit();
            serviceService = new ServiceService();
            factureService = new ServiceFacture();

            // Chargement des données
            chargerDonnees();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation du DashboardController", e);
            afficherErreur("Erreur de connexion", "Impossible de charger les données: " + e.getMessage());
        }
    }

    /**
     * Charge toutes les données depuis la base de données
     */
    private void chargerDonnees() {
        try {
            // Récupération des données
            produitList.setAll(produitService.recupererTous());
            serviceList.setAll(serviceService.recupererTous());

            // Récupération des factures
            try {
                factureList.setAll(factureService.recupererToutesLesFactures());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erreur lors du chargement des factures", e);
                factureList.clear();
            }

            // Mise à jour de l'affichage
            mettreAJourStatistiques();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des données", e);
            afficherErreur("Erreur", "Impossible de charger les données: " + e.getMessage());
        }
    }

    /**
     * Met à jour toutes les statistiques du dashboard
     */
    private void mettreAJourStatistiques() {
        // === CARTES DE RÉSUMÉ ===
        totalProduitsLabel.setText(String.valueOf(produitList.size()));
        totalServicesLabel.setText(String.valueOf(serviceList.size()));
        totalFacturesLabel.setText(String.valueOf(factureList.size()));

        // Calcul du montant total des factures
        double montantTotal = factureList.stream()
                .mapToDouble(f -> f.getMontant() != null ? f.getMontant().doubleValue() : 0)
                .sum();
        montantTotalFacturesLabel.setText(String.format("%.2f €", montantTotal));

        // === STATISTIQUES AVANCÉES DES FACTURES ===
        mettreAJourStatistiquesAvanceesFactures();

        // === STATISTIQUES DE BASE DES FACTURES (barres) ===
        mettreAJourStatistiquesFactures();

        // === GRAPHIQUES ===
        mettreAJourGraphiqueProduits();
        mettreAJourGraphiqueServices();
        mettreAJourGraphiqueFactures();
    }

    /**
     * Met à jour les statistiques avancées des factures
     */
    private void mettreAJourStatistiquesAvanceesFactures() {
        if (factureList.isEmpty()) {
            if (facturesMoisEnCours != null) facturesMoisEnCours.setText("0");
            if (facturesMoisPrecedent != null) facturesMoisPrecedent.setText("0");
            if (montantMoyenFacture != null) montantMoyenFacture.setText("0 €");
            if (facturesEcheance != null) facturesEcheance.setText("0");
            if (facturesEnRetard != null) facturesEnRetard.setText("0");
            return;
        }

        LocalDate aujourdhui = LocalDate.now();
        LocalDate debutMois = aujourdhui.withDayOfMonth(1);
        LocalDate debutMoisPrecedent = debutMois.minusMonths(1);
        LocalDate finMoisPrecedent = debutMois.minusDays(1);

        // Factures du mois en cours
        long facturesMois = factureList.stream()
                .filter(f -> f.getDateFacture() != null &&
                        !f.getDateFacture().isBefore(debutMois) &&
                        !f.getDateFacture().isAfter(aujourdhui))
                .count();

        // Factures du mois précédent
        long facturesMoisPrev = factureList.stream()
                .filter(f -> f.getDateFacture() != null &&
                        !f.getDateFacture().isBefore(debutMoisPrecedent) &&
                        !f.getDateFacture().isAfter(finMoisPrecedent))
                .count();

        // Montant moyen des factures
        double montantMoyen = factureList.stream()
                .filter(f -> f.getMontant() != null)
                .mapToDouble(f -> f.getMontant().doubleValue())
                .average()
                .orElse(0);

        // Factures à échéance (dans les 7 prochains jours)
        long echeanceProche = factureList.stream()
                .filter(f -> f.getDateEcheance() != null &&
                        !f.getDateEcheance().isBefore(aujourdhui) &&
                        f.getDateEcheance().isBefore(aujourdhui.plusDays(7)) &&
                        !"Payée".equalsIgnoreCase(f.getStatut()))
                .count();

        // Factures en retard
        long enRetard = factureList.stream()
                .filter(f -> f.getDateEcheance() != null &&
                        f.getDateEcheance().isBefore(aujourdhui) &&
                        !"Payée".equalsIgnoreCase(f.getStatut()) &&
                        !"Annulée".equalsIgnoreCase(f.getStatut()))
                .count();

        // Mise à jour des labels
        if (facturesMoisEnCours != null)
            facturesMoisEnCours.setText(String.valueOf(facturesMois));

        if (facturesMoisPrecedent != null)
            facturesMoisPrecedent.setText(String.valueOf(facturesMoisPrev));

        if (montantMoyenFacture != null)
            montantMoyenFacture.setText(String.format("%.2f €", montantMoyen));

        if (facturesEcheance != null)
            facturesEcheance.setText(String.valueOf(echeanceProche));

        if (facturesEnRetard != null)
            facturesEnRetard.setText(String.valueOf(enRetard));
    }

    /**
     * Met à jour les barres de progression des factures
     */
    private void mettreAJourStatistiquesFactures() {
        long total = factureList.size();

        if (total == 0) {
            if (progressEnAttente != null) progressEnAttente.setProgress(0);
            if (labelEnAttente != null) labelEnAttente.setText("0%");
            if (progressPayees != null) progressPayees.setProgress(0);
            if (labelPayees != null) labelPayees.setText("0%");
            if (progressImpayees != null) progressImpayees.setProgress(0);
            if (labelImpayees != null) labelImpayees.setText("0%");
            if (progressAnnulees != null) progressAnnulees.setProgress(0);
            if (labelAnnulees != null) labelAnnulees.setText("0%");
            return;
        }

        // Compter par statut
        long enAttente = factureList.stream()
                .filter(f -> "En attente".equalsIgnoreCase(f.getStatut()) ||
                        "en attente".equalsIgnoreCase(f.getStatut()))
                .count();

        long payees = factureList.stream()
                .filter(f -> "Payée".equalsIgnoreCase(f.getStatut()) ||
                        "payée".equalsIgnoreCase(f.getStatut()) ||
                        "Payé".equalsIgnoreCase(f.getStatut()))
                .count();

        long impayees = factureList.stream()
                .filter(f -> "Impayée".equalsIgnoreCase(f.getStatut()) ||
                        "impayée".equalsIgnoreCase(f.getStatut()))
                .count();

        long annulees = factureList.stream()
                .filter(f -> "Annulée".equalsIgnoreCase(f.getStatut()) ||
                        "annulée".equalsIgnoreCase(f.getStatut()))
                .count();

        // Calcul des pourcentages
        double pctEnAttente = (enAttente * 100.0) / total;
        double pctPayees = (payees * 100.0) / total;
        double pctImpayees = (impayees * 100.0) / total;
        double pctAnnulees = (annulees * 100.0) / total;

        // Mise à jour des barres de progression
        if (progressEnAttente != null) progressEnAttente.setProgress(pctEnAttente / 100);
        if (labelEnAttente != null) labelEnAttente.setText(String.format("%.1f%%", pctEnAttente));

        if (progressPayees != null) progressPayees.setProgress(pctPayees / 100);
        if (labelPayees != null) labelPayees.setText(String.format("%.1f%%", pctPayees));

        if (progressImpayees != null) progressImpayees.setProgress(pctImpayees / 100);
        if (labelImpayees != null) labelImpayees.setText(String.format("%.1f%%", pctImpayees));

        if (progressAnnulees != null) progressAnnulees.setProgress(pctAnnulees / 100);
        if (labelAnnulees != null) labelAnnulees.setText(String.format("%.1f%%", pctAnnulees));
    }

    /**
     * Met à jour le graphique circulaire des produits par type
     */
    private void mettreAJourGraphiqueProduits() {
        if (produitList.isEmpty()) {
            if (produitsPieChart != null) {
                produitsPieChart.setData(FXCollections.observableArrayList(
                        new PieChart.Data("Aucun produit", 1)
                ));
            }
            return;
        }

        Map<String, Long> produitsParType = produitList.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getTypeProduit() != null ? p.getTypeProduit() : "Non catégorisé",
                        Collectors.counting()
                ));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        produitsParType.forEach((type, count) ->
                pieData.add(new PieChart.Data(type + " (" + count + ")", count))
        );

        if (produitsPieChart != null) {
            produitsPieChart.setData(pieData);
            produitsPieChart.setTitle("Produits par type");
        }
    }

    /**
     * Met à jour le graphique circulaire des services par type
     */
    private void mettreAJourGraphiqueServices() {
        if (serviceList.isEmpty()) {
            if (servicesPieChart != null) {
                servicesPieChart.setData(FXCollections.observableArrayList(
                        new PieChart.Data("Aucun service", 1)
                ));
            }
            return;
        }

        Map<String, Long> servicesParType = serviceList.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getTypeService() != null ? s.getTypeService() : "Non catégorisé",
                        Collectors.counting()
                ));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        servicesParType.forEach((type, count) ->
                pieData.add(new PieChart.Data(type + " (" + count + ")", count))
        );

        if (servicesPieChart != null) {
            servicesPieChart.setData(pieData);
            servicesPieChart.setTitle("Services par type");
        }
    }

    /**
     * Met à jour le graphique circulaire des factures par statut
     */
    private void mettreAJourGraphiqueFactures() {
        if (factureList.isEmpty()) {
            if (facturesPieChart != null) {
                facturesPieChart.setData(FXCollections.observableArrayList(
                        new PieChart.Data("Aucune facture", 1)
                ));
            }
            return;
        }

        Map<String, Long> facturesParStatut = factureList.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getStatut() != null ? f.getStatut() : "Non défini",
                        Collectors.counting()
                ));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        facturesParStatut.forEach((statut, count) ->
                pieData.add(new PieChart.Data(statut + " (" + count + ")", count))
        );

        if (facturesPieChart != null) {
            facturesPieChart.setData(pieData);
            facturesPieChart.setTitle("Factures par statut");
        }
    }

    // ==================== GESTIONNAIRES D'ÉVÉNEMENTS ====================

    @FXML
    private void handleActualiser(ActionEvent event) {
        chargerDonnees();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Actualisation");
        alert.setHeaderText(null);
        alert.setContentText("Données actualisées avec succès !");
        alert.showAndWait();
    }

    @FXML
    private void handleExporter(ActionEvent event) {
        // TODO: Implémenter l'export des statistiques
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export");
        alert.setHeaderText(null);
        alert.setContentText("Fonction d'export à implémenter.");
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}