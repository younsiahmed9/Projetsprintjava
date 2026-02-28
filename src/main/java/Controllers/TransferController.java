package Controllers;

import Models.CarteVirtuelle;
import Models.Session;
import Models.Utilisateur;
import Services.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;

public class TransferController {

    @FXML private ComboBox<CarteVirtuelle> comboMesCartes;
    @FXML private TextField txtSearchCard;
    @FXML private VBox searchResultsContainer;
    @FXML private ListView<CarteVirtuelle> searchResultsList;
    @FXML private VBox selectedDestContainer;
    @FXML private Label lblSelectedCard;
    @FXML private TextField txtMontant;
    @FXML private Label lblSourceInfo, lblDestInfo, lblMontantInfo, lblConversionInfo;

    // New fee-related fields
    @FXML private Label lblFraisInfo;
    @FXML private Label lblFraisMontant;
    @FXML private Label lblMontantTotal;

    private CarteVirtuelleService carteService = new CarteVirtuelleService();
    private UtilisateurService utilisateurService = new UtilisateurService();
    private TransferFeeService transferFeeService = new TransferFeeService();
    private ObservableList<CarteVirtuelle> mesCartes = FXCollections.observableArrayList();
    private ObservableList<CarteVirtuelle> searchResults = FXCollections.observableArrayList();
    private CarteVirtuelle selectedDestCard = null;

