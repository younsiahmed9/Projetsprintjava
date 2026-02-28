package Services;

import Models.CarteVirtuelle;
import Models.ScheduledTransfer;
import Models.Utilisateur;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledTransferExecutor {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledTransferService scheduledService = new ScheduledTransferService();
    private final CarteVirtuelleService carteService = new CarteVirtuelleService();
    private final JavaMailService emailService = new JavaMailService(); // ← CHANGÉ
    private final UtilisateurService utilisateurService = new UtilisateurService();

    public void start() {
        scheduler.scheduleAtFixedRate(this::processDueTransfers, 0, 1, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void processDueTransfers() {
        System.out.println("🔍 Vérification des transferts programmés... " + LocalDateTime.now());
        try {
            List<ScheduledTransfer> due = scheduledService.getPendingDue();
            System.out.println("📊 " + due.size() + " transferts à traiter");

            for (ScheduledTransfer st : due) {
                processSingleTransfer(st);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur dans processDueTransfers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processSingleTransfer(ScheduledTransfer st) {
        try {
            System.out.println("⏳ Traitement du transfert programmé ID: " + st.getId());

            scheduledService.updateStatus(st.getId(), "PROCESSING", st.getAttempts(), null);
            st.setAttempts(st.getAttempts() + 1);

            CarteVirtuelle source = carteService.afficherParId(st.getFromCardId());
            CarteVirtuelle dest = carteService.afficherParId(st.getToCardId());

            if (source == null || dest == null) {
                String error = "Carte source ou destination introuvable";
                scheduledService.updateStatus(st.getId(), "FAILED", st.getAttempts(), error);
                return;
            }

            if (source.getSolde() < st.getAmount()) {
                String error = "Solde insuffisant";
                scheduledService.updateStatus(st.getId(), "FAILED", st.getAttempts(), error);
                return;
            }

            boolean success = carteService.transferer(st.getFromCardId(), st.getToCardId(), st.getAmount());

            Utilisateur user = utilisateurService.getUserByCardId(source.getId());

            if (success) {
                scheduledService.updateStatus(st.getId(), "COMPLETED", st.getAttempts(), null);
                System.out.println("✅ Transfert programmé réussi ID: " + st.getId());

                // ✅ Envoyer email avec JavaMail
                if (user != null) {
                    String subject = "✅ Transfert programmé exécuté - FinTrack";
                    String content = "Bonjour " + user.getPrenom() + " " + user.getNom() + ",\n\n" +
                            "Votre transfert programmé a été exécuté avec succès.\n\n" +
                            "Détails du transfert :\n" +
                            "Montant : " + st.getAmount() + " DT\n" +
                            "Date d'exécution : " + LocalDateTime.now() + "\n\n" +
                            "---\n" +
                            "FinTrack - Application de gestion financière";

                    emailService.sendEmail(user.getEmail(), subject, content);
                }

            } else {
                String error = "Échec du transfert";
                scheduledService.updateStatus(st.getId(), "FAILED", st.getAttempts(), error);
                System.err.println("❌ Transfert programmé échoué ID: " + st.getId());

                // ✅ Envoyer email d'échec
                if (user != null) {
                    String subject = "❌ Échec de transfert programmé - FinTrack";
                    String content = "Bonjour " + user.getPrenom() + " " + user.getNom() + ",\n\n" +
                            "Votre transfert programmé n'a pas pu être exécuté.\n\n" +
                            "Raison : " + error + "\n\n" +
                            "---\n" +
                            "FinTrack - Application de gestion financière";

                    emailService.sendEmail(user.getEmail(), subject, content);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            String error = "Exception: " + e.getMessage();
            scheduledService.updateStatus(st.getId(), "FAILED", st.getAttempts(), error);
        }
    }
}