package utils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Alias pour MyDatabase pour une meilleure sémantique
 * Fournit un point d'accès unique à la connexion à la base de données
 */
public class DataSource {
    private static DataSource instance;
    private final MyDatabase database;

    private DataSource() throws SQLException {
        this.database = MyDatabase.getInstance();
    }

    public static DataSource getInstance() throws SQLException {
        if (instance == null) {
            instance = new DataSource();
        }
        return instance;
    }

    public Connection getCnx() {
        return database.getConnection();
    }

    public Connection getConnection() {
        return database.getConnection();
    }
}

