package Controllers;

import Models.Compte;
import Services.ServiceCompte;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLDataException;
import java.util.List;

public class DashboardController {

    @FXML private VBox containerScores;
    @FXML private Label lblScoreGlobal, lblTotalAvoirs, lblUsagePercent, lblFiabilitePercent;
    @FXML private Label lblMaxPret; // AJOUTÉ : Pour l'affichage dynamique dans la carte orange
    @FXML private ProgressBar progressUsage, progressFiabilite;
    @FXML private PieChart chartComptes;

    private ServiceCompte serviceCompte = new ServiceCompte();
    private static DashboardController instance;

    // AJOUTÉ : Stockage du montant max pour le passer au simulateur
    private double montantMaxCalcule = 0;

    public DashboardController() { instance = this; }
    public static DashboardController getInstance() { return instance; }

    @FXML
    public void initialize() {
        try {
            chargerTableauBord();
        } catch (SQLDataException e) { e.printStackTrace(); }
    }

    private void chargerTableauBord() throws SQLDataException {
        List<Compte> comptes = serviceCompte.recuperer();

        // 1. Calcul du solde total
        double totalSolde = comptes.stream().mapToDouble(Compte::getSolde).sum();
        lblTotalAvoirs.setText(String.format("%.2f DT", totalSolde));

        // 2. Calcul des scores individuels et de la moyenne
        containerScores.getChildren().clear();
        double sommeScores = 0;
        for (Compte c : comptes) {
            double score = serviceCompte.calculerScoreSolvabilite(c);
            sommeScores += score;
            containerScores.getChildren().add(creerLigneScore(c, score));
        }

        double moyenne = comptes.isEmpty() ? 0 : sommeScores / comptes.size();
        lblScoreGlobal.setText((int)moyenne + "%");

        // 3. CALCUL DYNAMIQUE DU PRÊT MAXIMUM
        // Formule : (Solde Total * 4) pondéré par le score de confiance
        this.montantMaxCalcule = (totalSolde * 4) * (moyenne / 100);
        // Arrondi à la centaine la plus proche
        this.montantMaxCalcule = Math.floor(this.montantMaxCalcule / 100) * 100;

        if(lblMaxPret != null) {
            lblMaxPret.setText(String.format("%,.0f DT", this.montantMaxCalcule));
        }

        // 4. Mise à jour des graphiques
        remplirPieChart(comptes);
        mettreAJourRisque(totalSolde, moyenne);
    }

    private void mettreAJourRisque(double totalSolde, double scoreMoyen) {
        // Seuil arbitraire de 50k pour l'exemple de jauge
        double usage = Math.min(1.0, totalSolde / 50000.0);
        progressUsage.setProgress(usage);
        lblUsagePercent.setText((int)(usage * 100) + "%");

        double fiabilite = scoreMoyen / 100.0;
        progressFiabilite.setProgress(fiabilite);
        lblFiabilitePercent.setText((int)(fiabilite * 100) + "%");
    }

    private void remplirPieChart(List<Compte> comptes) {
        long courant = comptes.stream().filter(c -> c.getTypeCompte().equalsIgnoreCase("COURANT")).count();
        long epargne = comptes.stream().filter(c -> c.getTypeCompte().equalsIgnoreCase("EPARGNE")).count();
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("Courant (" + courant + ")", courant),
                new PieChart.Data("Épargne (" + epargne + ")", epargne));
        chartComptes.setData(data);
    }

    private HBox creerLigneScore(Compte c, double score) {
        HBox row = new HBox(20);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 15;");

        VBox info = new VBox(
                new Label("Compte " + c.getNumeroCompte()),
                new Label(c.getTypeCompte().toUpperCase())
        );

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        ProgressBar pb = new ProgressBar(score / 100.0);
        pb.setPrefWidth(180);
        pb.getStyleClass().add(score < 40 ? "custom-progress-orange" : "custom-progress-blue");

        row.getChildren().addAll(info, sp, pb, new Label((int)score + "%"));
        return row;
    }

    @FXML
    private void handleSimulerPret() {
        try {
            // Vérification du chemin du FXML
            URL fxmlLocation = getClass().getResource("/fxml/SimulateurPret.fxml");
            if (fxmlLocation == null) {
                fxmlLocation = getClass().getResource("/Views/SimulateurPret.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent simulatorNode = loader.load();

            // 1. Créer l'overlay global (couvre Sidebar + Dashboard)
            StackPane globalOverlay = new StackPane();
            globalOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);"); // Un peu plus sombre pour le contraste
            globalOverlay.getChildren().add(simulatorNode);

            // 2. Récupérer la racine absolue de la fenêtre (StackPane principal)
            StackPane rootPane = (StackPane) lblScoreGlobal.getScene().getRoot();

            // 3. Configurer le contrôleur du simulateur
            SimulateurPretController controller = loader.getController();
            controller.setOverlayParent(rootPane, globalOverlay);

            // AJOUTÉ : Transmission de la limite calculée au simulateur
            controller.configurerLimiteDyna(this.montantMaxCalcule);

            // 4. Afficher par-dessus tout
            rootPane.getChildren().add(globalOverlay);

        } catch (IOException e) {
            System.err.println("Erreur chargement simulateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetour() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/AfficherCompte.fxml"));
            lblScoreGlobal.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}