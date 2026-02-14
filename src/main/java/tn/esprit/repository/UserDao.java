package tn.esprit.repository;

import tn.esprit.domain.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    long insert(User user) throws SQLException;

    Optional<User> findById(long id) throws SQLException;

    Optional<User> findByEmail(String email) throws SQLException;

    List<User> findAll() throws SQLException;

    boolean update(User user) throws SQLException;

    boolean deleteById(long id) throws SQLException;
}
