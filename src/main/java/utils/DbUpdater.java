package utils;

import java.sql.Connection;
import java.sql.Statement;

public class DbUpdater {
    public static void main(String[] args) {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE documents ADD COLUMN date_facture TEXT;");
                System.out.println("Column date_facture added successfully.");
            } catch (Exception e) {
                System.out.println("Could not add date_facture: " + e.getMessage());
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE documents ADD COLUMN date_limite_paiement TEXT;");
                System.out.println("Column date_limite_paiement added successfully.");
            } catch (Exception e) {
                System.out.println("Could not add date_limite_paiement: " + e.getMessage());
            }
            System.out.println("DB update complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
