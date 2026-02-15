package utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MyDatabase {
    private static MyDatabase instance;
    private final Connection cnx;

    private MyDatabase() throws SQLException {
        try {
            Properties props = new Properties();
            try (InputStream is = MyDatabase.class.getResourceAsStream("/db.properties")) {
                if (is == null) throw new IllegalStateException("db.properties not found.");
                props.load(is);
            }
            cnx = DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.user"),
                    props.getProperty("db.password")
            );
        } catch (Exception e) {
            if (e instanceof SQLException) throw (SQLException) e;
            throw new SQLException("Failed to init DB: " + e.getMessage(), e);
        }
    }

    public static MyDatabase getInstance() throws SQLException {
        if (instance == null) instance = new MyDatabase();
        return instance;
    }

    public Connection getConnection() {
        return cnx;
    }
}

