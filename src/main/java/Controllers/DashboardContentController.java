package Controllers;

import Models.CarteVirtuelle;
import Models.Portefeuille;
import Models.ScheduledTransfer;
import Models.Session;
import Models.Utilisateur;
import Services.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardContentController {

    @FXML private Label welcomeLabel;
    @FXML private FlowPane containerPortefeuilles;
    @FXML private TextField txtRecherche;
    @FXML private Button btnTous, btnDT, btnUSD, btnEUR;

    @FXML private TableView<ScheduledTransfer> scheduledTable;
    @FXML private TableColumn<ScheduledTransfer, Integer> colId;
    @FXML private TableColumn<ScheduledTransfer, String> colFromCard;
    @FXML private TableColumn<ScheduledTransfer, String> colToCard;
    @FXML private TableColumn<ScheduledTransfer, Double> colAmount;
    @FXML private TableColumn<ScheduledTransfer, String> colDate;
    @FXML private TableColumn<ScheduledTransfer, String> colStatus;

    private PortefeuilleService portefeuilleService = new PortefeuilleService();
    private CarteVirtuelleService carteService = new CarteVirtuelleService();
    private ScheduledTransferService scheduledService = new ScheduledTransferService();
    private UtilisateurService utilisateurService = new UtilisateurService();

    private ObservableList<Portefeuille> portefeuilles = FXCollections.observableArrayList();
    private ObservableList<ScheduledTransfer> scheduledTransfers = FXCollections.observableArrayList();
    private String currentFilter = "TOUS";
    private Map<Integer, CarteVirtuelle> carteCache;

    private ClientDashboardController parentController;

    public void setParentController(ClientDashboardController parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        Utilisateur user = Session.getUtilisateur();
        if (user != null) {
            welcomeLabel.setText("Bienvenue, " + user.getPrenom() + " " + user.getNom());
        }

        loadCardCache();
        loadPortefeuilles();
        setupSearchListener();
        setupScheduledTable();
        loadScheduledTransfers();
    }

    private void loadCardCache() {
        carteCache = new HashMap<>();
        List<CarteVirtuelle> allUserCards = carteService.getCartesByUtilisateur(Session.getUtilisateur().getId());
        for (CarteVirtuelle c : allUserCards) {
            carteCache.put(c.getId(), c);
        }
    }

    // ========== Portefeuilles methods ==========

    private void loadPortefeuilles() {
        portefeuilles.setAll(portefeuilleService.getPortefeuillesByUtilisateur(Session.getUtilisateur().getId()));
        applyFilter();
    }

    private void setupSearchListener() {
        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    private void applyFilter() {
        List<Portefeuille> filtered = portefeuilles.stream()
                .filter(p -> currentFilter.equals("TOUS") || p.getDevisePrincipale().equals(currentFilter))
                .filter(p -> p.getNom().toLowerCase().contains(txtRecherche.getText().toLowerCase()))
                .collect(Collectors.toList());
        displayPortefeuilles(filtered);
    }

    private void displayPortefeuilles(List<Portefeuille> list) {
        containerPortefeuilles.getChildren().clear();
        for (Portefeuille p : list) {
            VBox card = createPortefeuilleCard(p);
            containerPortefeuilles.getChildren().add(card);
        }
    }

    private VBox createPortefeuilleCard(Portefeuille p) {
        VBox card = new VBox(15);
        card.getStyleClass().add("account-card");
        card.setPrefWidth(280);
        card.setPrefHeight(200);

        Label typeLabel = new Label(p.getDevisePrincipale());
        typeLabel.getStyleClass().add("card-type-header");

        Label nomLabel = new Label(p.getNom());
        nomLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #182d88;");

        Label soldeLabel = new Label(String.format("%.2f %s", p.getSoldeTotal(), p.getDevisePrincipale()));
        soldeLabel.getStyleClass().add("card-solde-text");

        Button detailsBtn = new Button("Voir les cartes");
        detailsBtn.getStyleClass().add("btn-details-orange");
        detailsBtn.setOnAction(e -> ouvrirVueCartes(p));

        card.getChildren().addAll(typeLabel, nomLabel, soldeLabel, detailsBtn);
        return card;
    }

    private void ouvrirVueCartes(Portefeuille p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/carte_list.fxml"));
            Parent root = loader.load();
            CarteListController controller = loader.getController();
            controller.setPortefeuille(p);
            Stage stage = (Stage) containerPortefeuilles.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Cartes du portefeuille : " + p.getNom());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouterPortefeuille() {
        ouvrirFormulairePortefeuille(null);
    }

    private void ouvrirFormulairePortefeuille(Portefeuille p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/portefeuille_form.fxml"));
            Parent root = loader.load();
            PortefeuilleFormController controller = loader.getController();
            controller.setPortefeuille(p);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadPortefeuilles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void filterTous() {
        setActiveFilter(btnTous);
        currentFilter = "TOUS";
        applyFilter();
    }

    @FXML
    private void filterDT() {
        setActiveFilter(btnDT);
        currentFilter = "DT";
        applyFilter();
    }

    @FXML
    private void filterUSD() {
        setActiveFilter(btnUSD);
        currentFilter = "USD";
        applyFilter();
    }

    @FXML
    private void filterEUR() {
        setActiveFilter(btnEUR);
        currentFilter = "EUR";
        applyFilter();
    }

    private void setActiveFilter(Button active) {
        btnTous.getStyleClass().remove("filter-active");
        btnDT.getStyleClass().remove("filter-active");
        btnUSD.getStyleClass().remove("filter-active");
        btnEUR.getStyleClass().remove("filter-active");
        active.getStyleClass().add("filter-active");
    }

    // ========== Scheduled transfers methods ==========

    private void setupScheduledTable() {
        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        colFromCard.setCellValueFactory(cellData -> {
            int cardId = cellData.getValue().getFromCardId();
            return new SimpleStringProperty(getCardNumberById(cardId));
        });

        colToCard.setCellValueFactory(cellData -> {
            int cardId = cellData.getValue().getToCardId();
            return new SimpleStringProperty(getCardNumberById(cardId));
        });

        colAmount.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());

        colDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getScheduledDate().toString()));

        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));
    }

    private String getCardNumberById(int cardId) {
        CarteVirtuelle c = carteCache.get(cardId);
        return c != null ? c.getNumeroCarte() : "Inconnue";
    }

    private void loadScheduledTransfers() {
        scheduledTransfers.setAll(scheduledService.getByUser(Session.getUtilisateur().getId()));
        scheduledTable.setItems(scheduledTransfers);
    }

    @FXML
    private void handleRefreshScheduled() {
        loadScheduledTransfers();
    }

    // ========== Export methods ==========

    @FXML
    private void handleExportScheduledPDF() {
        List<ScheduledTransferExport.ScheduledTransferData> data = prepareScheduledTransferData();
        boolean success = ScheduledTransferExport.exportToPDF(data, scheduledTable.getScene().getWindow());
        if (success) {
            showInfo("✅ PDF exporté avec succès !");
        } else {
            showAlert("❌ Erreur lors de l'export PDF");
        }
    }

    @FXML
    private void handleExportScheduledExcel() {
        showInfo("📊 Export Excel sera bientôt disponible");
    }

    private List<ScheduledTransferExport.ScheduledTransferData> prepareScheduledTransferData() {
        List<ScheduledTransferExport.ScheduledTransferData> data = new ArrayList<>();

        for (ScheduledTransfer st : scheduledTransfers) {
            String carteSourceNum = getFullCardNumberById(st.getFromCardId());
            String carteDestNum = getFullCardNumberById(st.getToCardId());

            String email = "";
            String nom = "";
            CarteVirtuelle carte = carteCache.get(st.getFromCardId());
            if (carte != null) {
                Utilisateur proprietaire = utilisateurService.getUserByCardId(carte.getId());
                if (proprietaire != null) {
                    email = proprietaire.getEmail();
                    nom = proprietaire.getPrenom() + " " + proprietaire.getNom();
                }
            }

            String frequence = "Une fois";
            data.add(new ScheduledTransferExport.ScheduledTransferData(
                    st, carteSourceNum, carteDestNum, email, nom, frequence));
        }

        return data;
    }

    private String getFullCardNumberById(int cardId) {
        CarteVirtuelle c = carteCache.get(cardId);
        return c != null ? c.getNumeroCarte() : "Inconnue";
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.showAndWait();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }
}