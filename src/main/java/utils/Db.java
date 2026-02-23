package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Db {
    private static final Properties props = DbConfig.load();

    private Db() {
    }

    public static Connection getConnection() throws SQLException {
        // Fallback: si le driver n'est pas découvert automatiquement (classpath / module path)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // On laisse DriverManager lancer une SQLException plus bas, avec un message clair.
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password", "");

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("No suitable driver")) {
                throw new SQLException(
                        e.getMessage() + " | Le driver MySQL n'est pas sur le classpath. " +
                                "Dans IntelliJ, recharge Maven et lance via Maven (exec:java) ou assure-toi que les dépendances sont incluses.",
                        e);
            }
            throw e;
        }
    }
}
