import java.sql.*;

public class TestSimple {
    public static void main(String[] args) {
        System.out.println("🔵 TEST D'INSERTION - VERSION CORRIGÉE");

        Connection conn = null;
        try {
            // 1. Charger le driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ Driver chargé");

            // 2. Connexion
            String url = "jdbc:mysql://localhost:3306/esprit?useSSL=false&serverTimezone=UTC";
            String user = "root";
            String password = "";

            conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connecté à la base 'esprit'");

            // 3. Vérifier la structure de la table
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet columns = meta.getColumns(null, null, "paiement", null);
            System.out.println("\n📋 Structure de la table paiement:");
            while (columns.next()) {
                System.out.println("   - " + columns.getString("COLUMN_NAME") +
                        " (" + columns.getString("TYPE_NAME") + ")");
            }

            // 4. Insertion test (SANS date_paiement)
            String insert = "INSERT INTO paiement (montant, statut) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insert);
            pstmt.setBigDecimal(1, new java.math.BigDecimal("199.99"));
            pstmt.setString(2, "actif");

            int rows = pstmt.executeUpdate();
            System.out.println("\n✅ Insertion réussie! Lignes affectées: " + rows);

            // 5. Voir le résultat
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM paiement ORDER BY id_paiement DESC");
            System.out.println("\n📋 Paiements dans la base:");
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.println("   ID: " + rs.getInt("id_paiement") +
                        " | Montant: " + rs.getBigDecimal("montant") +
                        " | Service: " + rs.getInt("id_service") +
                        " | Produit: " + rs.getInt("id_produit") +
                        " | Statut: " + rs.getString("statut"));
            }
            if (!hasData) {
                System.out.println("   ⚠️ Aucune donnée dans la table");
            }

        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver non trouvé: " + e.getMessage());
            System.err.println("   Téléchargez mysql-connector-java");
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            System.err.println("   Code erreur: " + e.getErrorCode());

            if (e.getMessage().contains("Unknown database")) {
                System.err.println("\n🔧 Solution: Créez la base 'esprit'");
                System.err.println("   CREATE DATABASE esprit;");
            } else if (e.getMessage().contains("Access denied")) {
                System.err.println("\n🔧 Solution: Vérifiez user/password");
                System.err.println("   user=root, password='' (vide)");
            } else if (e.getMessage().contains("doesn't exist")) {
                System.err.println("\n🔧 Solution: Créez la table 'paiement'");
                System.err.println("""
                   CREATE TABLE paiement (
                       id_paiement INT PRIMARY KEY AUTO_INCREMENT,
                       montant DECIMAL(10,2) NOT NULL,
                       id_service INT NULL,
                       id_produit INT NULL,
                       statut VARCHAR(20) DEFAULT 'actif'
                   );
                """);
            }
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
}