package config;

public class APIConfig {
    // URL de base de l'API
    public static final String BASE_URL = "https://api.votre-service.com/v1";

    // ⚠️ REMPLACEZ CES INFORMATIONS PAR VOS VRAIES CLÉS API
    public static final String API_KEY = "votre-clé-api";
    public static final String API_SECRET = "votre-secret-api";

    // Timeouts (en secondes)
    public static final int CONNECT_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;
}