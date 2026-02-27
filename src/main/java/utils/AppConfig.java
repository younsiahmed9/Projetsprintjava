package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {
    private static final Properties props;
    static {
        props = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new IllegalStateException("Fichier application.properties introuvable dans src/main/resources");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de charger application.properties", e);
        }
    }
    public static String get(String key) {
        return props.getProperty(key);
    }
}
