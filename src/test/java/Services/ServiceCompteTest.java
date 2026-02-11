package Services;

import Models.Compte;
import org.junit.jupiter.api.*;

import java.sql.SQLDataException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceCompteTest {
    static ServiceCompte service;
    @BeforeAll
    static void setUp() { service  = new ServiceCompte(); }
    @Order(1)
    @Test
    void testAjouterCompte() {

        Compte c = new Compte(
                "ACC0010",
                5500,
                "EPARGNE",
                3.5,        // taux interet
                null,       // pas de plafond
                LocalDate.now(),
                "ACTIF"
        );

        try {
            service.ajouter(c);

            List<Compte> comptes = service.recuperer();

            assertFalse(comptes.isEmpty());
            assertTrue(
                    comptes.stream()
                            .anyMatch(compte ->
                                    compte.getNumeroCompte().equals("ACC0010")
                                            && compte.getTypeCompte().equals("EPARGNE"))
            );

        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
    }

    @Order(2)
    @Test
    void testModifierCompte() {

        try {

            // 1️⃣ Ajouter un compte à modifier
            Compte original = new Compte(
                    "ACC0011",
                    8000,
                    "COURANT",
                    null,
                    2000.0,
                    LocalDate.now(),
                    "ACTIF"
            );

            service.ajouter(original);

            // 2️⃣ Récupérer l'id du compte ajouté
            Compte compteAjoute = service.recuperer().stream()
                    .filter(c -> c.getNumeroCompte().equals("ACC0011"))
                    .findFirst()
                    .orElseThrow();

            // 3️⃣ Modifier
            Compte modifie = new Compte(
                    compteAjoute.getIdCompte(),
                    "ACC0011-MOD",
                    15000,
                    "COURANT",
                    null,
                    3000.0,
                    LocalDate.now(),
                    "ACTIF"
            );

            service.modifier(modifie);

            // 4️⃣ Vérifier
            List<Compte> comptes = service.recuperer();

            assertTrue(
                    comptes.stream()
                            .anyMatch(compte ->
                                    compte.getNumeroCompte().equals("ACC0011-MOD")
                                            && compte.getSolde() == 15000
                                            && compte.getPlafondDecouvert() == 3000.0)
            );

        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
    }


}
