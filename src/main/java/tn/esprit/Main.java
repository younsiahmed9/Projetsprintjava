package tn.esprit;

import utils.Db;
import Models.Client;
import Models.User;
import Models.Role;
import Services.JdbcClientDao;
import Services.JdbcUserDao;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("FinTrack - démarrage");

        try (Connection cn = Db.getConnection()) {
            JdbcUserDao userDao = new JdbcUserDao(cn);
            JdbcClientDao clientDao = new JdbcClientDao(cn);

            upsertClient(userDao, clientDao,
                    "ghaith@fintrack.tn",
                    "ghaith",
                    "CIN123456",
                    "+216 20 000 000");

            upsertClient(userDao, clientDao,
                    "abdou@fintrack.tn",
                    "abdou",
                    "CIN222222",
                    "+216 22 222 222");
        }
    }

    private static void upsertClient(JdbcUserDao userDao,
                                    JdbcClientDao clientDao,
                                    String email,
                                    String fullName,
                                    String cin,
                                    String phone) throws Exception {

        // 1) users: create si absent
        long userId = userDao.findByEmail(email)
                .map(User::getId)
                .orElseGet(() -> {
                    try {
                        User u = new User(null, email, "hash-demo", fullName, Role.CLIENT, true);
                        return userDao.insert(u);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        // 2) clients: create si absent
        if (!clientDao.existsByUserId(userId)) {
            clientDao.insert(new Client(userId, cin, phone));
            System.out.println("Client ajouté dans users+clients: user_id=" + userId + ", nom=" + fullName);
        } else {
            System.out.println("Client déjà existant: user_id=" + userId + ", nom=" + fullName);
        }
    }
}