    @FXML
    public void initialize() {
        // Charger les cartes de l'utilisateur connecté
        Utilisateur currentUser = Session.getUtilisateur();
        if (currentUser == null) {
            showAlert("Session expirée. Veuillez vous reconnecter.");
            return;
        }

        int userId = currentUser.getId();
        List<CarteVirtuelle> userCards = carteService.getCartesByUtilisateur(userId);
        mesCartes.setAll(userCards);

        // Configurer l'affichage des cartes dans la ComboBox
        StringConverter<CarteVirtuelle> converter = new StringConverter<>() {
            @Override
            public String toString(CarteVirtuelle c) {
                if (c == null) return "";
                return String.format("ID:%d | %s (%s %.2f) - %s",
                        c.getId(), c.getNumeroCarte(), c.getDevise(), c.getSolde(), c.getType());
            }
            @Override
            public CarteVirtuelle fromString(String s) { return null; }
        };

        comboMesCartes.setItems(mesCartes);
        comboMesCartes.setConverter(converter);

        // Configurer la ListView des résultats
        searchResultsList.setItems(searchResults);
        searchResultsList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(CarteVirtuelle carte, boolean empty) {
                super.updateItem(carte, empty);
                if (empty || carte == null) {
                    setText(null);
                } else {
                    // Récupérer le propriétaire de la carte
                    Utilisateur owner = getCardOwner(carte);
                    String ownerInfo = (owner != null) ?
                            String.format(" (%s %s)", owner.getPrenom(), owner.getNom()) : "";
                    setText(String.format("ID:%d | %s%s - %s %.2f - %s",
                            carte.getId(), carte.getNumeroCarte(), ownerInfo,
                            carte.getDevise(), carte.getSolde(), carte.getType()));
                }
            }
        });

        // Double-clic pour sélectionner une carte
        searchResultsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                CarteVirtuelle selected = searchResultsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    selectDestinationCard(selected);
                }
            }
        });

        // Mise à jour des infos lors de la sélection
        comboMesCartes.setOnAction(e -> updateInfo());
        txtMontant.textProperty().addListener((obs, old, newVal) -> updateInfo());

        // Afficher les taux en temps réel au chargement
        updateExchangeRates();

        // Message d'aide pour la recherche
        txtSearchCard.setPromptText("Rechercher par ID, numéro de carte, email ou nom...");
    }

    private void updateExchangeRates() {
        try {
            double usdToDt = RealTimeCurrencyService.getExchangeRate("USD", "DT");
            double eurToDt = RealTimeCurrencyService.getExchangeRate("EUR", "DT");
            lblConversionInfo.setText(String.format("Taux en temps réel: 1 USD = %.2f DT | 1 EUR = %.2f DT",
                    usdToDt, eurToDt));
        } catch (Exception e) {
            lblConversionInfo.setText("Taux: 1 USD = 3.1 DT | 1 EUR = 3.4 DT (mode secours)");
        }
    }

    private Utilisateur getCardOwner(CarteVirtuelle carte) {
        try {
            return utilisateurService.getUserByCardId(carte.getId());
        } catch (Exception e) {
            System.err.println("Erreur récupération propriétaire: " + e.getMessage());
            return null;
        }
    }

    @FXML
    private void handleSearchCard() {
        String query = txtSearchCard.getText().trim();
        if (query.isEmpty()) {
            showAlert("Veuillez entrer un ID, numéro de carte, email ou nom.");
            return;
        }

        List<CarteVirtuelle> results = carteService.rechercherCartes(query);
        searchResults.setAll(results);

        if (results.isEmpty()) {
            searchResultsContainer.setVisible(false);
            searchResultsContainer.setManaged(false);
            showAlert("Aucune carte trouvée avec ce critère.");
        } else {
            searchResultsContainer.setVisible(true);
            searchResultsContainer.setManaged(true);
        }
    }

    private void selectDestinationCard(CarteVirtuelle carte) {
        selectedDestCard = carte;
        selectedDestContainer.setVisible(true);
        selectedDestContainer.setManaged(true);
        searchResultsContainer.setVisible(false);
        searchResultsContainer.setManaged(false);

        Utilisateur owner = getCardOwner(carte);
        String ownerInfo = (owner != null) ?
                String.format(" (Propriétaire: %s %s)", owner.getPrenom(), owner.getNom()) : "";
        lblSelectedCard.setText(String.format("ID:%d | %s%s - %s %.2f",
                carte.getId(), carte.getNumeroCarte(), ownerInfo,
                carte.getDevise(), carte.getSolde()));

        updateInfo();
    }

    @FXML
    private void handleClearSelection() {
        selectedDestCard = null;
        selectedDestContainer.setVisible(false);
        selectedDestContainer.setManaged(false);
        updateInfo();
    }

    private void updateFees() {
        CarteVirtuelle source = comboMesCartes.getValue();
        String montantText = txtMontant.getText().trim();

        if (source != null && !montantText.isEmpty()) {
            try {
                double montant = Double.parseDouble(montantText);
                if (montant > 0) {
                    int userId = Session.getUtilisateur().getId();

                    // Calculer les frais
                    double frais = transferFeeService.calculerFrais(montant, userId);
                    double montantTotal = montant + frais;

                    // Mettre à jour les labels
                    lblFraisInfo.setText(transferFeeService.getFeesInfoMessage(userId));
                    lblFraisMontant.setText(String.format("Frais (%.1f%%): %.2f %s",
                            transferFeeService.getTauxFrais(userId), frais, source.getDevise()));
                    lblMontantTotal.setText(String.format("Total à débiter: %.2f %s",
                            montantTotal, source.getDevise()));
                }
            } catch (NumberFormatException e) {
                // Ignorer
            }
        } else {
            lblFraisInfo.setText("Frais: -");
            lblFraisMontant.setText("-");
            lblMontantTotal.setText("-");
        }
    }

    private void updateInfo() {
        CarteVirtuelle source = comboMesCartes.getValue();
        String montantText = txtMontant.getText().trim();

        // Info source
        if (source != null) {
            lblSourceInfo.setText("Source: ID:" + source.getId() + " | " +
                    source.getNumeroCarte() + " (" + source.getSolde() + " " + source.getDevise() + ")");
        } else {
            lblSourceInfo.setText("Source: -");
        }

        // Info destination
        if (selectedDestCard != null) {
            lblDestInfo.setText("Destination: ID:" + selectedDestCard.getId() + " | " +
                    selectedDestCard.getNumeroCarte() + " (" + selectedDestCard.getSolde() + " " + selectedDestCard.getDevise() + ")");
        } else {
            lblDestInfo.setText("Destination: -");
        }

        // Info montant et conversion
        if (!montantText.isEmpty() && source != null && selectedDestCard != null) {
            try {
                double montant = Double.parseDouble(montantText);
                if (montant > 0) {
                    double converti = ConversionDevise.convertir(
                            montant, source.getDevise(), selectedDestCard.getDevise());

                    if (!source.getDevise().equals(selectedDestCard.getDevise())) {
                        lblMontantInfo.setText(String.format("Montant: %.2f %s → %.2f %s",
                                montant, source.getDevise(), converti, selectedDestCard.getDevise()));
                    } else {
                        lblMontantInfo.setText(String.format("Montant: %.2f %s",
                                montant, source.getDevise()));
                    }

                    // Mettre à jour les frais
                    updateFees();
                }
            } catch (NumberFormatException e) {
                // Ignorer
            }
        } else {
            // Reset fee display if no valid input
            lblFraisInfo.setText("Frais: -");
            lblFraisMontant.setText("-");
            lblMontantTotal.setText("-");
        }
    }

    @FXML
    private void handleTransferer() {
        CarteVirtuelle source = comboMesCartes.getValue();
        String montantText = txtMontant.getText().trim();

        if (source == null) {
            showAlert("Veuillez sélectionner votre carte source.");
            return;
        }
        if (selectedDestCard == null) {
            showAlert("Veuillez sélectionner une carte destination.");
            return;
        }
        if (montantText.isEmpty()) {
            showAlert("Veuillez saisir un montant.");
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

        // Calculer le montant total avec frais
        int userId = Session.getUtilisateur().getId();
        double frais = transferFeeService.calculerFrais(montant, userId);
        double montantTotal = montant + frais;

        if (source.getSolde() < montantTotal) {
            showAlert(String.format("Solde insuffisant sur votre carte. Nécessaire: %.2f %s (dont frais: %.2f %s)",
                    montantTotal, source.getDevise(), frais, source.getDevise()));
            return;
        }

        if (source.getId() == selectedDestCard.getId()) {
            showAlert("La carte source et destination doivent être différentes.");
            return;
        }

        // --- VÉRIFICATION SIMPLIFIÉE (PAYS + VILLE) ---
        Utilisateur currentUser = Session.getUtilisateur();
        if (currentUser == null) {
            showAlert("Session expirée. Veuillez vous reconnecter.");
            return;
        }

        String country = currentUser.getCountry();
        String city = currentUser.getCity();

        if (country == null || country.isEmpty()) {
            showAlert("❌ Pays non renseigné. Veuillez mettre à jour votre profil.");
            return;
        }

        if (city == null || city.isEmpty()) {
            showAlert("❌ Ville non renseignée. Veuillez mettre à jour votre profil.");
            return;
        }

        if (!AddressVerificationService.isAddressValidForTransfer(country, city)) {
            String errorMsg = AddressVerificationService.getErrorMessage(country, city);
            showAlert(errorMsg);
            return;
        }

        System.out.println("✅ Vérification réussie - Transfert autorisé pour " + country + ", " + city);
        // --- FIN DE LA VÉRIFICATION ---

        // Demander confirmation des frais
        double taux = transferFeeService.getTauxFrais(userId);

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation des frais");
        confirmAlert.setHeaderText("Frais de transfert");
        confirmAlert.setContentText(String.format(
                "Montant à transférer: %.2f %s\n" +
                        "Frais (%.1f%%): %.2f %s\n" +
                        "Total à débiter: %.2f %s\n\n" +
                        "Voulez-vous continuer?",
                montant, source.getDevise(), taux, frais, source.getDevise(),
                montantTotal, source.getDevise()));

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // Effectuer le transfert (seulement le montant, pas les frais)
        boolean success = carteService.transferer(source.getId(), selectedDestCard.getId(), montant);

        if (success) {
            Alert info = new Alert(Alert.AlertType.INFORMATION,
                    String.format("✅ Transfert effectué avec succès !\nFrais prélevés: %.2f %s",
                            frais, source.getDevise()));
            info.showAndWait();
            fermer();
        } else {
            showAlert("Échec du transfert. Vérifiez les soldes et réessayez.");
        }
    }

    // ========== NAVIGATION METHODS ==========

    @FXML
    private void handlePortefeuilles() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/client_dashboard.fxml"));
            Stage stage = (Stage) comboMesCartes.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
            stage.setTitle("FinTrack - Mes Portefeuilles");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de la navigation vers les portefeuilles");
        }
    }

    @FXML
    private void handleCartes() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/carte_list.fxml"));
            Stage stage = (Stage) comboMesCartes.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
            stage.setTitle("FinTrack - Mes Cartes");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de la navigation vers les cartes");
        }
    }

    @FXML
    private void handleTransactions() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/transactions.fxml"));
            Stage stage = (Stage) comboMesCartes.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
            stage.setTitle("FinTrack - Transactions");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de la navigation vers les transactions");
        }
    }

    @FXML
    private void handleScheduledTransfer() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/scheduled_transfer_form.fxml"));
            Stage stage = (Stage) comboMesCartes.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
            stage.setTitle("FinTrack - Transferts programmés");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de la navigation vers les transferts programmés");
        }
    }

    @FXML
    private void handleLogout() {
        Session.clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
            Stage stage = (Stage) comboMesCartes.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FinTrack - Connexion");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de la déconnexion");
        }
    }

    @FXML
    private void handleRetour() {
        handlePortefeuilles(); // Retour au dashboard principal
    }

    @FXML
    private void handleAnnuler() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) comboMesCartes.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }
}