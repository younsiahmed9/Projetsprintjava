package Services;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class MailService {
    private final String username;
    private final String password;
    private final String from;
    private final Properties props;

    public MailService(String username, String password, String from) {
        this.username = username;
        this.password = password;
        this.from = from;
        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
    }

    public void sendVerificationMail(String to, String subject, String body) throws MessagingException {
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);
        Transport.send(message);
    }

    /**
     * Envoie un mail contenant le code de vérification
     */
    public void sendVerificationCode(String to, String code) {
        String subject = "Votre code de vérification";
        String body = "Bonjour,\n\nVotre code de vérification est : " + code + "\n\nCe code expire dans 10 minutes.\n\nMerci.";
        try {
            sendVerificationMail(to, subject, body);
        } catch (MessagingException e) {
            System.err.println("Erreur envoi mail code: " + e.getMessage());
        }
    }
}
