package Services;

import Models.*;
import utils.MyDataBase;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CarteVirtuelleServiceTest {

    private static PortefeuilleService portefeuilleService;
    private static CarteVirtuelleService carteService;
    private static Portefeuille portefeuilleTest;
    private static int portefeuilleId = 0; // Initialisé à 0

    @BeforeAll
    public static void setUpClass() {
        portefeuilleService = new PortefeuilleService();
        carteService = new CarteVirtuelleService();

        // 1. NETTOYER la base
        nettoyerBase();

        // 2. CRÉER un portefeuille pour les tests
        portefeuilleTest = new Portefeuille("Portefeuille Test Carte", 5000.0, "DT");
        portefeuilleService.ajouter(portefeuilleTest);

        // 3. RÉCUPÉRER son ID
        List<Portefeuille> list = portefeuilleService.afficherTous();
        if (!list.isEmpty()) {
            // Prendre le dernier portefeuille ajouté
            portefeuilleId = list.get(list.size() - 1).getId();
            System.out.println("✅ Portefeuille ID pour tests: " + portefeuilleId);
        } else {
            // Fallback: créer un autre portefeuille
            Portefeuille p = new Portefeuille("Portefeuille Secours", 1000.0, "DT");
            portefeuilleService.ajouter(p);
            list = portefeuilleService.afficherTous();
            portefeuilleId = list.get(list.size() - 1).getId();
            System.out.println("✅ Portefeuille ID (secours): " + portefeuilleId);
        }

        // 4. Vérifier que l'ID est valide
        assertTrue(portefeuilleId > 0, "❌ Impossible de créer un portefeuille pour les tests!");
    }

    @BeforeEach
    public void setUp() {
        // Nettoyer seulement les cartes avant chaque test
        nettoyerCartes();
    }

    // ✅ NETTOYER BASE - SANS try-with-resources
    private static void nettoyerBase() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = MyDataBase.getInstance().getConnection();
            stmt = conn.createStatement();

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("DELETE FROM transaction");
            stmt.execute("DELETE FROM carte_virtuelle");
            stmt.execute("DELETE FROM portefeuille");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

            System.out.println("✅ Base nettoyée");
        } catch (Exception e) {
            System.err.println("❌ Erreur nettoyage base: " + e.getMessage());
        }
    }

    // ✅ NETTOYER CARTES - SANS try-with-resources
    private static void nettoyerCartes() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = MyDataBase.getInstance().getConnection();
            stmt = conn.createStatement();

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("DELETE FROM transaction");
            stmt.execute("DELETE FROM carte_virtuelle");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

            System.out.println("✅ Cartes nettoyées");
        } catch (Exception e) {
            System.err.println("❌ Erreur nettoyage cartes: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    @DisplayName("✅ Test 1: Ajouter une carte NORMAL")
    public void testAjouterCarteNormal() {
        System.out.println("\n🧪 TEST 1: Ajout carte NORMAL");
        assertTrue(portefeuilleId > 0, "portefeuilleId invalide!");

        CarteVirtuelle carte = new CarteVirtuelle(
                1000.0, 2000.0,
                TypeCarte.NORMAL,
                Devise.DT,
                portefeuilleId
        );

        carteService.ajouter(carte);

        List<CarteVirtuelle> cartes = carteService.getCartesByPortefeuille(portefeuilleId);
        assertFalse(cartes.isEmpty(), "La carte devrait être ajoutée");
        System.out.println("✅ Test ajout carte NORMAL réussi");
    }

    @Test
    @Order(2)
    @DisplayName("✅ Test 2: Ajouter une carte GOLD")
    public void testAjouterCarteGold() {
        System.out.println("\n🧪 TEST 2: Ajout carte GOLD");
        assertTrue(portefeuilleId > 0, "portefeuilleId invalide!");

        CarteVirtuelle carte = new CarteVirtuelle(
                2000.0, 5000.0,
                TypeCarte.GOLD,
                Devise.USD,
                portefeuilleId
        );

        carteService.ajouter(carte);

        List<CarteVirtuelle> cartes = carteService.getCartesByPortefeuille(portefeuilleId);
        assertFalse(cartes.isEmpty(), "La carte devrait être ajoutée");
        System.out.println("✅ Test ajout carte GOLD réussi");
    }

    @Test
    @Order(3)
    @DisplayName("✅ Test 3: Ajouter une carte SILVER")
    public void testAjouterCarteSilver() {
        System.out.println("\n🧪 TEST 3: Ajout carte SILVER");
        assertTrue(portefeuilleId > 0, "portefeuilleId invalide!");

        CarteVirtuelle carte = new CarteVirtuelle(
                500.0, 1000.0,
                TypeCarte.SILVER,
                Devise.EUR,
                portefeuilleId
        );

        carteService.ajouter(carte);

        List<CarteVirtuelle> cartes = carteService.getCartesByPortefeuille(portefeuilleId);
        assertFalse(cartes.isEmpty(), "La carte devrait être ajoutée");
        System.out.println("✅ Test ajout carte SILVER réussi");
    }

    @Test
    @Order(4)
    @DisplayName("✅ Test 4: Afficher toutes les cartes")
    public void testAfficherTous() {
        System.out.println("\n🧪 TEST 4: Afficher toutes les cartes");
        assertTrue(portefeuilleId > 0, "portefeuilleId invalide!");

        // Vider d'abord
        nettoyerCartes();

        // Ajouter 3 cartes
        carteService.ajouter(new CarteVirtuelle(100, 1000, TypeCarte.NORMAL, Devise.DT, portefeuilleId));
        carteService.ajouter(new CarteVirtuelle(200, 2000, TypeCarte.GOLD, Devise.USD, portefeuilleId));
        carteService.ajouter(new CarteVirtuelle(300, 3000, TypeCarte.SILVER, Devise.EUR, portefeuilleId));

        // Afficher et vérifier
        List<CarteVirtuelle> cartes = carteService.afficherTous();
        assertNotNull(cartes, "La liste ne devrait pas être null");
        assertTrue(cartes.size() >= 3, "Devrait avoir au moins 3 cartes");

        System.out.println("✅ Test afficher toutes les cartes réussi (" + cartes.size() + " cartes)");
    }

    @Test
    @Order(5)
    @DisplayName("✅ Test 5: Chercher carte par ID")
    public void testAfficherParId() {
        System.out.println("\n🧪 TEST 5: Chercher carte par ID");
        assertTrue(portefeuilleId > 0, "portefeuilleId invalide!");

        // Ajouter une carte
        CarteVirtuelle carte = new CarteVirtuelle(777, 1500, TypeCarte.GOLD, Devise.DT, portefeuilleId);
        carteService.ajouter(carte);

        // Récupérer son ID
        List<CarteVirtuelle> cartes = carteService.getCartesByPortefeuille(portefeuilleId);
        assertFalse(cartes.isEmpty(), "La carte devrait être ajoutée");

        int id = cartes.get(cartes.size() - 1).getId();
        assertTrue(id > 0, "ID devrait être positif");

        // Chercher par ID
        CarteVirtuelle trouve = carteService.afficherParId(id);
        assertNotNull(trouve, "La carte devrait être trouvée");
        assertEquals(id, trouve.getId(), "Les IDs devraient correspondre");
        assertEquals(777.0, trouve.getSolde(), 0.001, "Le solde devrait être 777");
        assertEquals(TypeCarte.GOLD, trouve.getType(), "Le type devrait être GOLD");

        System.out.println("✅ Test chercher carte par ID réussi (ID: " + id + ")");
    }

    @Test
    @Order(6)
    @DisplayName("✅ Test 6: Modifier une carte")
    public void testModifierCarte() {
        System.out.println("\n🧪 TEST 6: Modifier une carte");
        assertTrue(portefeuilleId > 0, "portefeuilleId invalide!");

        // Ajouter une carte
        CarteVirtuelle carte = new CarteVirtuelle(100, 1000, TypeCarte.NORMAL, Devise.DT, portefeuilleId);
        carteService.ajouter(carte);

        // Récupérer son ID
        List<CarteVirtuelle> cartes = carteService.getCartesByPortefeuille(portefeuilleId);
        assertFalse(cartes.isEmpty(), "La carte devrait être ajoutée");

        CarteVirtuelle carteModif = cartes.get(cartes.size() - 1);
        carteModif.setSolde(999.0);
        carteModif.setPlafond(5000.0);
        carteModif.setType(TypeCarte.GOLD);

        carteService.modifier(carteModif);

        // Vérifier
        CarteVirtuelle verif = carteService.afficherParId(carteModif.getId());
        assertNotNull(verif, "La carte devrait exister");
        assertEquals(999.0, verif.getSolde(), 0.001, "Solde modifié");
        assertEquals(5000.0, verif.getPlafond(), 0.001, "Plafond modifié");
        assertEquals(TypeCarte.GOLD, verif.getType(), "Type modifié");

        System.out.println("✅ Test modifier carte réussi");
    }

    @Test
    @Order(7)
    @DisplayName("✅ Test 7: Recharger une carte")
    public void testRechargerCarte() {
        System.out.println("\n🧪 TEST 7: Recharger une carte");
        assertTrue(portefeuilleId > 0, "portefeuilleId invalide!");

        // Ajouter une carte
        CarteVirtuelle carte = new CarteVirtuelle(500, 2000, TypeCarte.NORMAL, Devise.DT, portefeuilleId);
        carteService.ajouter(carte);

        // Récupérer son ID et solde
        List<CarteVirtuelle> cartes = carteService.getCartesByPortefeuille(portefeuilleId);
        assertFalse(cartes.isEmpty(), "La carte devrait être ajoutée");

        int id = cartes.get(cartes.size() - 1).getId();
        double soldeAvant = cartes.get(cartes.size() - 1).getSolde();

        // Recharger
        double montant = 300.0;
        carteService.recharger(id, montant);

        // Vérifier
        CarteVirtuelle apres = carteService.afficherParId(id);
        assertNotNull(apres, "La carte devrait exister");
        assertEquals(soldeAvant + montant, apres.getSolde(), 0.001, "Le solde devrait être augmenté");

        System.out.println("✅ Test recharger carte réussi");
    }

    @Test
    @Order(8)
    @DisplayName("✅ Test 8: Cartes par portefeuille")
    public void testGetCartesByPortefeuille() {
        System.out.println("\n🧪 TEST 8: Cartes par portefeuille");

        // ✅ VÉRIFIER que portefeuilleId est valide
        assertTrue(portefeuilleId > 0, "❌ portefeuilleId devrait être > 0");
        System.out.println("✅ Utilisation portefeuille ID: " + portefeuilleId);

        // ✅ VIDER les cartes existantes
        nettoyerCartes();

        // ✅ AJOUTER 3 cartes
        carteService.ajouter(new CarteVirtuelle(100, 1000, TypeCarte.NORMAL, Devise.DT, portefeuilleId));
        carteService.ajouter(new CarteVirtuelle(200, 2000, TypeCarte.GOLD, Devise.USD, portefeuilleId));
        carteService.ajouter(new CarteVirtuelle(300, 3000, TypeCarte.SILVER, Devise.EUR, portefeuilleId));

        // ✅ VÉRIFIER que les cartes sont ajoutées
        List<CarteVirtuelle> cartes = carteService.getCartesByPortefeuille(portefeuilleId);

        // ✅ AFFICHER pour debug
        System.out.println("📋 Cartes trouvées: " + cartes.size());
        for (CarteVirtuelle c : cartes) {
            System.out.println("   " + c);
        }

        // ✅ ASSERTIONS
        assertNotNull(cartes, "La liste ne devrait pas être null");
        assertEquals(3, cartes.size(), "Devrait avoir 3 cartes");

        for (CarteVirtuelle c : cartes) {
            assertEquals(portefeuilleId, c.getPortefeuilleId(), "La carte devrait appartenir au bon portefeuille");
        }

        System.out.println("✅ Test cartes par portefeuille réussi (" + cartes.size() + " cartes)");
    }

    @Test
    @Order(9)
    @DisplayName("✅ Test 9: Supprimer une carte")
    public void testSupprimerCarte() {
        System.out.println("\n🧪 TEST 9: Supprimer une carte");
        assertTrue(portefeuilleId > 0, "portefeuilleId invalide!");

        // Ajouter une carte
        CarteVirtuelle carte = new CarteVirtuelle(123, 456, TypeCarte.NORMAL, Devise.DT, portefeuilleId);
        carteService.ajouter(carte);

        // Récupérer son ID
        List<CarteVirtuelle> cartes = carteService.getCartesByPortefeuille(portefeuilleId);
        assertFalse(cartes.isEmpty(), "La carte devrait être ajoutée");

        int id = cartes.get(cartes.size() - 1).getId();

        // Supprimer
        carteService.supprimer(id);

        // Vérifier
        CarteVirtuelle verif = carteService.afficherParId(id);
        assertNull(verif, "La carte devrait être supprimée");

        System.out.println("✅ Test supprimer carte réussi");
    }

    @Test
    @Order(10)
    @DisplayName(" Test 10: Ajout carte avec portefeuille inexistant")
    public void testAjoutCartePortefeuilleInexistant() {
        System.out.println("\n🧪 TEST 10: Ajout carte avec portefeuille inexistant");

        int mauvaisId = 99999;
        System.out.println(" Tentative avec ID inexistant: " + mauvaisId);

        CarteVirtuelle carte = new CarteVirtuelle(
                1000, 2000,
                TypeCarte.NORMAL,
                Devise.DT,
                mauvaisId
        );

        // Cette méthode devrait afficher une erreur mais pas planter
        carteService.ajouter(carte);

        // Vérifier qu'aucune carte n'a été ajoutée
        List<CarteVirtuelle> cartes = carteService.getCartesByPortefeuille(mauvaisId);
        assertTrue(cartes.isEmpty(), "Aucune carte ne devrait être ajoutée");

        System.out.println("✅ Test ajout carte portefeuille inexistant réussi");
    }
}