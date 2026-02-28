package services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import models.Facture;
import okhttp3.*;
import config.APIConfig;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServiceAPI {

    private final OkHttpClient client;
    private final Gson gson;
    private final String baseUrl;
    private final String apiKey;

    // Constructeur avec paramètres
    public ServiceAPI(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(APIConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(APIConfig.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(APIConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    // Constructeur par défaut (utilise APIConfig)
    public ServiceAPI() {
        this(APIConfig.BASE_URL, APIConfig.API_KEY);
    }

    public ServiceAPI(String url) {
        this(url, APIConfig.API_KEY);
    }

    public boolean envoyerFactureAPI(Facture facture) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("idFacture", facture.getIdFacture());
            json.addProperty("numeroFacture", facture.getNumeroFacture());
            json.addProperty("montant", facture.getMontant());
            json.addProperty("dateFacture", facture.getDateFacture() != null ? facture.getDateFacture().toString() : "");
            json.addProperty("statut", facture.getStatut());
            json.addProperty("emailClient", facture.getEmailClient());
            json.addProperty("nomClient", facture.getNomClient());

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(baseUrl + "/factures")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    System.out.println("✅ Facture envoyée à l'API");
                    return true;
                } else {
                    System.err.println("❌ Erreur API: " + response.code());
                    return false;
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur connexion API: " + e.getMessage());
            return false;
        }
    }

    public List<Facture> getToutesFactures() {
        Request request = new Request.Builder()
                .url(baseUrl + "/factures")
                .get()
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                Type factureListType = new TypeToken<List<Facture>>(){}.getType();
                return gson.fromJson(jsonResponse, factureListType);
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur récupération API: " + e.getMessage());
        }
        return null;
    }

    public boolean testConnexion() {
        Request request = new Request.Builder()
                .url(baseUrl + "/health")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            System.err.println("❌ API non accessible: " + e.getMessage());
            return false;
        }
    }
}