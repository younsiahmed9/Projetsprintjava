package tn.esprit;

import tn.esprit.db.Db;
import tn.esprit.domain.Role;
import tn.esprit.domain.User;
import tn.esprit.repository.JdbcUserDao;

import java.sql.Connection;

/**
 * Runner simple (sans JUnit) pour tester le CRUD sur MySQL (XAMPP).
 * Prérequis:
 * - MySQL démarré
 * - Script SQL exécuté (src/main/resources/sql/fintrack_schema.sql)
 * - application.properties correctement configuré
 */
public class UserCrudStaticRunner {
    public static void main(String[] args) throws Exception {
        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);

            // CREATE
            User u = new User(null,
                    "runner_client@fintrack.tn",
                    "hash-runner-123",
                    "Runner Client",
                    Role.CLIENT,
                    true);
            long id = dao.insert(u);
            System.out.println("Inserted user id=" + id);

            // READ
            User fromDb = dao.findById(id).orElseThrow();
            System.out.println("Read email=" + fromDb.getEmail());

            // UPDATE
            fromDb.setFullName("Runner Client Updated");
            fromDb.setActive(false);
            dao.update(fromDb);
            System.out.println("Updated user id=" + id);

            // DELETE
            dao.deleteById(id);
            System.out.println("Deleted user id=" + id);
        }
    }
}
