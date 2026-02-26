package Services;

import Models.ScheduledTransfer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledTransferExecutor {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledTransferService scheduledService = new ScheduledTransferService();
    private final CarteVirtuelleService carteService = new CarteVirtuelleService();
    private final EmailService emailService = new EmailService();

    public void start() {
        scheduler.scheduleAtFixedRate(this::processDueTransfers, 0, 1, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void processDueTransfers() {
        System.out.println("🔍 Vérification des transferts programmés... " + LocalDateTime.now());
        List<ScheduledTransfer> due = scheduledService.getPendingDue();
        for (ScheduledTransfer st : due) {
            try {
                scheduledService.updateStatus(st.getId(), "PROCESSING", st.getAttempts(), null);
                boolean success = carteService.transferer(st.getFromCardId(), st.getToCardId(), st.getAmount());
                st.setAttempts(st.getAttempts() + 1);
                if (success) {
                    scheduledService.updateStatus(st.getId(), "COMPLETED", st.getAttempts(), null);
                    emailService.sendTransferNotification(st, true, null);
                } else {
                    String error = "Solde insuffisant";
                    scheduledService.updateStatus(st.getId(), "FAILED", st.getAttempts(), error);
                    emailService.sendTransferNotification(st, false, error);
                }
            } catch (Exception e) {
                e.printStackTrace();
                String error = "Exception: " + e.getMessage();
                scheduledService.updateStatus(st.getId(), "FAILED", st.getAttempts(), error);
                emailService.sendTransferNotification(st, false, error);
            }
        }
    }
}