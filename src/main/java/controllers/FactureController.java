package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Facture;
import services.ServiceFacture;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class FactureController implements Initializable {

    @FXML private TableView<Facture> tableViewFactures;
    @FXML private TableColumn<Facture, Integer> colIdFacture;
    @FXML private TableColumn<Facture, String> colNumeroFacture;
    @FXML private TableColumn<Facture, BigDecimal> colMontantFacture;
    @FXML private TableColumn<Facture, String> colDateFacture;
    @FXML private TableColumn<Facture, String> colDateEcheance;
    @FXML private TableColumn<Facture, String> colStatutFacture;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statutFilter;

    @FXML private Label totalFacturesLabel;
    @FXML private Label totalPayeesLabel;
    @FXML private Label totalImpayeesLabel;

    private ServiceFacture factureService;
    private ObservableList<Facture> factureList = FXCollections.observableArrayList();
    private FilteredList<Facture> filteredFactures;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        factureService = new ServiceFacture();

        colIdFacture.setCellValueFactory(new PropertyValueFactory<>("idFacture"));
        colNumeroFacture.setCellValueFactory(new PropertyValueFactory<>("numeroFacture"));
        colMontantFacture.setCellValueFactory(new PropertyValueFactory<>("montant"));

        colDateFacture.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDateFacture();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });

        colDateEcheance.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDateEcheance();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });

        colStatutFacture.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Formatage du montant (BigDecimal)
        colMontantFacture.setCellFactory(column -> new TableCell<Facture, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal montant, boolean empty) {
                super.updateItem(montant, empty);
                if (empty || montant == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", montant));
                }
            }
        });

        // Coloration du statut
        colStatutFacture.setCellFactory(column -> new TableCell<Facture, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    switch (statut) {
                        case "payee":
                            setStyle("-fx-text-fill: #3e893e; -fx-font-weight: bold;");
                            break;
                        case "impayee":
                            setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                            break;
                        case "en_attente":
                            setStyle("-fx-text-fill: #f78f34; -fx-font-weight: bold;");
                            break;
                        case "annulee":
                            setStyle("-fx-text-fill: #999;");
                            break;
                    }
                }
            }
        });

        if (statutFilter != null) {
            statutFilter.getItems().addAll("Tous", "payee", "impayee", "en_attente", "annulee");
            statutFilter.setValue("Tous");
        }

        chargerDonnees();
        configurerRecherche();
    }

    private void configurerRecherche() {
        searchField.textProperty().addListener((obs, old, n) -> filtrerFactures());
        statutFilter.valueProperty().addListener((obs, old, n) -> filtrerFactures());
    }

    private void filtrerFactures() {
        if (filteredFactures == null) {
            filteredFactures = new FilteredList<>(factureList, p -> true);
        }
        String searchText = searchField.getText().toLowerCase();
        String statut = statutFilter.getValue();
        filteredFactures.setPredicate(facture -> {
            if (!searchText.isEmpty() && !facture.getNumeroFacture().toLowerCase().contains(searchText))
                return false;
            if (statut != null && !"Tous".equals(statut) && !statut.equals(facture.getStatut()))
                return false;
            return true;
        });
        tableViewFactures.setItems(filteredFactures);
        mettreAJourStatistiques();
    }

    public void chargerDonnees() {
        try {
            List<Facture> factures = factureService.recupererToutes();
            factureList.setAll(factures);
            tableViewFactures.setItems(factureList);
            mettreAJourStatistiques();
            System.out.println("✅ Factures chargées: " + factures.size());
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les factures: " + e.getMessage());
        }
    }

    private void mettreAJourStatistiques() {
        int total = factureList.size();
        long payees = factureList.stream().filter(f -> "payee".equals(f.getStatut())).count();
        long impayees = factureList.stream().filter(f -> "impayee".equals(f.getStatut()) || "en_attente".equals(f.getStatut())).count();
        totalFacturesLabel.setText(String.valueOf(total));
        totalPayeesLabel.setText(String.valueOf(payees));
        totalImpayeesLabel.setText(String.valueOf(impayees));
    }

    @FXML
    private void ajouterFacture() {
        // À implémenter si nécessaire
        showInfo("Info", "Ajout de facture à implémenter");
    }

    @FXML
    private void resetFiltres() {
        searchField.clear();
        statutFilter.setValue("Tous");
        filtrerFactures();
    }

    @FXML
    private void refreshAll() {
        chargerDonnees();
    }

    @FXML
    private void handleEnvoyerFacture() {
        Facture selected = tableViewFactures.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Veuillez sélectionner une facture");
            return;
        }
        ouvrirDialogueEnvoi(selected);
    }

    @FXML
    private void handleTelechargerPDF() {
        Facture selected = tableViewFactures.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Veuillez sélectionner une facture");
            return;
        }
        ouvrirDialogueEnvoi(selected);
    }

    private void ouvrirDialogueEnvoi(Facture facture) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/EnvoiFactureDialog.fxml"));
            Parent root = loader.load();
            EnvoiFactureController controller = loader.getController();
            controller.setFacture(facture);
            Stage stage = new Stage();
            controller.setDialogStage(stage); // ← DOIT ÊTRE PRÉSENT
            stage.setTitle("Envoi de facture");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            if (controller.isOkClicked()) {
                // Optionnel : rafraîchir la liste après envoi
                chargerDonnees();
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'envoi: " + e.getMessage());
        }
    }

    @FXML
    private void handleFermer() {
        Stage stage = (Stage) tableViewFactures.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}