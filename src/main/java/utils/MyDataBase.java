package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private static MyDataBase instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3306/fintrackdb";
    private final String USER = "root";
    private final String PASSWORD = ""; // XAMPP default is empty

    private MyDataBase() {
        try {
            // Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Établir la connexion
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion établie avec succès à la base de données!");

        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données!");
            e.printStackTrace();
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    // Méthode pour tester la connexion
    public static void main(String[] args) {
        MyDataBase db = MyDataBase.getInstance();
        if (db.getConnection() != null) {
            System.out.println("🎉 Base de données prête à l'emploi!");
        }
    }
}