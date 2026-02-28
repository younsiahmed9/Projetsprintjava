package Services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();
    private static boolean loaded = false;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("❌ Fichier config.properties non trouvé");
                return;
            }
            props.load(input);
            loaded = true;
            System.out.println("✅ Configuration chargée avec succès");
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement configuration: " + e.getMessage());
        }
    }

    public static String get(String key) {
        if (!loaded) {
            loadConfig();
        }
        return props.getProperty(key);
    }

    public static String getApiKey() {
        return get("elasticemail.api.key");
    }

    public static String getFromEmail() {
        return get("elasticemail.from.email");
    }

    public static String getFromName() {
        return get("elasticemail.from.name");
    }
}