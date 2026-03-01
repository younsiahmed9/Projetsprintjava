package utils;

import java.sql.Connection;
import java.sql.Statement;

public class DbAlertMigration {
    public static void main(String[] args) {
        try {
            Connection conn = MyDatabase.getInstance().getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE echeance ADD COLUMN date_creation_alerte DATE;");
                System.out.println("Column date_creation_alerte added successfully to echeance table.");
            } catch (Exception e) {
                System.out.println("Note: Could not add date_creation_alerte (might already exist): " + e.getMessage());
            }

            // Also ensure document table has date_limite_paiement (from previous updates
            // but just in case)
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE documents ADD COLUMN date_limite_paiement DATE;");
                System.out.println("Column date_limite_paiement ensured in documents table.");
            } catch (Exception e) {
                // Ignore if exists
            }

            System.out.println("Database migration for Alerts completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
