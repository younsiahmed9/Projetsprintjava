package Services;

import Models.Transaction;
import Models.Utilisateur;
import Services.TransactionService;

import java.time.LocalDate;
import java.util.List;

public class TransferFeeService {

    // Taux de frais standard
    private static final double STANDARD_FEE_PERCENTAGE = 2.0; // 2%

    // Taux de frais après réduction (5+ transferts par jour)
    private static final double DISCOUNT_FEE_PERCENTAGE = 1.0; // 1%

    // Seuil pour obtenir la réduction (nombre de transferts par jour)
    private static final int DAILY_TRANSFER_THRESHOLD = 5;

    private final TransactionService transactionService = new TransactionService();

    /**
     * Calcule les frais de transfert pour un montant donné
     * @param montant Le montant du transfert
     * @param utilisateurId L'ID de l'utilisateur
     * @return Le montant des frais à appliquer
     */
    public double calculerFrais(double montant, int utilisateurId) {
        double tauxFrais = getTauxFrais(utilisateurId);
        return montant * (tauxFrais / 100);
    }

    /**
     * Calcule le montant total à débiter (montant + frais)
     */
    public double calculerMontantTotal(double montant, int utilisateurId) {
        return montant + calculerFrais(montant, utilisateurId);
    }

    /**
     * Détermine le taux de frais applicable basé sur le nombre de transferts du jour
     */
    public double getTauxFrais(int utilisateurId) {
        int transfertsAujourdhui = compterTransfertsAujourdhui(utilisateurId);

        if (transfertsAujourdhui >= DAILY_TRANSFER_THRESHOLD) {
            System.out.println("✅ Réduction appliquée! " + transfertsAujourdhui +
                    " transferts aujourd'hui → frais à " + DISCOUNT_FEE_PERCENTAGE + "%");
            return DISCOUNT_FEE_PERCENTAGE;
        } else {
            System.out.println("ℹ️ " + transfertsAujourdhui + " transferts aujourd'hui, " +
                    (DAILY_TRANSFER_THRESHOLD - transfertsAujourdhui) +
                    " transferts restants pour obtenir la réduction (1%)");
            return STANDARD_FEE_PERCENTAGE;
        }
    }

    /**
     * Compte le nombre de transferts effectués par l'utilisateur aujourd'hui
     */
    private int compterTransfertsAujourdhui(int utilisateurId) {
        List<Transaction> toutesTransactions = transactionService.afficherTous();

        LocalDate aujourdhui = LocalDate.now();

        return (int) toutesTransactions.stream()
                .filter(t -> t.getType() == Models.TypeTransaction.TRANSFERT)
                .filter(t -> t.getStatut() == Models.StatutTransaction.SUCCESS)
                .filter(t -> t.getDate().toLocalDate().equals(aujourdhui))
                .filter(t -> {
                    // Vérifier si l'utilisateur est impliqué dans la transaction
                    // Note: Cette logique suppose que vous pouvez récupérer l'utilisateur à partir de la carte
                    try {
                        CarteVirtuelleService carteService = new CarteVirtuelleService();
                        Models.CarteVirtuelle carteSource = carteService.afficherParId(t.getCarteSourceId());
                        if (carteSource != null) {
                            PortefeuilleService portefeuilleService = new PortefeuilleService();
                            Models.Portefeuille portefeuille = portefeuilleService.afficherParId(carteSource.getPortefeuilleId());
                            return portefeuille != null && portefeuille.getUtilisateurId() == utilisateurId;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                })
                .count();
    }

    /**
     * Version simplifiée qui utilise le comptage direct si disponible
     */
    public int getNombreTransfertsAujourdhui(int utilisateurId) {
        return compterTransfertsAujourdhui(utilisateurId);
    }

    /**
     * Retourne un message d'information sur les frais
     */
    public String getFeesInfoMessage(int utilisateurId) {
        int transfertsAujourdhui = compterTransfertsAujourdhui(utilisateurId);
        double tauxActuel = getTauxFrais(utilisateurId);
        int restants = DAILY_TRANSFER_THRESHOLD - transfertsAujourdhui;

        StringBuilder message = new StringBuilder();
        message.append("💰 Frais de transfert: ").append(tauxActuel).append("%");

        if (transfertsAujourdhui < DAILY_TRANSFER_THRESHOLD) {
            message.append("\n📊 ").append(transfertsAujourdhui).append("/").append(DAILY_TRANSFER_THRESHOLD)
                    .append(" transferts aujourd'hui. Encore ").append(restants)
                    .append(" transfert(s) pour obtenir 1% de frais!");
        } else {
            message.append("\n🎉 Félicitations! Vous bénéficiez du tarif réduit (1%)!");
        }

        return message.toString();
    }
}