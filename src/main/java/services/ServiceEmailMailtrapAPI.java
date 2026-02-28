package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import config.MailtrapConfig;
import okhttp3.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class ServiceEmailMailtrapAPI {

    private final OkHttpClient client = new OkHttpClient();
    private final String apiToken;

    public ServiceEmailMailtrapAPI() {
        this.apiToken = MailtrapConfig.API_TOKEN;
    }

    public boolean envoyerFactureEmail(String destinataire, String sujet, String corps, String cheminPDF) {
        try {
            byte[] pdfContent = Files.readAllBytes(Paths.get(cheminPDF));
            String base64Pdf = Base64.getEncoder().encodeToString(pdfContent);

            JsonObject json = new JsonObject();

            JsonObject fromObj = new JsonObject();
            fromObj.addProperty("email", MailtrapConfig.FROM_EMAIL);
            fromObj.addProperty("name", MailtrapConfig.FROM_NAME);
            json.add("from", fromObj);

            JsonArray toArray = new JsonArray();
            JsonObject toObj = new JsonObject();
            toObj.addProperty("email", destinataire);
            toArray.add(toObj);
            json.add("to", toArray);

            json.addProperty("subject", sujet);
            json.addProperty("text", corps);

            JsonArray attachments = new JsonArray();
            JsonObject attachment = new JsonObject();
            attachment.addProperty("filename", "facture.pdf");
            attachment.addProperty("content", base64Pdf);
            attachment.addProperty("type", "application/pdf");
            attachments.add(attachment);
            json.add("attachments", attachments);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url("https://send.api.mailtrap.io/api/send")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiToken)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    System.out.println("✅ Email envoyé avec Mailtrap API à " + destinataire);
                    return true;
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    System.err.println("❌ Erreur Mailtrap API : " + response.code() + " " + errorBody);
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}