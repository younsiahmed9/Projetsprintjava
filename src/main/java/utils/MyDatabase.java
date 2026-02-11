package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    final String URL="jdbc:mysql://localhost:3306/fintrack";
    final String USERNAME="root";
    final String PASSWORD="";
    private Connection conn;
    private  static MyDatabase instance ;

    public Connection getConn() {
        return conn;
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public MyDatabase(){
        try {
            conn= DriverManager.getConnection(URL,USERNAME,PASSWORD);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
