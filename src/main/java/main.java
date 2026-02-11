import Services.PortefeuilleService;
import Models.Portefeuille;
import utils.MyDataBase;

public class main {
    public static void main(String[] args) {
        System.out.println("🧪 TEST RAPIDE D'AJOUT");
        System.out.println("-".repeat(40));

        PortefeuilleService service = new PortefeuilleService();

        // Test 1: Ajout DT
        Portefeuille p1 = new Portefeuille("Test DT", 1000.0, "DT");
        service.ajouter(p1);

        // Test 2: Ajout USD
        Portefeuille p2 = new Portefeuille("Test USD", 500.0, "USD");
        service.ajouter(p2);

        // Test 3: Ajout EUR
        Portefeuille p3 = new Portefeuille("Test EUR", 300.0, "EUR");
        service.ajouter(p3);

        // Afficher résultat
        System.out.println("\n📋 Résultat final:");
        service.afficherTous();
    }
}