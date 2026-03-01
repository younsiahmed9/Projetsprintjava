package Controllers.Dialogs;

import Models.Document;
import Controllers.AlertUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;

import javafx.scene.layout.VBox;
import utils.UiStyles;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ShareActionDialog {

    public static void showShareDialog(Document doc) {
        if (doc == null)
            return;

        Dialog<Void> dialog = new Dialog<>();
        UiStyles.applyDialogStyles(dialog.getDialogPane());
        dialog.setTitle("Partager le document");
        dialog.setWidth(500);

        // Conteneur principal (Card style)
        VBox content = new VBox(25);
        content.getStyleClass().add("share-card");
        content.setPadding(new Insets(35));
        content.setAlignment(Pos.CENTER);

        // Titre de la section
        Label lblHeader = new Label("Partage de Document");
        lblHeader.getStyleClass().add("share-header");

        Label lblSubHeader = new Label("Document : " + doc.getTitre());
        lblSubHeader.setStyle("-fx-font-size: 14; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        // Bouton Copier le lien/texte
        Button btnCopy = createShareButton("📋  Copier les informations", "share-btn-main");
        btnCopy.setOnAction(e -> {
            copyToClipboard(buildShareText(doc));
            AlertUtils.showSuccess("Copié", "Les informations ont été copiées !");
        });

        // Bouton Email
        Button btnEmail = createShareButton("📧  Envoyer par Email", "share-btn-main");
        btnEmail.setStyle(btnEmail.getStyle() + "; -fx-border-color: #3b82f6; -fx-text-fill: #2563eb;");
        btnEmail.setOnAction(e -> sendByEmail(doc));

        // Section Réseaux Sociaux
        VBox socialSection = new VBox(15);
        socialSection.setAlignment(Pos.CENTER);
        Label lblSocial = new Label("Partager via les réseaux sociaux");
        lblSocial.setStyle("-fx-font-weight: 800; -fx-text-fill: #475569; -fx-font-size: 13;");

        FlowPane socialPane = new FlowPane(12, 12);
        socialPane.setAlignment(Pos.CENTER);

        Button btnWhatsApp = createSmallSocialButton("WhatsApp", "#22c55e");
        btnWhatsApp.setOnAction(e -> shareOnWhatsApp(doc));

        Button btnTwitter = createSmallSocialButton("X / Twitter", "#0f172a");
        btnTwitter.setOnAction(e -> shareOnTwitter(doc));

        Button btnFacebook = createSmallSocialButton("Facebook", "#1877f2");
        btnFacebook.setOnAction(e -> shareOnFacebook(doc));

        Button btnLinkedIn = createSmallSocialButton("LinkedIn", "#0077b5");
        btnLinkedIn.setOnAction(e -> shareOnLinkedIn(doc));

        Button btnTelegram = createSmallSocialButton("Telegram", "#0088cc");
        btnTelegram.setOnAction(e -> shareOnTelegram(doc));

        socialPane.getChildren().addAll(btnWhatsApp, btnTwitter, btnFacebook, btnLinkedIn, btnTelegram);
        socialSection.getChildren().addAll(lblSocial, socialPane);

        content.getChildren().addAll(lblHeader, lblSubHeader, btnCopy, btnEmail, new Separator(), socialSection);

        dialog.getDialogPane().setContent(content);

        // Bouton fermer
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Button closeButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        if (closeButton != null) {
            closeButton.setText("Fermer");
            closeButton.getStyleClass().add("btn-secondary-premium");
        }

        dialog.showAndWait();
    }

    private static Button createShareButton(String text, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().add(styleClass);
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private static Button createSmallSocialButton(String text, String color) {
        Button btn = new Button(text);
        btn.getStyleClass().add("share-social-btn");
        btn.setStyle("-fx-background-color: " + color + ";");
        return btn;
    }

    private static String buildShareText(Document doc) {
        String titre = doc.getTitre() != null ? doc.getTitre() : "(Sans titre)";
        String path = doc.getFilePath() != null ? doc.getFilePath() : "Lien non disponible";
        return "Document : " + titre + "\nConsultez ce document ici : " + path;
    }

    private static void copyToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private static void sendByEmail(Document doc) {
        try {
            String subject = URLEncoder.encode("Partage de document : " + doc.getTitre(), StandardCharsets.UTF_8);
            String body = URLEncoder.encode(buildShareText(doc), StandardCharsets.UTF_8);
            String mailto = "mailto:?subject=" + subject + "&body=" + body;
            Desktop.getDesktop().mail(URI.create(mailto.replace("+", "%20"))); // Fix spaces in URLEncoder
        } catch (Exception e) {
            AlertUtils.showError("Erreur d'email", "Impossible d'ouvrir le client de messagerie par défaut.");
        }
    }

    private static void shareOnWhatsApp(Document doc) {
        try {
            String text = URLEncoder.encode(buildShareText(doc), StandardCharsets.UTF_8);
            String url = "https://wa.me/?text=" + text;
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Impossible d'ouvrir WhatsApp.");
        }
    }

    private static void shareOnTwitter(Document doc) {
        try {
            String text = URLEncoder.encode(buildShareText(doc), StandardCharsets.UTF_8);
            String url = "https://twitter.com/intent/tweet?text=" + text;
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Impossible d'ouvrir X (Twitter).");
        }
    }

    private static void shareOnFacebook(Document doc) {
        try {
            // Facebook shared usually takes a URL. We use the file path as "url" if it's
            // reachable or just a placeholder.
            String url = "https://www.facebook.com/sharer/sharer.php?u="
                    + URLEncoder.encode(doc.getFilePath(), StandardCharsets.UTF_8);
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Impossible d'ouvrir Facebook.");
        }
    }

    private static void shareOnLinkedIn(Document doc) {
        try {
            String url = "https://www.linkedin.com/sharing/share-offsite/?url="
                    + URLEncoder.encode(doc.getFilePath(), StandardCharsets.UTF_8);
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Impossible d'ouvrir LinkedIn.");
        }
    }

    private static void shareOnTelegram(Document doc) {
        try {
            String text = URLEncoder.encode(buildShareText(doc), StandardCharsets.UTF_8);
            String url = "https://t.me/share/url?url=" + URLEncoder.encode(doc.getFilePath(), StandardCharsets.UTF_8)
                    + "&text=" + text;
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Impossible d'ouvrir Telegram.");
        }
    }
}
