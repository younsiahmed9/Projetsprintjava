package Services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import Models.ScheduledTransfer;
import Models.Utilisateur;

import java.io.IOException;

public class EmailService {
    // Replace with your actual SendGrid API key
    private static final String SENDGRID_API_KEY = "YOUR_SENDGRID_API_KEY";
    private static final String FROM_EMAIL = "noreply@fintrack.com";
    private static final String FROM_NAME = "FinTrack";

    private UtilisateurService utilisateurService = new UtilisateurService();

    public EmailService() {}

    public void sendTransferNotification(ScheduledTransfer st, boolean success, String errorMessage) {
        Utilisateur user = utilisateurService.afficherParId(st.getUserId());
        if (user == null) {
            System.err.println("❌ Utilisateur introuvable pour l'ID: " + st.getUserId());
            return;
        }

        String subject = success ? "✅ Transfert programmé effectué" : "❌ Échec de transfert programmé";
        String htmlContent = buildEmailContent(st, success, errorMessage);
        sendEmail(user.getEmail(), subject, htmlContent);
    }

    private String buildEmailContent(ScheduledTransfer st, boolean success, String errorMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: Arial, sans-serif;'>");
        sb.append("<h2 style='color: #182d88;'>FinTrack – Notification de transfert</h2>");
        sb.append("<hr/>");

        if (success) {
            sb.append("<p style='color: green; font-weight: bold;'>✅ Votre transfert programmé a été exécuté avec succès.</p>");
        } else {
            sb.append("<p style='color: red; font-weight: bold;'>❌ Échec de l'exécution de votre transfert programmé.</p>");
            sb.append("<p>Raison : ").append(errorMessage).append("</p>");
        }

        sb.append("<h3>Détails :</h3>");
        sb.append("<ul>");
        sb.append("<li>Montant : ").append(st.getAmount()).append(" DT</li>");
        sb.append("<li>Date planifiée : ").append(st.getScheduledDate()).append("</li>");
        sb.append("<li>Carte source ID : ").append(st.getFromCardId()).append("</li>");
        sb.append("<li>Carte destination ID : ").append(st.getToCardId()).append("</li>");
        sb.append("</ul>");
        sb.append("<hr/><p>Merci d'utiliser FinTrack.</p></body></html>");

        return sb.toString();
    }

    private void sendEmail(String toEmail, String subject, String htmlContent) {
        Email from = new Email(FROM_EMAIL, FROM_NAME);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("📧 Email envoyé à " + toEmail + " (statut: " + response.getStatusCode() + ")");

            // Optional: log response body for debugging
            // System.out.println("Réponse: " + response.getBody());

        } catch (IOException ex) {
            System.err.println("❌ Erreur envoi email: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}