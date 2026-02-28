package Services;

import Models.*;
import okhttp3.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ElasticEmailService {

    // Utiliser la configuration au lieu des constantes
    private static final String API_KEY = Config.getApiKey();
    private static final String FROM_EMAIL = Config.getFromEmail();
    private static final String FROM_NAME = Config.getFromName();

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final OkHttpClient client = new OkHttpClient();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final CarteVirtuelleService carteService = new CarteVirtuelleService();

    // Vérification que la configuration est chargée
    static {
        if (API_KEY == null || API_KEY.isEmpty() || API_KEY.equals("null")) {
            System.err.println("❌ Clé API non configurée dans config.properties");
        } else {
            System.out.println("✅ API Email configurée avec succès");
        }
    }

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
        String content = buildTransferEmailContent(transaction, source, dest, user, frais);

        sendEmail(user.getEmail(), user.getPrenom() + " " + user.getNom(), subject, content);
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

        String content = buildScheduledTransferEmailContent(st, source, dest, user, success, errorMessage);

        sendEmail(user.getEmail(), user.getPrenom() + " " + user.getNom(), subject, content);
    }

    /**
     * Construire le contenu de l'email de transfert
     */
    private String buildTransferEmailContent(Transaction transaction, CarteVirtuelle source,
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

    /**
     * Construire le contenu de l'email de transfert programmé
     */
    private String buildScheduledTransferEmailContent(ScheduledTransfer st, CarteVirtuelle source,
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
        sb.append("Tentatives : ").append(st.getAttempts()).append("\n");

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

    /**
     * Masquer le numéro de carte
     */
    private String masquerNumero(String numero) {
        if (numero == null || numero.length() < 8) return "****";
        return "**** **** **** " + numero.substring(numero.length() - 4);
    }

    /**
     * Envoyer l'email via l'API Elastic Email
     */
    public void sendEmail(String toEmail, String toName, String subject, String body) {
        // Vérifier que la clé API est configurée
        if (API_KEY == null || API_KEY.isEmpty() || API_KEY.equals("null")) {
            System.err.println("❌ Impossible d'envoyer l'email : clé API non configurée");
            System.err.println("   Vérifiez le fichier src/main/resources/config.properties");
            return;
        }

        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.elasticemail.com/v2/email/send").newBuilder();
            urlBuilder.addQueryParameter("apikey", API_KEY);
            urlBuilder.addQueryParameter("from", FROM_EMAIL);
            urlBuilder.addQueryParameter("fromName", FROM_NAME);
            urlBuilder.addQueryParameter("to", toEmail);
            urlBuilder.addQueryParameter("toName", toName);
            urlBuilder.addQueryParameter("subject", subject);
            urlBuilder.addQueryParameter("bodyText", body);
            urlBuilder.addQueryParameter("isTransactional", "true");

            Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .post(RequestBody.create(new byte[0], null))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    // Vérifier la réponse JSON
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (json.has("success") && json.get("success").getAsBoolean()) {
                        System.out.println("📧 Email envoyé avec succès à " + toEmail);
                    } else {
                        String error = json.has("error") ? json.get("error").getAsString() : "Erreur inconnue";
                        System.err.println("❌ Erreur ElasticEmail: " + error);
                    }
                } else {
                    System.err.println("❌ Erreur HTTP: " + response.code() + " - " + responseBody);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Exception lors de l'envoi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}