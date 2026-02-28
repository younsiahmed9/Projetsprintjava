package Services;

import Models.*;
// ⚠️ NE PAS importer Models.Session ici

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class GmailService {

    // Configuration Gmail
    private static final String FROM_EMAIL = "votre.email@gmail.com"; // Votre email Gmail
    private static final String PASSWORD = "votre-mot-de-passe-app"; // Mot de passe d'application

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final CarteVirtuelleService carteService = new CarteVirtuelleService();

    /**
     * Envoyer un email de confirmation de transfert
     */
    public void sendTransferConfirmation(Transaction transaction, CarteVirtuelle source,
                                         CarteVirtuelle dest, double frais) {
        Utilisateur user = utilisateurService.getUserByCardId(source.getId());
        if (user == null) {
            System.err.println("❌ Utilisateur non trouvé pour la carte source");
            return;
        }

        String subject = "✅ Confirmation de transfert - FinTrack";
        String content = buildSimpleEmailContent(transaction, source, dest, user, frais);

        sendEmail(user.getEmail(), subject, content);
    }

    /**
     * Envoyer un email de confirmation de transfert programmé
     */
    public void sendScheduledTransferConfirmation(ScheduledTransfer st, CarteVirtuelle source,
                                                  CarteVirtuelle dest, boolean success,
                                                  String errorMessage) {
        Utilisateur user = utilisateurService.getUserByCardId(source.getId());
        if (user == null) {
            System.err.println("❌ Utilisateur non trouvé pour la carte source");
            return;
        }

        String subject = success ?
                "✅ Transfert programmé exécuté - FinTrack" :
                "❌ Échec de transfert programmé - FinTrack";

        String content = buildSimpleScheduledEmailContent(st, source, dest, user, success, errorMessage);

        sendEmail(user.getEmail(), subject, content);
    }

    /**
     * Version simplifiée de l'email
     */
    private String buildSimpleEmailContent(Transaction transaction, CarteVirtuelle source,
                                           CarteVirtuelle dest, Utilisateur user, double frais) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour ").append(user.getPrenom()).append(" ").append(user.getNom()).append(",\n\n");
        sb.append("✅ Votre transfert a été effectué avec succès !\n\n");
        sb.append("DÉTAILS DU TRANSFERT :\n");
        sb.append("------------------------\n");
        sb.append("Montant : ").append(String.format("%.2f %s", transaction.getMontant(), source.getDevise())).append("\n");
        sb.append("Date : ").append(transaction.getDate().format(DATE_FORMATTER)).append("\n");
        sb.append("Type : ").append(transaction.getType()).append("\n");
        sb.append("Statut : ").append(transaction.getStatut()).append("\n\n");

        sb.append("CARTES CONCERNÉES :\n");
        sb.append("------------------------\n");
        sb.append("Carte source : ").append(masquerNumero(source.getNumeroCarte())).append("\n");
        sb.append("Carte destination : ").append(masquerNumero(dest.getNumeroCarte())).append("\n");
        sb.append("Nouveau solde : ").append(String.format("%.2f %s", source.getSolde(), source.getDevise())).append("\n");

        if (frais > 0) {
            sb.append("\n💰 Frais de transfert : ").append(String.format("%.2f %s", frais, source.getDevise())).append("\n");
        }

        sb.append("\n------------------------\n");
        sb.append("Merci d'utiliser FinTrack !\n");
        sb.append("© 2026 FinTrack");

        return sb.toString();
    }

    private String buildSimpleScheduledEmailContent(ScheduledTransfer st, CarteVirtuelle source,
                                                    CarteVirtuelle dest, Utilisateur user,
                                                    boolean success, String errorMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour ").append(user.getPrenom()).append(" ").append(user.getNom()).append(",\n\n");

        if (success) {
            sb.append("✅ Votre transfert programmé a été exécuté avec succès !\n\n");
        } else {
            sb.append("❌ Échec de l'exécution de votre transfert programmé.\n\n");
        }

        sb.append("DÉTAILS DU TRANSFERT PROGRAMMÉ :\n");
        sb.append("------------------------\n");
        sb.append("Montant : ").append(String.format("%.2f %s", st.getAmount(), source.getDevise())).append("\n");
        sb.append("Date planifiée : ").append(st.getScheduledDate().format(DATE_FORMATTER)).append("\n");
        sb.append("Date d'exécution : ").append(java.time.LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        sb.append("Statut : ").append(st.getStatus()).append("\n");

        if (!success && errorMessage != null) {
            sb.append("Raison de l'échec : ").append(errorMessage).append("\n");
        }

        sb.append("\nCARTES CONCERNÉES :\n");
        sb.append("------------------------\n");
        sb.append("Carte source : ").append(masquerNumero(source.getNumeroCarte())).append("\n");
        sb.append("Carte destination : ").append(masquerNumero(dest.getNumeroCarte())).append("\n");
        if (success) {
            sb.append("Nouveau solde : ").append(String.format("%.2f %s", source.getSolde(), source.getDevise())).append("\n");
        }

        sb.append("\n------------------------\n");
        sb.append("Merci d'utiliser FinTrack !\n");
        sb.append("© 2026 FinTrack");

        return sb.toString();
    }

    private String masquerNumero(String numero) {
        if (numero == null || numero.length() < 8) return "****";
        return "**** **** **** " + numero.substring(numero.length() - 4);
    }

    /**
     * Envoyer un email via Gmail
     */
    private void sendEmail(String toEmail, String subject, String content) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // ✅ Utiliser le nom complet pour éviter le conflit
        javax.mail.Session session = javax.mail.Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
            System.out.println("📧 Email envoyé à " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}