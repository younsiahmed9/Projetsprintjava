package Models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Modèle représentant une échéance liée à un document
 */
public class Echeance {
    private int id;
    private Document document;
    private LocalDate dateEcheance;
    private String typeEcheance; // paiement, renouvellement, expiration, etc.
    private String description;
    private String statut; // NON_VUE, VUE, EXPIREE
    private boolean notifie;
    private LocalDate dateCreationAlerte;

    // Constructeurs
    public Echeance() {
        this.statut = "NON_VUE";
        this.notifie = false;
        this.dateCreationAlerte = LocalDate.now();
    }

    public Echeance(Document document, LocalDate dateEcheance, String typeEcheance, String description) {
        this.document = document;
        this.dateEcheance = dateEcheance;
        this.typeEcheance = typeEcheance;
        this.description = description;
        this.statut = "NON_VUE";
        this.notifie = false;
        this.dateCreationAlerte = LocalDate.now();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public String getTypeEcheance() {
        return typeEcheance;
    }

    public void setTypeEcheance(String typeEcheance) {
        this.typeEcheance = typeEcheance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public boolean isNotifie() {
        return notifie;
    }

    public void setNotifie(boolean notifie) {
        this.notifie = notifie;
    }

    public LocalDate getDateCreationAlerte() {
        return dateCreationAlerte;
    }

    public void setDateCreationAlerte(LocalDate dateCreationAlerte) {
        this.dateCreationAlerte = dateCreationAlerte;
    }

    // Méthodes utilitaires

    /**
     * Calcule le nombre de jours restants avant l'échéance
     */
    public long getJoursRestants() {
        if (dateEcheance == null)
            return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), dateEcheance);
    }

    /**
     * Vérifie si l'échéance est dépassée
     */
    public boolean isEchue() {
        if (dateEcheance == null)
            return false;
        return LocalDate.now().isAfter(dateEcheance) && !statut.equals("VUE");
    }

    /**
     * Vérifie si l'échéance arrive bientôt (dans les X jours)
     */
    public boolean isImminente(int jours) {
        long joursRestants = getJoursRestants();
        return joursRestants >= 0 && joursRestants <= jours;
    }

    /**
     * Obtient une description de l'urgence
     */
    public String getUrgence() {
        if (isEchue()) {
            return "🔴 EXPIREE";
        } else if (getJoursRestants() == 0) {
            return "🔥 AUJOURD'HUI";
        } else if (isImminente(1)) {
            return "🟠 TRÈS PROCHE";
        } else if (isImminente(3)) {
            return "🟡 PROCHE";
        } else if (isImminente(7)) {
            return "🔵 URGENT";
        } else {
            return "⚪ NORMAL";
        }
    }

    /**
     * Obtient une description formatée de l'échéance
     */
    public String getDescriptionComplete() {
        return String.format("%s - %s (%s) - %d jours restants",
                document != null ? document.getTitre() : "Sans document",
                typeEcheance,
                dateEcheance,
                getJoursRestants());
    }

    @Override
    public String toString() {
        return String.format("Echeance{id=%d, document='%s', date=%s, type='%s', statut='%s'}",
                id,
                document != null ? document.getTitre() : "null",
                dateEcheance,
                typeEcheance,
                statut);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Echeance echeance = (Echeance) o;
        return id == echeance.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
