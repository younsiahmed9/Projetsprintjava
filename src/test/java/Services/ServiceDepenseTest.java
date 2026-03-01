package Services;

import Models.Depense;
import org.junit.jupiter.api.*;

import java.sql.SQLDataException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceDepenseTest {
    static ServiceDepense service;
    private static Integer depenseIdForCleanup;

    @BeforeAll
    static void setUp() {
        service = new ServiceDepense();
    }

    @Order(1)
    @Test
    void testAjouterDepense() {
        Depense d = new Depense(
                0,
                1,
                2, // ⚠️ doit correspondre à un budget existant
                "Alimentation",
                150.00,
                new Date(),
                "Courses supermarché",
                "carte"
        );

        try {
            service.ajouter(d);
            List<Depense> depenses = service.recuperer();
            assertFalse(depenses.isEmpty());
            assertTrue(depenses.stream()
                    .anyMatch(depense -> depense.getDescription().equals("Courses supermarché")));
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
    }

    @Order(2)
    @Test
    void testModifierDepense() {
        String description = "TaxiTest-" + System.currentTimeMillis();

        Depense aCreer = new Depense(
                0,
                1,
                2,
                "Init",
                80.00,
                new Date(),
                description,
                "cash"
        );

        try {
            service.ajouter(aCreer);
            List<Depense> depenses = service.recuperer();

            Depense cree = depenses.stream()
                    .filter(depense -> description.equals(depense.getDescription()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Depense de test introuvable"));

            depenseIdForCleanup = cree.getIdDepense();

            Depense aModifier = new Depense(
                    cree.getIdDepense(),
                    cree.getIdUtilisateur(),
                    cree.getIdBudget(),
                    "Transport",
                    90.00,
                    new Date(),
                    cree.getDescription(),
                    cree.getModePaiement()
            );

            service.modifier(aModifier);
            List<Depense> apres = service.recuperer();
            assertTrue(apres.stream()
                    .anyMatch(depense -> depense.getIdDepense() == cree.getIdDepense()
                            && "Transport".equals(depense.getCategorie())));
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
    }

    @Order(3)
    @Test
    void testSupprimerDepense() {
        try {
            if (depenseIdForCleanup == null) {
                Depense d = new Depense(
                        0,
                        1,
                        2,
                        "Temp",
                        10.00,
                        new Date(),
                        "TempDelete-" + System.currentTimeMillis(),
                        "cash"
                );
                service.ajouter(d);
                depenseIdForCleanup = service.recuperer().stream()
                        .filter(depense -> depense.getDescription().startsWith("TempDelete-"))
                        .findFirst()
                        .map(Depense::getIdDepense)
                        .orElse(null);
            }

            assertNotNull(depenseIdForCleanup, "ID de depense manquant pour suppression");
            service.supprimer(depenseIdForCleanup);
            List<Depense> depenses = service.recuperer();
            assertTrue(depenses.stream()
                    .noneMatch(depense -> depense.getIdDepense() == depenseIdForCleanup));
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
    }
}
