package Services;

import Models.Document;
import Models.Echeance;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service professionnel pour la gestion des alertes de documents
 */
public class AlertService {
    private final ServiceEcheance echeanceService;
    private final ServiceDocument documentService;

    public AlertService() throws SQLException {
        this.echeanceService = new ServiceEcheance();
        this.documentService = new ServiceDocument();
    }

    /**
     * Génère des alertes pour tous les documents approchant d'une date limite
     */
    public void generateAlerts() throws SQLException {
        List<Document> documents = documentService.findAll();
        LocalDate today = LocalDate.now();

        for (Document doc : documents) {
            LocalDate deadline = doc.getDateLimitePaiement();
            if (deadline == null)
                continue;

            // Vérifier si une alerte existe déjà pour ce document
            List<Echeance> existing = echeanceService.findByDocument(doc.getId());
            boolean activeAlertExists = existing.stream()
                    .anyMatch(e -> !e.getStatut().equals("VUE"));

            if (!activeAlertExists) {
                long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, deadline);

                // Seuils : 7, 3, 1, 0 jours
                if (daysUntil <= 7) {
                    Echeance alert = new Echeance();
                    alert.setDocument(doc);
                    alert.setDateEcheance(deadline);
                    alert.setTypeEcheance(determineType(doc));
                    alert.setDescription("Échéance proche pour le document: " + doc.getTitre());
                    alert.setStatut("NON_VUE");
                    alert.setNotifie(false);
                    alert.setDateCreationAlerte(today);

                    echeanceService.add(alert);
                }
            } else {
                // Mettre à jour les alertes existantes si elles sont expirées
                for (Echeance e : existing) {
                    if (e.getStatut().equals("NON_VUE") && e.isEchue()) {
                        e.setStatut("EXPIREE");
                        echeanceService.update(e);
                    }
                }
            }
        }
    }

    private String determineType(Document doc) {
        if (doc.getCategorie() != null) {
            String cat = doc.getCategorie().getNom().toLowerCase();
            if (cat.contains("facture"))
                return "Facture";
            if (cat.contains("contrat"))
                return "Contrat";
            if (cat.contains("assurance"))
                return "Assurance";
            if (cat.contains("abonnement"))
                return "Abonnement";
        }
        return "Document";
    }

    /**
     * Récupère les alertes actives (NON_VUE ou EXPIREE)
     */
    public List<Echeance> getActiveAlerts() throws SQLException {
        return echeanceService.findAll().stream()
                .filter(e -> !e.getStatut().equals("VUE"))
                .collect(Collectors.toList());
    }

    /**
     * Marque une alerte comme vue
     */
    public void markAsSeen(int alertId) throws SQLException {
        echeanceService.markAsCompleted(alertId);
    }
}
