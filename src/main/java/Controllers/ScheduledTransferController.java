package Controllers;

import Models.CarteVirtuelle;
import Models.ScheduledTransfer;
import Models.Session;
import Models.Utilisateur;
import Services.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ScheduledTransferController {

    @FXML private ComboBox<CarteVirtuelle> comboMesCartes;
    @FXML private ComboBox<CarteVirtuelle> comboDestination;
    @FXML private TextField txtMontant;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Integer> hourCombo;
    @FXML private ComboBox<Integer> minuteCombo;

    // Labels pour l'aperçu
    @FXML private Label lblSourceInfo;
    @FXML private Label lblDestInfo;
    @FXML private Label lblMontantInfo;
    @FXML private Label lblMontantConverti;
    @FXML private Label lblConversionInfo;

    private CarteVirtuelleService carteService = new CarteVirtuelleService();
    private ScheduledTransferService scheduledService = new ScheduledTransferService();
    private UtilisateurService utilisateurService = new UtilisateurService();
    private ElasticEmailService emailService = new ElasticEmailService(); // ✅ Déplacé ici avec les autres services
    private ObservableList<CarteVirtuelle> mesCartes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadUserCards();
        setupTimeCombos();
        setupListeners();
        updateExchangeRates();
    }

    private void loadUserCards() {
        int userId = Session.getUtilisateur().getId();
        List<CarteVirtuelle> userCards = carteService.getCartesByUtilisateur(userId);
        mesCartes.setAll(userCards);

        // Aussi charger toutes les cartes pour la destination
        List<CarteVirtuelle> allCards = carteService.afficherTous();

        StringConverter<CarteVirtuelle> converter = new StringConverter<>() {
            @Override
            public String toString(CarteVirtuelle c) {
                if (c == null) return "";
                Utilisateur owner = getCardOwner(c);
                String ownerInfo = (owner != null) ? " (" + owner.getPrenom() + " " + owner.getNom() + ")" : "";
                return String.format("ID:%d | %s%s - %s %.2f",
                        c.getId(), c.getNumeroCarte(), ownerInfo, c.getDevise(), c.getSolde());
            }
            @Override
            public CarteVirtuelle fromString(String s) { return null; }
        };

        comboMesCartes.setItems(mesCartes);
        comboMesCartes.setConverter(converter);

        comboDestination.setItems(FXCollections.observableArrayList(allCards));
        comboDestination.setConverter(converter);
    }

    private Utilisateur getCardOwner(CarteVirtuelle carte) {
        try {
            return utilisateurService.getUserByCardId(carte.getId());
        } catch (Exception e) {
            return null;
        }
    }

    private void setupTimeCombos() {
        // Heures de 0 à 23
        ObservableList<Integer> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            hours.add(i);
        }
        hourCombo.setItems(hours);

        // Minutes de 0 à 59 (TOUTES les minutes)
        ObservableList<Integer> minutes = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i++) {
            minutes.add(i);
        }
        minuteCombo.setItems(minutes);

        LocalTime now = LocalTime.now();
        hourCombo.setValue(now.getHour() + 1 > 23 ? 23 : now.getHour() + 1);
        minuteCombo.setValue(0);
    }

    private void setupListeners() {
        comboMesCartes.setOnAction(e -> updatePreview());
        comboDestination.setOnAction(e -> updatePreview());
        txtMontant.textProperty().addListener((obs, old, newVal) -> updatePreview());
    }

    private void updateExchangeRates() {
        try {
            double usdToDt = RealTimeCurrencyService.getExchangeRate("USD", "DT");
            double eurToDt = RealTimeCurrencyService.getExchangeRate("EUR", "DT");
            if (lblConversionInfo != null) {
                lblConversionInfo.setText(String.format("Taux en temps réel: 1 USD = %.2f DT | 1 EUR = %.2f DT",
                        usdToDt, eurToDt));
            }
        } catch (Exception e) {
            if (lblConversionInfo != null) {
                lblConversionInfo.setText("Taux: 1 USD = 3.1 DT | 1 EUR = 3.4 DT (mode secours)");
            }
        }
    }

    private void updatePreview() {
        CarteVirtuelle source = comboMesCartes.getValue();
        CarteVirtuelle dest = comboDestination.getValue();
        String montantText = txtMontant.getText().trim();

        // Info source
        if (source != null && lblSourceInfo != null) {
            lblSourceInfo.setText("Source: " + source.getNumeroCarte() +
                    " (" + source.getSolde() + " " + source.getDevise() + ")");
        } else if (lblSourceInfo != null) {
            lblSourceInfo.setText("Source: -");
        }

        // Info destination
        if (dest != null && lblDestInfo != null) {
            lblDestInfo.setText("Destination: " + dest.getNumeroCarte() +
                    " (" + dest.getSolde() + " " + dest.getDevise() + ")");
        } else if (lblDestInfo != null) {
            lblDestInfo.setText("Destination: -");
        }

        // Calcul de la conversion
        if (!montantText.isEmpty() && source != null && dest != null && lblMontantInfo != null) {
            try {
                double montant = Double.parseDouble(montantText);
                if (montant > 0) {
                    lblMontantInfo.setText("Montant: " + montant + " " + source.getDevise());

                    if (!source.getDevise().equals(dest.getDevise()) && lblMontantConverti != null) {
                        double montantConverti = ConversionDevise.convertir(
                                montant, source.getDevise(), dest.getDevise());
                        lblMontantConverti.setText(String.format("Après conversion: %.2f %s",
                                montantConverti, dest.getDevise()));
                        lblMontantConverti.setVisible(true);
                        lblMontantConverti.setManaged(true);
                    } else if (lblMontantConverti != null) {
                        lblMontantConverti.setVisible(false);
                        lblMontantConverti.setManaged(false);
                    }
                }
            } catch (NumberFormatException e) {
                // Ignorer
            }
        } else if (lblMontantInfo != null) {
            lblMontantInfo.setText("Montant: -");
            if (lblMontantConverti != null) {
                lblMontantConverti.setVisible(false);
                lblMontantConverti.setManaged(false);
            }
        }
    }

    @FXML
    private void handleProgrammer() {
        CarteVirtuelle source = comboMesCartes.getValue();
        CarteVirtuelle dest = comboDestination.getValue();
        String montantText = txtMontant.getText().trim();
        LocalDate date = datePicker.getValue();
        Integer hour = hourCombo.getValue();
        Integer minute = minuteCombo.getValue();

        if (source == null || dest == null || montantText.isEmpty() || date == null || hour == null || minute == null) {
            showAlert("Tous les champs sont requis.");
            return;
        }

        double montant;
        try {
            montant = Double.parseDouble(montantText);
            if (montant <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Montant invalide.");
            return;
        }

        if (source.getSolde() < montant) {
            showAlert("Solde insuffisant sur la carte source.");
            return;
        }

        LocalDateTime scheduledDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));

        if (scheduledDateTime.isBefore(LocalDateTime.now())) {
            showAlert("La date choisie est dans le passé.");
            return;
        }

        // --- VÉRIFICATION DE LA LOCALISATION AVEC API ---
        System.out.println("🌍 Vérification de la localisation...");

        if (!GeoLocationService.isLocationValidForTransfer()) {
            String errorMsg = GeoLocationService.getLocationErrorMessage();
            showAlert(errorMsg != null ? errorMsg : "❌ Localisation non autorisée");
            return;
        }

        GeoLocationService.LocationResult location = GeoLocationService.getCurrentLocation();
        System.out.println("✅ Localisation vérifiée: " + location.getCountry() + ", " + location.getCity());
        // --- FIN DE LA VÉRIFICATION ---

        // Afficher un résumé avec la conversion si nécessaire
        StringBuilder resume = new StringBuilder();
        resume.append(String.format("Transfert de %.2f %s\n", montant, source.getDevise()));
        resume.append(String.format("Vers: %s\n", dest.getNumeroCarte()));

        if (!source.getDevise().equals(dest.getDevise())) {
            double montantConverti = ConversionDevise.convertir(montant, source.getDevise(), dest.getDevise());
            resume.append(String.format("Soit %.2f %s après conversion\n", montantConverti, dest.getDevise()));
        }

        resume.append(String.format("Date: %s", scheduledDateTime));

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Confirmer le transfert programmé");
        confirm.setContentText(resume.toString());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
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

        // ✅ Envoi d'email de confirmation
        emailService.sendScheduledTransferConfirmation(st, source, dest, true, null);

        Alert info = new Alert(Alert.AlertType.INFORMATION, "✅ Transfert programmé avec succès !\nUn email de confirmation a été envoyé.");
        info.showAndWait();

        NavigationService.navigateTo("/fxml/client_dashboard.fxml", "Mes Portefeuilles");
    }

    @FXML
    private void handleRetour() {
        NavigationService.navigateTo("/fxml/client_dashboard.fxml", "Mes Portefeuilles");
    }

    @FXML
    private void handlePortefeuilles() {
        NavigationService.navigateTo("/fxml/client_dashboard.fxml", "Mes Portefeuilles");
    }

    @FXML
    private void handleCartes() {
        NavigationService.navigateTo("/fxml/carte_list.fxml", "Mes Cartes");
    }

    @FXML
    private void handleTransferts() {
        NavigationService.navigateTo("/fxml/transfer_form.fxml", "Transfert");
    }

    @FXML
    private void handleTransactions() {
        NavigationService.navigateTo("/fxml/transactions.fxml", "Transactions");
    }

    @FXML
    private void handleLogout() {
        Session.clear();
        NavigationService.navigateTo("/fxml/login.fxml", "Connexion");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }
}