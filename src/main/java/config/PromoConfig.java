package config;

import java.io.InputStream;
import java.util.Properties;

public class PromoConfig {

    private static final String CONFIG_FILE = "config/promo.properties";
    private static Properties props = new Properties();

    static {
        try (InputStream input = PromoConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                props.load(input);
                System.out.println("✅ Configuration promotion chargée");
            } else {
                System.out.println("⚠️ Fichier non trouvé, valeurs par défaut");
                setDefaultValues();
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
            setDefaultValues();
        }
    }

    private static void setDefaultValues() {
        props.setProperty("sms.mode", "simulation");
        props.setProperty("promo.reduction", "30");
        props.setProperty("promo.duree", "7");
        props.setProperty("test.numbers", "+21612345678,+21687654321");
        props.setProperty("twilio.sid", "AC123456");
        props.setProperty("twilio.token", "token123");
        props.setProperty("twilio.phone", "+33123456789");
    }

    public static String get(String key) {
        return props.getProperty(key, "");
    }

    public static boolean isSimulation() {
        return "simulation".equals(get("sms.mode"));
    }

    public static int getReduction() {
        try {
            return Integer.parseInt(get("promo.reduction"));
        } catch (Exception e) {
            return 30;
        }
    }

    public static int getDuree() {
        try {
            return Integer.parseInt(get("promo.duree"));
        } catch (Exception e) {
            return 7;
        }
    }

    public static String[] getTestNumbers() {
        return get("test.numbers").split(",");
    }

    public static void afficher() {
        System.out.println("\n📋 CONFIGURATION PROMO:");
        System.out.println("   Mode SMS: " + get("sms.mode"));
        System.out.println("   Réduction: " + getReduction() + "%");
        System.out.println("   Durée: " + getDuree() + " jours");
        System.out.println("   Numéros: " + get("test.numbers"));
    }
}