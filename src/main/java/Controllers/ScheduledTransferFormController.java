package Controllers;

import Models.CarteVirtuelle;
import Models.ScheduledTransfer;
import Models.Session;
import Services.CarteVirtuelleService;
import Services.ScheduledTransferService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import javafx.scene.control.ButtonType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ScheduledTransferFormController {

    @FXML private ComboBox<CarteVirtuelle> comboSource;
    @FXML private ComboBox<CarteVirtuelle> comboDestination;
    @FXML private TextField txtMontant;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Integer> hourCombo;
    @FXML private ComboBox<Integer> minuteCombo;

    private CarteVirtuelleService carteService = new CarteVirtuelleService();
    private ScheduledTransferService scheduledService = new ScheduledTransferService();
    private ObservableList<CarteVirtuelle> cartes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        int userId = Session.getUtilisateur().getId();
        List<CarteVirtuelle> list = carteService.getCartesByUtilisateur(userId);
        cartes.setAll(list);

        // Converter to display card info in ComboBox
        StringConverter<CarteVirtuelle> converter = new StringConverter<>() {
            @Override
            public String toString(CarteVirtuelle c) {
                return c == null ? "" : c.getNumeroCarte() + " (" + c.getSolde() + " " + c.getDevise() + ")";
            }
            @Override
            public CarteVirtuelle fromString(String s) { return null; }
        };

        comboSource.setItems(cartes);
        comboSource.setConverter(converter);
        comboDestination.setItems(cartes);
        comboDestination.setConverter(converter);

        // Disable past dates
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Populate hour combo (0-23)
        ObservableList<Integer> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) hours.add(i);
        hourCombo.setItems(hours);

        // Populate minute combo (0-59)
        ObservableList<Integer> minutes = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i++) minutes.add(i);
        minuteCombo.setItems(minutes);

        // Set default time to next hour
        LocalTime now = LocalTime.now();
        int defaultHour = now.getHour() + 1;
        if (defaultHour > 23) defaultHour = 23;
        hourCombo.setValue(defaultHour);
        minuteCombo.setValue(0);
    }


    @FXML
    private void handleProgrammer() {
        CarteVirtuelle source = comboSource.getValue();
        CarteVirtuelle dest = comboDestination.getValue();
        String montantText = txtMontant.getText().trim();
        LocalDate date = datePicker.getValue();
        Integer hour = hourCombo.getValue();
        Integer minute = minuteCombo.getValue();

        // Validation
        if (source == null) { showAlert("Veuillez sélectionner la carte source."); return; }
        if (dest == null) { showAlert("Veuillez sélectionner la carte destination."); return; }
        if (montantText.isEmpty()) { showAlert("Veuillez saisir un montant."); return; }
        if (date == null) { showAlert("Veuillez choisir une date."); return; }
        if (hour == null) { showAlert("Veuillez choisir une heure."); return; }
        if (minute == null) { showAlert("Veuillez choisir une minute."); return; }

        if (source.getId() == dest.getId()) {
            showAlert("La carte source et destination doivent être différentes.");
            return;
        }

        double montant;
        try {
            montant = Double.parseDouble(montantText);
            if (montant <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Montant invalide. Veuillez entrer un nombre positif.");
            return;
        }

        // --- NOUVEAU : Vérification du solde actuel ---
        if (source.getSolde() < montant) {
            Alert warn = new Alert(Alert.AlertType.WARNING,
                    "Le solde actuel de la carte source est insuffisant. Le transfert pourra échouer si la situation n'évolue pas d'ici la date programmée.",
                    ButtonType.OK, ButtonType.CANCEL);
            if (warn.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.CANCEL) {
                return; // L'utilisateur a annulé
            }
            // S'il clique OK, on continue malgré l'avertissement
        }

        LocalDateTime scheduledDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));

        if (scheduledDateTime.isBefore(LocalDateTime.now())) {
            showAlert("La date/heure choisie est dans le passé.");
            return;
        }

        ScheduledTransfer st = new ScheduledTransfer(
                Session.getUtilisateur().getId(),
                source.getId(),
                dest.getId(),
                montant,
                scheduledDateTime
        );
        scheduledService.ajouter(st);

        Alert info = new Alert(Alert.AlertType.INFORMATION, "Transfert programmé avec succès !");
        info.showAndWait();
        fermer();
    }

    @FXML
    private void handleAnnuler() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) comboSource.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }
}