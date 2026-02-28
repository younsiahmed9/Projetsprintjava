package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private BorderPane mainPane;

    // Navigation items
    @FXML private HBox navServices;
    @FXML private HBox navFactures;
    @FXML private HBox navComptes;
    @FXML private HBox navDepenses;
    @FXML private HBox navBudget;
    @FXML private HBox navDocuments;
    @FXML private HBox navDashboard;

    // Circles pour les indicateurs visuels
    @FXML private Circle circleServices;
    @FXML private Circle circleFactures;
    @FXML private Circle circleComptes;
    @FXML private Circle circleDepenses;
    @FXML private Circle circleBudget;
    @FXML private Circle circleDocuments;
    @FXML private Circle circleDashboard;

    // Textes pour la navigation
    @FXML private Text textServices;
    @FXML private Text textFactures;
    @FXML private Text textComptes;
    @FXML private Text textDepenses;
    @FXML private Text textBudget;
    @FXML private Text textDocuments;
    @FXML private Text textDashboard;

    private ServiceController serviceController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initialisation du MainController...");
        // Charger la vue par défaut (Services)
        chargerVueServices();
        setActiveNav("services");
    }

    private void chargerVueServices() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/serviceView.fxml"));
            Parent view = loader.load();
            serviceController = loader.getController();
            mainPane.setCenter(view);
            System.out.println("✅ Vue des services chargée");
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement serviceView.fxml: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger la vue des services:\n" + e.getMessage());
        }
    }

    private void chargerVueFactures() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/FactureView.fxml"));
            Parent view = loader.load();
            mainPane.setCenter(view);
            System.out.println("✅ Vue des factures chargée");
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement FactureView.fxml: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger la vue des factures:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleServices() {
        chargerVueServices();
        setActiveNav("services");
    }

    @FXML
    private void handleFactures() {
        chargerVueFactures();
        setActiveNav("factures");
    }

    @FXML
    private void handleComptes() {
        showInfo("Info", "Page des comptes en cours de développement");
        setActiveNav("comptes");
    }

    @FXML
    private void handleDepenses() {
        showInfo("Info", "Page des dépenses en cours de développement");
        setActiveNav("depenses");
    }

    @FXML
    private void handleBudget() {
        showInfo("Info", "Page du budget en cours de développement");
        setActiveNav("budget");
    }

    @FXML
    private void handleDocuments() {
        showInfo("Info", "Page des documents en cours de développement");
        setActiveNav("documents");
    }

    @FXML
    private void handleDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/DashboardView.fxml"));
            Parent view = loader.load();
            mainPane.setCenter(view);
            setActiveNav("dashboard");
        } catch (IOException e) {
            showInfo("Info", "Page du dashboard en cours de développement");
        }
    }

    private void setActiveNav(String activeItem) {
        // Réinitialiser tous les styles
        resetNavStyles();

        // Appliquer le style actif selon l'élément sélectionné
        switch (activeItem) {
            case "services":
                if (navServices != null) {
                    navServices.setStyle("-fx-background-color: #d2e7fe; -fx-background-radius: 10;");
                }
                if (circleServices != null) {
                    circleServices.setStyle("-fx-fill: #182d88; -fx-opacity: 1;");
                }
                if (textServices != null) {
                    textServices.setStyle("-fx-font-weight: bold; -fx-fill: #182d88;");
                }
                break;
            case "factures":
                if (navFactures != null) {
                    navFactures.setStyle("-fx-background-color: #d2e7fe; -fx-background-radius: 10;");
                }
                if (circleFactures != null) {
                    circleFactures.setStyle("-fx-fill: #182d88; -fx-opacity: 1;");
                }
                if (textFactures != null) {
                    textFactures.setStyle("-fx-font-weight: bold; -fx-fill: #182d88;");
                }
                break;
            case "comptes":
                if (navComptes != null) {
                    navComptes.setStyle("-fx-background-color: #d2e7fe; -fx-background-radius: 10;");
                }
                if (circleComptes != null) {
                    circleComptes.setStyle("-fx-fill: #182d88; -fx-opacity: 1;");
                }
                if (textComptes != null) {
                    textComptes.setStyle("-fx-font-weight: bold; -fx-fill: #182d88;");
                }
                break;
            case "depenses":
                if (navDepenses != null) {
                    navDepenses.setStyle("-fx-background-color: #d2e7fe; -fx-background-radius: 10;");
                }
                if (circleDepenses != null) {
                    circleDepenses.setStyle("-fx-fill: #182d88; -fx-opacity: 1;");
                }
                if (textDepenses != null) {
                    textDepenses.setStyle("-fx-font-weight: bold; -fx-fill: #182d88;");
                }
                break;
            case "budget":
                if (navBudget != null) {
                    navBudget.setStyle("-fx-background-color: #d2e7fe; -fx-background-radius: 10;");
                }
                if (circleBudget != null) {
                    circleBudget.setStyle("-fx-fill: #182d88; -fx-opacity: 1;");
                }
                if (textBudget != null) {
                    textBudget.setStyle("-fx-font-weight: bold; -fx-fill: #182d88;");
                }
                break;
            case "documents":
                if (navDocuments != null) {
                    navDocuments.setStyle("-fx-background-color: #d2e7fe; -fx-background-radius: 10;");
                }
                if (circleDocuments != null) {
                    circleDocuments.setStyle("-fx-fill: #182d88; -fx-opacity: 1;");
                }
                if (textDocuments != null) {
                    textDocuments.setStyle("-fx-font-weight: bold; -fx-fill: #182d88;");
                }
                break;
            case "dashboard":
                if (navDashboard != null) {
                    navDashboard.setStyle("-fx-background-color: #d2e7fe; -fx-background-radius: 10;");
                }
                if (circleDashboard != null) {
                    circleDashboard.setStyle("-fx-fill: #182d88; -fx-opacity: 1;");
                }
                if (textDashboard != null) {
                    textDashboard.setStyle("-fx-font-weight: bold; -fx-fill: #182d88;");
                }
                break;
        }
    }

    private void resetNavStyles() {
        // Réinitialiser les styles de tous les éléments de navigation
        HBox[] navs = {navServices, navFactures, navComptes, navDepenses, navBudget, navDocuments, navDashboard};
        for (HBox nav : navs) {
            if (nav != null) {
                nav.setStyle("-fx-background-color: transparent;");
            }
        }

        Circle[] circles = {circleServices, circleFactures, circleComptes, circleDepenses, circleBudget, circleDocuments, circleDashboard};
        for (Circle circle : circles) {
            if (circle != null) {
                circle.setStyle("-fx-fill: #182d88; -fx-opacity: 0.5;");
            }
        }

        Text[] texts = {textServices, textFactures, textComptes, textDepenses, textBudget, textDocuments, textDashboard};
        for (Text text : texts) {
            if (text != null) {
                text.setStyle("-fx-font-weight: normal; -fx-fill: #182d88;");
            }
        }
    }

    @FXML
    private void ajouterService() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/AjoutServiceDialog.fxml"));
            Parent root = loader.load();

            AjoutServiceController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un service");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (serviceController != null && controller.isOkClicked()) {
                serviceController.chargerDonnees();
            }

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void ajouterProduit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaces/AjoutProduitDialog.fxml"));
            Parent root = loader.load();

            AjoutProduitController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un produit");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (serviceController != null && controller.isOkClicked()) {
                serviceController.chargerDonnees();
            }

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeconnexion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/interfaces/LoginView.fxml"));
                    Stage stage = (Stage) mainPane.getScene().getWindow();
                    stage.setScene(new Scene(root));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleQuitter() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quitter");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment quitter l'application ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.exit(0);
            }
        });
    }

    @FXML
    private void handleProfil() {
        showInfo("Info", "Page de profil en cours de développement");
    }

    @FXML
    private void handleParametres() {
        showInfo("Info", "Page des paramètres en cours de développement");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}