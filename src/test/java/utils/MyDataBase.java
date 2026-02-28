package utils;

import java.sql.*;

public class MyDataBase {
    private static MyDataBase instance;
    private Connection connection;
    private final String URL = "jdbc:mysql://localhost:3306/service et produit";
    private final String USER = "root";
    private final String PASSWORD = "";

    private MyDataBase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion à la base de données établie avec succès!");
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion: " + e.getMessage());
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}