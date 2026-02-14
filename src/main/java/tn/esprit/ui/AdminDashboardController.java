package tn.esprit.ui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.db.Db;
import tn.esprit.domain.Role;
import tn.esprit.domain.User;
import tn.esprit.repository.JdbcUserDao;

import java.sql.Connection;

public class AdminDashboardController {

    @FXML private Label adminCountLabel;
    @FXML private Label clientCountLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Long> idCol;
    @FXML private TableColumn<User, String> fullNameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, Role> roleCol;
    @FXML private TableColumn<User, Boolean> activeCol;

    @FXML
    public void initialize() {
        if (idCol != null) idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (fullNameCol != null) fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        if (emailCol != null) emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (roleCol != null) roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        if (activeCol != null) activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));

        refresh();
    }

    @FXML
    public void onRefresh(ActionEvent e) {
        refresh();
    }

    private void refresh() {
        if (statusLabel != null) statusLabel.setText("");

        try (Connection cn = Db.getConnection()) {
            JdbcUserDao dao = new JdbcUserDao(cn);

            long admins = dao.countByRole(Role.ADMIN);
            long clients = dao.countByRole(Role.CLIENT);

            if (adminCountLabel != null) adminCountLabel.setText(String.valueOf(admins));
            if (clientCountLabel != null) clientCountLabel.setText(String.valueOf(clients));

            if (usersTable != null) {
                usersTable.setItems(FXCollections.observableArrayList(dao.findAll()));
            }
        } catch (Exception ex) {
            if (statusLabel != null) statusLabel.setText("Erreur: " + ex.getMessage());
        }
    }

    @FXML
    public void onLogout(ActionEvent e) {
        AppState.clear();
        SceneNavigator.goLogin();
    }
}
