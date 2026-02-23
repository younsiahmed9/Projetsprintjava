package tn.esprit;

import org.junit.jupiter.api.*;
import tn.esprit.db.Db;
import tn.esprit.domain.User;
import tn.esprit.domain.UserType;
import tn.esprit.repository.JdbcUserDao;

import java.sql.Connection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test CRUD "statique" (sans mocks) sur une vraie base MySQL (XAMPP).
 * Prérequis:
 * 1) Lancer MySQL dans XAMPP
 * 2) Exécuter src/main/resources/sql/fintrack_schema.sql
 * 3) Vérifier src/main/resources/application.properties (user/password)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserCrudStaticTest {

    private static long insertedId;

    @Test
    @Order(1)
    void insert_user() throws Exception {
        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);

            User u = new User(null,
                    "client1@fintrack.tn",
                    "hash-demo-123",
                    "Client One",
                    UserType.CLIENT,
                    true);

            insertedId = dao.insert(u);
            assertTrue(insertedId > 0);
        }
    }

    @Test
    @Order(2)
    void select_user_by_id() throws Exception {
        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);

            Optional<User> u = dao.findById(insertedId);
            assertTrue(u.isPresent());
            assertEquals("client1@fintrack.tn", u.get().getEmail());
        }
    }

    @Test
    @Order(3)
    void update_user() throws Exception {
        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);

            User u = dao.findById(insertedId).orElseThrow();
            u.setFullName("Client One Updated");
            u.setActive(false);

            assertTrue(dao.update(u));

            User after = dao.findById(insertedId).orElseThrow();
            assertEquals("Client One Updated", after.getFullName());
            assertFalse(after.isActive());
        }
    }

    @Test
    @Order(4)
    void delete_user() throws Exception {
        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);
            assertTrue(dao.deleteById(insertedId));
            assertTrue(dao.findById(insertedId).isEmpty());
        }
    }
}
