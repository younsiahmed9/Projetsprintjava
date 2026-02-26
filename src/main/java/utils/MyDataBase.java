package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private static final String URL = "jdbc:mysql://localhost:3306/workshopdb";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static MyDataBase instance;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé!");
            e.printStackTrace();
        }
    }

    private MyDataBase() {}

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}