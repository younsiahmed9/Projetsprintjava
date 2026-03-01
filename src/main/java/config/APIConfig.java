package config;

import java.io.InputStream;
import java.util.Properties;

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

    // ==================== CLASSE INTERNE POUR LA CONFIGURATION ====================
    public static class ApiConfig {

        private static final String CONFIG_FILE = "config/api.properties";
        private static Properties properties = new Properties();

        static {
            try (InputStream input = ApiConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                if (input != null) {
                    properties.load(input);
                    System.out.println("✅ Configuration API chargée");
                } else {
                    System.err.println("⚠️ Fichier " + CONFIG_FILE + " non trouvé, utilisation des valeurs par défaut");
                    setDefaultValues();
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur chargement config: " + e.getMessage());
                setDefaultValues();
            }
        }

        private static void setDefaultValues() {
            // Fournisseur SMS
            properties.setProperty("sms.provider", "simulation");

            // Twilio
            properties.setProperty("twilio.account.sid", "ACvotre_account_sid");
            properties.setProperty("twilio.auth.token", "votre_auth_token");
            properties.setProperty("twilio.phone.number", "+33123456789");

            // Infobip
            properties.setProperty("infobip.api.key", "votre_api_key");
            properties.setProperty("infobip.api.url", "https://api.infobip.com");

            // WhatsApp
            properties.setProperty("whatsapp.business.id", "votre_business_id");
            properties.setProperty("whatsapp.access.token", "votre_access_token");

            // Configuration promotion
            properties.setProperty("promotion.delai.jours", "7");
            properties.setProperty("promotion.reduction.default", "20");
            properties.setProperty("promotion.duree.jours", "7");
            properties.setProperty("promotion.reduction.expiree", "30");

            // Numéros de test
            properties.setProperty("test.numbers", "+21612345678,+21687654321");
        }

        // ==================== GETTERS GÉNÉRAUX ====================
        public static String get(String key) {
            return properties.getProperty(key);
        }

        public static String get(String key, String defaultValue) {
            return properties.getProperty(key, defaultValue);
        }

        // ==================== FOURNISSEUR SMS ====================
        public static String getSmsProvider() {
            return properties.getProperty("sms.provider", "simulation");
        }

        // ==================== TWILIO ====================
        public static String getTwilioAccountSid() {
            return properties.getProperty("twilio.account.sid");
        }

        public static String getTwilioAuthToken() {
            return properties.getProperty("twilio.auth.token");
        }

        public static String getTwilioPhoneNumber() {
            return properties.getProperty("twilio.phone.number");
        }

        // ==================== INFOBIP ====================
        public static String getInfobipApiKey() {
            return properties.getProperty("infobip.api.key");
        }

        public static String getInfobipApiUrl() {
            return properties.getProperty("infobip.api.url");
        }

        // ==================== WHATSAPP ====================
        public static String getWhatsAppBusinessId() {
            return properties.getProperty("whatsapp.business.id");
        }

        public static String getWhatsAppAccessToken() {
            return properties.getProperty("whatsapp.access.token");
        }

        // ==================== CONFIGURATION PROMOTION ====================
        public static int getPromotionDelaiJours() {
            try {
                return Integer.parseInt(properties.getProperty("promotion.delai.jours", "7"));
            } catch (NumberFormatException e) {
                return 7;
            }
        }

        public static double getPromotionReductionDefault() {
            try {
                return Double.parseDouble(properties.getProperty("promotion.reduction.default", "20"));
            } catch (NumberFormatException e) {
                return 20.0;
            }
        }

        public static int getPromotionDureeJours() {
            try {
                return Integer.parseInt(properties.getProperty("promotion.duree.jours", "7"));
            } catch (NumberFormatException e) {
                return 7;
            }
        }

        public static double getPromotionReductionExpiree() {
            try {
                return Double.parseDouble(properties.getProperty("promotion.reduction.expiree", "30"));
            } catch (NumberFormatException e) {
                return 30.0;
            }
        }

        // ==================== NUMÉROS DE TEST ====================
        public static String[] getTestNumbers() {
            String numbers = properties.getProperty("test.numbers", "+21612345678");
            return numbers.split(",");
        }

        // ==================== MÉTHODES UTILITAIRES ====================
        public static boolean isSimulationMode() {
            return "simulation".equalsIgnoreCase(getSmsProvider());
        }

        public static boolean isTwilioMode() {
            return "twilio".equalsIgnoreCase(getSmsProvider());
        }

        public static boolean isInfobipMode() {
            return "infobip".equalsIgnoreCase(getSmsProvider());
        }

        public static void afficherConfiguration() {
            System.out.println("\n📋 CONFIGURATION ACTUELLE:");
            System.out.println("   Fournisseur SMS: " + getSmsProvider());
            System.out.println("   Délai expiration: " + getPromotionDelaiJours() + " jours");
            System.out.println("   Réduction défaut: " + getPromotionReductionDefault() + "%");
            System.out.println("   Réduction expirée: " + getPromotionReductionExpiree() + "%");
            System.out.println("   Durée promotion: " + getPromotionDureeJours() + " jours");
            System.out.println("   Numéros test: " + String.join(", ", getTestNumbers()));
        }
    }
}