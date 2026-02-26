package Controllers;

import Models.Utilisateur;
import Models.Portefeuille;
import Models.CarteVirtuelle;
import Services.UtilisateurService;
import Services.PortefeuilleService;
import Services.CarteVirtuelleService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label lblNbUtilisateurs, lblNbPortefeuilles, lblNbCartes;
    @FXML private BarChart<String, Number> chartTypes;
    @FXML private VBox containerUtilisateurs;

    private UtilisateurService utilisateurService = new UtilisateurService();
    private PortefeuilleService portefeuilleService = new PortefeuilleService();
    private CarteVirtuelleService carteService = new CarteVirtuelleService();
    private ObservableList<Utilisateur> utilisateurs = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadStats();
        loadUtilisateurs();
        loadChart();
    }

    private void loadStats() {
        List<Utilisateur> allUsers = utilisateurService.afficherTous();
        utilisateurs.setAll(allUsers);
        lblNbUtilisateurs.setText(String.valueOf(allUsers.size()));

        List<Portefeuille> allPortefeuilles = portefeuilleService.afficherTous();
        lblNbPortefeuilles.setText(String.valueOf(allPortefeuilles.size()));

        List<CarteVirtuelle> allCartes = carteService.afficherTous();
        lblNbCartes.setText(String.valueOf(allCartes.size()));
    }

    private void loadUtilisateurs() {
        containerUtilisateurs.getChildren().clear();
        for (Utilisateur u : utilisateurs) {
            HBox row = createUserRow(u);
            containerUtilisateurs.getChildren().add(row);
        }
    }

    private HBox createUserRow(Utilisateur u) {
        HBox row = new HBox(0);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPrefHeight(50);
        row.getStyleClass().add("custom-row-credit");

        Label idLabel = new Label(String.valueOf(u.getId()));
        idLabel.setMinWidth(60);
        idLabel.setPrefWidth(60);
        idLabel.setMaxWidth(60);

        Label nomLabel = new Label(u.getNom() + " " + u.getPrenom());
        nomLabel.setMinWidth(150);
        nomLabel.setPrefWidth(150);
        nomLabel.setMaxWidth(150);

        Label emailLabel = new Label(u.getEmail());
        emailLabel.setMinWidth(200);
        emailLabel.setPrefWidth(200);
        emailLabel.setMaxWidth(200);

        Label roleLabel = new Label(u.getRole());
        roleLabel.setMinWidth(80);
        roleLabel.setPrefWidth(80);
        roleLabel.setMaxWidth(80);
        roleLabel.getStyleClass().addAll("status-badge", "admin".equals(u.getRole()) ? "status-active" : "status-pending");

        Label soldeLabel = new Label(String.format("%.2f DT", u.getSolde()));
        soldeLabel.setMinWidth(120);
        soldeLabel.setPrefWidth(120);
        soldeLabel.setMaxWidth(120);

        Region spacer = new Region();                     // ← Import ajouté
        HBox.setHgrow(spacer, Priority.ALWAYS);           // ← Correction : Priority.ALWAYS

        Button editBtn = new Button("✏️");
        editBtn.getStyleClass().add("btn-action-edit");
        editBtn.setOnAction(e -> modifierUtilisateur(u));

        Button deleteBtn = new Button("🗑️");
        deleteBtn.getStyleClass().add("btn-action-delete");
        deleteBtn.setOnAction(e -> supprimerUtilisateur(u));

        HBox actions = new HBox(10, editBtn, deleteBtn);
        actions.setMinWidth(100);
        actions.setPrefWidth(100);
        actions.setMaxWidth(100);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        row.getChildren().addAll(idLabel, nomLabel, emailLabel, roleLabel, soldeLabel, spacer, actions);
        return row;
    }

    private void loadChart() {
        List<CarteVirtuelle> allCartes = carteService.afficherTous();
        Map<String, Long> typeCount = allCartes.stream()
                .collect(Collectors.groupingBy(c -> c.getType().toString(), Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        typeCount.forEach((type, count) -> series.getData().add(new XYChart.Data<>(type, count)));
        chartTypes.getData().clear();
        chartTypes.getData().add(series);
    }

    private void modifierUtilisateur(Utilisateur u) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Modification de l'utilisateur " + u.getEmail() + " (à implémenter)");
        alert.showAndWait();
    }

    private void supprimerUtilisateur(Utilisateur u) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer l'utilisateur " + u.getEmail() + " ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                utilisateurService.supprimer(u.getId());
                loadStats();
                loadUtilisateurs();
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadStats();
        loadUtilisateurs();
        loadChart();
    }

    @FXML
    private void handleUtilisateurs() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Page de gestion des utilisateurs (à implémenter)");
        alert.showAndWait();
    }

    @FXML
    private void handlePortefeuilles() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/resources/views/admin_portefeuilles.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCartes() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/resources/views/admin_cartes.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        Models.Session.clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}