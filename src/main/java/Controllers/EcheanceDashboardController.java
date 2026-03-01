package Controllers;

import Models.Document;
import Models.Echeance;
import Services.ServiceEcheance;
import Controllers.AlertUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour le tableau de bord des échéances
 */
public class EcheanceDashboardController {

    @FXML
    private TableView<EcheanceRow> tableEcheances;
    @FXML
    private TableColumn<EcheanceRow, String> colDocument;
    @FXML
    private TableColumn<EcheanceRow, String> colType;
    @FXML
    private TableColumn<EcheanceRow, LocalDate> colDate;
    @FXML
    private TableColumn<EcheanceRow, String> colJoursRestants;
    @FXML
    private TableColumn<EcheanceRow, String> colUrgence;
    @FXML
    private TableColumn<EcheanceRow, String> colStatut;
    @FXML
    private TableColumn<EcheanceRow, Void> colActions;

    @FXML
    private Label lblTotalEcheances;
    @FXML
    private Label lblEcheancesEnRetard;
    @FXML
    private Label lblEcheancesUrgentes;
    @FXML
    private Label lblEcheancesAVenir;

    @FXML
    private ComboBox<String> cbFiltre;
    @FXML
    private Button btnRefresh;
    @FXML
    private Button btnAddEcheance;

    private final ServiceEcheance echeanceService;
    private ObservableList<EcheanceRow> echeancesList;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public EcheanceDashboardController() {
        try {
            this.echeanceService = new ServiceEcheance();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible d'initialiser le service d'échéances: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        setupButtons();
        loadEcheances();
        updateStatistics();
    }

    private void setupTable() {
        // Configuration des colonnes
        colDocument.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getEcheance().getDocument().getTitre()));

        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEcheance().getTypeEcheance()));

        colDate.setCellValueFactory(
                data -> new SimpleObjectProperty<>(data.getValue().getEcheance().getDateEcheance()));
        colDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DATE_FORMATTER));
                }
            }
        });

        colJoursRestants.setCellValueFactory(data -> {
            long jours = data.getValue().getEcheance().getJoursRestants();
            String text = jours >= 0 ? jours + " jours" : "En retard de " + Math.abs(jours) + " jours";
            return new SimpleStringProperty(text);
        });

        colUrgence.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEcheance().getUrgence()));
        colUrgence.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String urgence, boolean empty) {
                super.updateItem(urgence, empty);
                if (empty || urgence == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(urgence);
                    if (urgence.contains("EXPIREE")) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    } else if (urgence.contains("URGENT") || urgence.contains("PROCHE")) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    } else if (urgence.contains("AUJOURD'HUI")) {
                        setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEcheance().getStatut()));

        // Colonne actions
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnTerminer = new Button("✓");
            private final Button btnSupprimer = new Button("✕");

            {
                btnTerminer.getStyleClass().add("btn-success");
                btnTerminer.setStyle("-fx-min-width: 30; -fx-font-size: 12;");
                btnTerminer.setOnAction(e -> {
                    EcheanceRow row = getTableView().getItems().get(getIndex());
                    marquerTerminee(row.getEcheance());
                });

                btnSupprimer.getStyleClass().add("btn-delete");
                btnSupprimer.setStyle("-fx-min-width: 30; -fx-font-size: 12;");
                btnSupprimer.setOnAction(e -> {
                    EcheanceRow row = getTableView().getItems().get(getIndex());
                    supprimerEcheance(row.getEcheance());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5, btnTerminer, btnSupprimer);
                    setGraphic(box);
                }
            }
        });
    }

    private void setupFilters() {
        cbFiltre.setItems(FXCollections.observableArrayList(
                "Toutes",
                "En retard",
                "Urgentes (7 jours)",
                "À venir (30 jours)",
                "Terminées"));
        cbFiltre.setValue("Toutes");
        cbFiltre.setOnAction(e -> filterEcheances());
    }

    private void setupButtons() {
        btnRefresh.setOnAction(e -> {
            loadEcheances();
            updateStatistics();
        });

        if (btnAddEcheance != null) {
            btnAddEcheance.setOnAction(e -> ajouterEcheance());
        }
    }

    private void loadEcheances() {
        try {
            List<Echeance> echeances = echeanceService.findAll();
            echeancesList = FXCollections.observableArrayList();

            for (Echeance echeance : echeances) {
                echeancesList.add(new EcheanceRow(echeance));
            }

            tableEcheances.setItems(echeancesList);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les échéances: " + e.getMessage());
        }
    }

    private void filterEcheances() {
        String filtre = cbFiltre.getValue();

        try {
            List<Echeance> filtered;

            switch (filtre) {
                case "En retard":
                    filtered = echeanceService.findOverdue();
                    break;
                case "Urgentes (7 jours)":
                    filtered = echeanceService.findInNextDays(7);
                    break;
                case "À venir (30 jours)":
                    filtered = echeanceService.findInNextDays(30);
                    break;
                case "Terminées":
                    filtered = echeanceService.findAll().stream()
                            .filter(e -> e.getStatut().equals("VUE"))
                            .collect(Collectors.toList());
                    break;
                default: // Toutes
                    filtered = echeanceService.findAll();
                    break;
            }

            echeancesList.clear();
            for (Echeance echeance : filtered) {
                echeancesList.add(new EcheanceRow(echeance));
            }

        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de filtrer les échéances: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        try {
            ServiceEcheance.EcheanceStats stats = echeanceService.getStatistics();

            lblTotalEcheances.setText(String.valueOf(stats.getTotal()));
            lblEcheancesEnRetard.setText(String.valueOf(stats.getOverdue()));

            try {
                lblEcheancesUrgentes.setText(String.valueOf(
                        echeanceService.findInNextDays(7).size() - stats.getOverdue()));
            } catch (SQLException e) {
                lblEcheancesUrgentes.setText("N/A");
            }

            lblEcheancesAVenir.setText(String.valueOf(stats.getUpcoming()));

        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de calculer les statistiques");
        }
    }

    private void marquerTerminee(Echeance echeance) {
        try {
            echeanceService.markAsCompleted(echeance.getId());
            AlertUtils.showSuccess("Succès", "Échéance marquée comme terminée");
            loadEcheances();
            updateStatistics();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de mettre à jour l'échéance: " + e.getMessage());
        }
    }

    private void supprimerEcheance(Echeance echeance) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'échéance");
        confirmation.setContentText("Voulez-vous vraiment supprimer cette échéance ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    echeanceService.delete(echeance.getId());
                    AlertUtils.showSuccess("Succès", "Échéance supprimée");
                    loadEcheances();
                    updateStatistics();
                } catch (SQLException e) {
                    AlertUtils.showError("Erreur", "Impossible de supprimer l'échéance: " + e.getMessage());
                }
            }
        });
    }

    private void ajouterEcheance() {
        // TODO: Ouvrir un dialogue pour créer une nouvelle échéance
        AlertUtils.showInfo("Info", "Fonction d'ajout d'échéance à implémenter");
    }

    /**
     * Classe wrapper pour afficher les échéances dans la table
     */
    public static class EcheanceRow {
        private final Echeance echeance;

        public EcheanceRow(Echeance echeance) {
            this.echeance = echeance;
        }

        public Echeance getEcheance() {
            return echeance;
        }
    }
}
