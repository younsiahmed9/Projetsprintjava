package tn.esprit.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DbConfig {
    private DbConfig() {
    }

    public static Properties load() {
        Properties props = new Properties();
        try (InputStream in = DbConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new IllegalStateException("Fichier application.properties introuvable dans src/main/resources");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de charger application.properties", e);
        }

        require(props, "db.url");
        require(props, "db.user");
        // db.password peut être vide.

        return props;
    }

    private static void require(Properties props, String key) {
        String v = props.getProperty(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Propriété manquante: " + key);
        }
    }
}
