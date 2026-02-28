package Services;

import Models.*;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmailService {

    // Configuration SendGrid - Remplacez par votre vraie clé API
    private static final String SENDGRID_API_KEY = System.getenv("SENDGRID_API_KEY");
    private static final String FROM_EMAIL = "noreply@fintrack.com";
    private static final String FROM_NAME = "FinTrack";

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
        String htmlContent = buildTransferEmailContent(transaction, source, dest, user, frais);

        sendEmail(user.getEmail(), subject, htmlContent);
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

        String htmlContent = buildScheduledTransferEmailContent(st, source, dest, user, success, errorMessage);

        sendEmail(user.getEmail(), subject, htmlContent);
    }

    /**
     * Envoyer un email de bienvenue pour un nouvel utilisateur
     */
    public void sendWelcomeEmail(Utilisateur user) {
        String subject = "🎉 Bienvenue sur FinTrack !";
        String htmlContent = buildWelcomeEmailContent(user);

        sendEmail(user.getEmail(), subject, htmlContent);
    }

    /**
     * Envoyer une newsletter à tous les utilisateurs
     */
    public void sendNewsletter(String sujet, String contenu) {
        List<Utilisateur> users = utilisateurService.afficherTous();

        for (Utilisateur user : users) {
            String subject = "📧 FinTrack Newsletter : " + sujet;
            String htmlContent = buildNewsletterContent(user, contenu);

            sendEmail(user.getEmail(), subject, htmlContent);

            // Pause pour éviter de surcharger l'API
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("✅ Newsletter envoyée à " + users.size() + " utilisateurs");
    }

    /**
     * Envoyer une alerte de sécurité
     */
    public void sendSecurityAlert(Utilisateur user, String action) {
        String subject = "🔒 Alerte de sécurité - FinTrack";
        String htmlContent = buildSecurityAlertContent(user, action);

        sendEmail(user.getEmail(), subject, htmlContent);
    }

    /**
     * Envoyer un rapport mensuel à un utilisateur
     */
    public void sendMonthlyReport(Utilisateur user, List<Transaction> transactions,
                                  double totalDepenses, double totalRevenus) {
        String subject = "📊 Votre rapport mensuel FinTrack";
        String htmlContent = buildMonthlyReportContent(user, transactions, totalDepenses, totalRevenus);

        sendEmail(user.getEmail(), subject, htmlContent);
    }

    // ========== CONSTRUCTION DES EMAILS ==========

    private String buildTransferEmailContent(Transaction transaction, CarteVirtuelle source,
                                             CarteVirtuelle dest, Utilisateur user, double frais) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html><head><meta charset='UTF-8'>");
        sb.append("<style>");
        sb.append("body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7fb; margin: 0; padding: 20px; }");
        sb.append(".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 20px; padding: 30px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }");
        sb.append(".header { background: linear-gradient(135deg, #182d88, #2c5aa0); color: white; padding: 25px; border-radius: 20px 20px 0 0; text-align: center; margin: -30px -30px 20px -30px; }");
        sb.append(".header h1 { margin: 0; font-size: 28px; }");
        sb.append(".success-badge { background: #27ae60; color: white; padding: 12px; border-radius: 50px; text-align: center; font-weight: bold; margin-bottom: 25px; font-size: 16px; }");
        sb.append(".amount-box { font-size: 36px; font-weight: bold; color: #182d88; text-align: center; padding: 25px; background: #f0f5ff; border-radius: 15px; margin: 20px 0; }");
        sb.append(".details-table { width: 100%; border-collapse: collapse; margin: 20px 0; background: #f8f9fa; border-radius: 10px; overflow: hidden; }");
        sb.append(".details-table td { padding: 15px; border-bottom: 1px solid #e0e0e0; }");
        sb.append(".details-table tr:last-child td { border-bottom: none; }");
        sb.append(".details-table td:first-child { font-weight: bold; color: #555; width: 40%; background: #f1f3f5; }");
        sb.append(".details-table td:last-child { color: #182d88; font-weight: 500; }");
        sb.append(".fees-box { background: #fff3e0; padding: 15px; border-radius: 10px; margin: 20px 0; border-left: 4px solid #f39c12; }");
        sb.append(".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 2px solid #e0e0e0; color: #999; font-size: 12px; }");
        sb.append(".button { background: #182d88; color: white; padding: 12px 30px; text-decoration: none; border-radius: 25px; display: inline-block; margin: 20px 0; font-weight: bold; }");
        sb.append("</style>");
        sb.append("</head><body>");

        sb.append("<div class='container'>");
        sb.append("<div class='header'><h1>🏦 FinTrack</h1></div>");

        sb.append("<div class='success-badge'>✅ Transfert effectué avec succès</div>");

        sb.append("<div class='amount-box'>");
        sb.append(String.format("%.2f %s", transaction.getMontant(), source.getDevise()));
        sb.append("</div>");

        sb.append("<h3 style='color: #182d88; margin-top: 25px;'>Détails du transfert</h3>");

        sb.append("<table class='details-table'>");
        sb.append("<tr><td>ID Transaction</td><td>#").append(transaction.getId()).append("</td></tr>");
        sb.append("<tr><td>Date</td><td>").append(transaction.getDate().format(DATE_FORMATTER)).append("</td></tr>");
        sb.append("<tr><td>Type</td><td>").append(transaction.getType()).append("</td></tr>");
        sb.append("<tr><td>Statut</td><td><span style='color: #27ae60; font-weight: bold;'>").append(transaction.getStatut()).append("</span></td></tr>");
        sb.append("</table>");

        sb.append("<h3 style='color: #182d88; margin-top: 25px;'>Cartes concernées</h3>");
        sb.append("<table class='details-table'>");
        sb.append("<tr><td>Carte source</td><td>").append(masquerNumero(source.getNumeroCarte())).append("</td></tr>");
        sb.append("<tr><td>Propriétaire</td><td>").append(user.getPrenom()).append(" ").append(user.getNom()).append("</td></tr>");
        sb.append("<tr><td>Carte destination</td><td>").append(masquerNumero(dest.getNumeroCarte())).append("</td></tr>");
        sb.append("<tr><td>Nouveau solde</td><td>").append(String.format("%.2f %s", source.getSolde(), source.getDevise())).append("</td></tr>");
        sb.append("</table>");

        if (frais > 0) {
            sb.append("<div class='fees-box'>");
            sb.append("<p style='margin: 0; color: #e67e22;'><strong>💰 Frais de transfert:</strong> ").append(String.format("%.2f %s", frais, source.getDevise())).append("</p>");
            sb.append("</div>");
        }

        sb.append("<div style='text-align: center;'>");
        sb.append("<a href='#' class='button'>Voir mes transactions</a>");
        sb.append("</div>");

        sb.append("<div class='footer'>");
        sb.append("<p>Merci d'utiliser FinTrack pour vos opérations bancaires.</p>");
        sb.append("<p>Ce message est automatique, merci de ne pas y répondre.</p>");
        sb.append("<p>© 2026 FinTrack - Tous droits réservés</p>");
        sb.append("</div>");

        sb.append("</div></body></html>");

        return sb.toString();
    }

    private String buildScheduledTransferEmailContent(ScheduledTransfer st, CarteVirtuelle source,
                                                      CarteVirtuelle dest, Utilisateur user,
                                                      boolean success, String errorMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html><head><meta charset='UTF-8'>");
        sb.append("<style>");
        sb.append("body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7fb; margin: 0; padding: 20px; }");
        sb.append(".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 20px; padding: 30px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }");
        sb.append(".header { background: linear-gradient(135deg, #182d88, #2c5aa0); color: white; padding: 25px; border-radius: 20px 20px 0 0; text-align: center; margin: -30px -30px 20px -30px; }");
        sb.append(".header h1 { margin: 0; font-size: 28px; }");
        sb.append(".success-badge { background: #27ae60; color: white; padding: 12px; border-radius: 50px; text-align: center; font-weight: bold; margin-bottom: 25px; font-size: 16px; }");
        sb.append(".failed-badge { background: #e74c3c; color: white; padding: 12px; border-radius: 50px; text-align: center; font-weight: bold; margin-bottom: 25px; font-size: 16px; }");
        sb.append(".amount-box { font-size: 36px; font-weight: bold; color: #182d88; text-align: center; padding: 25px; background: #f0f5ff; border-radius: 15px; margin: 20px 0; }");
        sb.append(".details-table { width: 100%; border-collapse: collapse; margin: 20px 0; background: #f8f9fa; border-radius: 10px; overflow: hidden; }");
        sb.append(".details-table td { padding: 15px; border-bottom: 1px solid #e0e0e0; }");
        sb.append(".details-table tr:last-child td { border-bottom: none; }");
        sb.append(".details-table td:first-child { font-weight: bold; color: #555; width: 40%; background: #f1f3f5; }");
        sb.append(".details-table td:last-child { color: #182d88; font-weight: 500; }");
        sb.append(".error-box { background: #fdedec; padding: 15px; border-radius: 10px; margin: 20px 0; border-left: 4px solid #e74c3c; }");
        sb.append(".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 2px solid #e0e0e0; color: #999; font-size: 12px; }");
        sb.append("</style>");
        sb.append("</head><body>");

        sb.append("<div class='container'>");
        sb.append("<div class='header'><h1>🏦 FinTrack</h1></div>");

        if (success) {
            sb.append("<div class='success-badge'>✅ Transfert programmé exécuté avec succès</div>");
        } else {
            sb.append("<div class='failed-badge'>❌ Échec de l'exécution du transfert programmé</div>");
        }

        sb.append("<div class='amount-box'>");
        sb.append(String.format("%.2f %s", st.getAmount(), source.getDevise()));
        sb.append("</div>");

        if (!success && errorMessage != null) {
            sb.append("<div class='error-box'>");
            sb.append("<p style='margin: 0; color: #c0392b;'><strong>Raison de l'échec:</strong> ").append(errorMessage).append("</p>");
            sb.append("</div>");
        }

        sb.append("<h3 style='color: #182d88; margin-top: 25px;'>Détails du transfert programmé</h3>");

        sb.append("<table class='details-table'>");
        sb.append("<tr><td>ID</td><td>#").append(st.getId()).append("</td></tr>");
        sb.append("<tr><td>Date planifiée</td><td>").append(st.getScheduledDate().format(DATE_FORMATTER)).append("</td></tr>");
        sb.append("<tr><td>Date d'exécution</td><td>").append(java.time.LocalDateTime.now().format(DATE_FORMATTER)).append("</td></tr>");
        sb.append("<tr><td>Statut</td><td>").append(st.getStatus()).append("</td></tr>");
        sb.append("<tr><td>Tentatives</td><td>").append(st.getAttempts()).append("</td></tr>");
        sb.append("</table>");

        sb.append("<h3 style='color: #182d88; margin-top: 25px;'>Cartes concernées</h3>");
        sb.append("<table class='details-table'>");
        sb.append("<tr><td>Carte source</td><td>").append(masquerNumero(source.getNumeroCarte())).append("</td></tr>");
        sb.append("<tr><td>Propriétaire</td><td>").append(user.getPrenom()).append(" ").append(user.getNom()).append("</td></tr>");
        sb.append("<tr><td>Carte destination</td><td>").append(masquerNumero(dest.getNumeroCarte())).append("</td></tr>");
        if (success) {
            sb.append("<tr><td>Nouveau solde</td><td>").append(String.format("%.2f %s", source.getSolde(), source.getDevise())).append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("<div class='footer'>");
        sb.append("<p>Merci d'utiliser FinTrack pour vos opérations bancaires.</p>");
        sb.append("<p>Ce message est automatique, merci de ne pas y répondre.</p>");
        sb.append("<p>© 2026 FinTrack - Tous droits réservés</p>");
        sb.append("</div>");

        sb.append("</div></body></html>");

        return sb.toString();
    }

    private String buildWelcomeEmailContent(Utilisateur user) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html><head><meta charset='UTF-8'>");
        sb.append("<style>");
        sb.append("body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7fb; margin: 0; padding: 20px; }");
        sb.append(".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 20px; padding: 30px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }");
        sb.append(".header { background: linear-gradient(135deg, #182d88, #2c5aa0); color: white; padding: 25px; border-radius: 20px 20px 0 0; text-align: center; margin: -30px -30px 20px -30px; }");
        sb.append(".header h1 { margin: 0; font-size: 28px; }");
        sb.append("</style>");
        sb.append("</head><body>");

        sb.append("<div class='container'>");
        sb.append("<div class='header'><h1>🏦 FinTrack</h1></div>");

        sb.append("<h2 style='color: #182d88;'>Bienvenue ").append(user.getPrenom()).append(" !</h2>");
        sb.append("<p style='font-size: 16px; line-height: 1.6;'>Merci d'avoir rejoint FinTrack, votre application de gestion financière.</p>");

        sb.append("<div style='background: #f8f9fa; padding: 20px; border-radius: 10px; margin: 20px 0;'>");
        sb.append("<h3 style='color: #182d88; margin-top: 0;'>🚀 Pour commencer :</h3>");
        sb.append("<ul style='line-height: 1.8;'>");
        sb.append("<li>Créez votre premier portefeuille</li>");
        sb.append("<li>Ajoutez des cartes virtuelles</li>");
        sb.append("<li>Effectuez des transferts en temps réel</li>");
        sb.append("<li>Planifiez des transferts programmés</li>");
        sb.append("</ul>");
        sb.append("</div>");

        sb.append("<div class='footer' style='text-align: center; margin-top: 30px; padding-top: 20px; border-top: 2px solid #e0e0e0; color: #999; font-size: 12px;'>");
        sb.append("<p>L'équipe FinTrack</p>");
        sb.append("</div>");

        sb.append("</div></body></html>");

        return sb.toString();
    }

    private String buildNewsletterContent(Utilisateur user, String contenu) {
        return "<html><body><h2>Bonjour " + user.getPrenom() + ",</h2>" + contenu + "</body></html>";
    }

    private String buildSecurityAlertContent(Utilisateur user, String action) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html><head><meta charset='UTF-8'>");
        sb.append("<style>");
        sb.append("body { font-family: Arial, sans-serif; background-color: #f4f7fb; padding: 20px; }");
        sb.append(".container { max-width: 500px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px; }");
        sb.append("</style>");
        sb.append("</head><body>");
        sb.append("<div class='container'>");
        sb.append("<h2 style='color: #e74c3c;'>🔒 Alerte de sécurité</h2>");
        sb.append("<p>Bonjour ").append(user.getPrenom()).append(",</p>");
        sb.append("<p>Une action de sécurité a été détectée sur votre compte :</p>");
        sb.append("<p><strong>").append(action).append("</strong></p>");
        sb.append("<p>Si vous n'êtes pas à l'origine de cette action, contactez-nous immédiatement.</p>");
        sb.append("<hr>");
        sb.append("<p style='color: #999; font-size: 12px;'>FinTrack - Sécurité de votre compte</p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String buildMonthlyReportContent(Utilisateur user, List<Transaction> transactions,
                                             double totalDepenses, double totalRevenus) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html><head><meta charset='UTF-8'>");
        sb.append("<style>");
        sb.append("body { font-family: Arial, sans-serif; }");
        sb.append(".container { max-width: 600px; margin: 0 auto; }");
        sb.append(".positive { color: #27ae60; font-weight: bold; }");
        sb.append(".negative { color: #e74c3c; font-weight: bold; }");
        sb.append("</style>");
        sb.append("</head><body>");
        sb.append("<div class='container'>");
        sb.append("<h2>📊 Votre rapport mensuel</h2>");
        sb.append("<p>Bonjour ").append(user.getPrenom()).append(",</p>");
        sb.append("<p>Voici le résumé de votre activité du mois :</p>");
        sb.append("<div style='background: #f8f9fa; padding: 20px; border-radius: 10px;'>");
        sb.append("<h3>Résumé</h3>");
        sb.append("<p>Total des dépenses : <span class='negative'>").append(String.format("%.2f DT", totalDepenses)).append("</span></p>");
        sb.append("<p>Total des revenus : <span class='positive'>").append(String.format("%.2f DT", totalRevenus)).append("</span></p>");
        sb.append("<p><strong>Solde net : </strong>").append(String.format("%.2f DT", totalRevenus - totalDepenses)).append("</p>");
        sb.append("</div>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String masquerNumero(String numero) {
        if (numero == null || numero.length() < 8) return "****";
        return "**** **** **** " + numero.substring(numero.length() - 4);
    }

    /**
     * Envoyer une notification pour un transfert programmé (utilisé par ScheduledTransferExecutor)
     */
    public void sendTransferNotification(ScheduledTransfer st, boolean success, String errorMessage) {
        // Récupérer les cartes
        CarteVirtuelle source = carteService.afficherParId(st.getFromCardId());
        CarteVirtuelle dest = carteService.afficherParId(st.getToCardId());

        if (source == null || dest == null) {
            System.err.println("❌ Impossible de récupérer les cartes pour le transfert programmé ID: " + st.getId());
            return;
        }

        // Réutiliser la méthode existante pour les transferts programmés
        sendScheduledTransferConfirmation(st, source, dest, success, errorMessage);
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

            if (response.getStatusCode() >= 400) {
                System.err.println("❌ Erreur SendGrid: " + response.getBody());
            }

        } catch (IOException ex) {
            System.err.println("❌ Erreur envoi email: " + ex.getMessage());
        }
    }
}