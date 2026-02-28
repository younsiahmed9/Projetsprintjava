package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// La classe est 'final' car un Singleton ne doit pas être hérité.
public final class MyDataBase {
    // 'volatile' garantit que les modifications de 'instance' sont visibles par tous les threads.
    private static volatile MyDataBase instance;
    private Connection connection;

    // Le nom de la base de données ne devrait pas contenir d'espaces.
    // Pensez à renommer votre base de données en 'service_et_produit'.
    private final String URL = "jdbc:mysql://localhost:3306/service et produit";
    private final String USER = "root";
    private final String PASSWORD = "";

    private MyDataBase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion à la base de données établie avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
            throw new RuntimeException("Erreur de connexion à la base de données", e);
        }
    }

    // Implémentation du Singleton "thread-safe" avec double-checked locking.
    public static MyDataBase getInstance() {
        if (instance == null) {
            synchronized (MyDataBase.class) {
                if (instance == null) {
                    instance = new MyDataBase();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}