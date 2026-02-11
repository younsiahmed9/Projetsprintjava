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
        Depense d = new Depense(
                4, // ⚠️ id_depense existant en base
                1,
                2,
                "Transport",
                80.00,
                new Date(),
                "Taxi",
                "cash"
        );

        try {
            service.modifier(d);
            List<Depense> depenses = service.recuperer();
            assertTrue(depenses.stream()
                    .anyMatch(depense -> depense.getCategorie().equals("Transport")));
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
    }

    @Order(3)
    @Test
    void testSupprimerDepense() {
        try {
            service.supprimer(4); // ⚠️ id_depense existant
            List<Depense> depenses = service.recuperer();
            assertTrue(depenses.stream()
                    .noneMatch(depense -> depense.getIdDepense() == 4));
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
    }
}
