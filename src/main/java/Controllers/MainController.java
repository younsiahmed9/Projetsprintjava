package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class MainController {
    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnDocuments;

    @FXML
    public void initialize() {
        loadDashboardView();
        setActiveButton(btnDashboard);
    }

    /**
     * Charge la vue Dashboard
     */
    @FXML
    private void loadDashboardView() {
        loadView("/fxml/dashboard_view.fxml");
        setActiveButton(btnDashboard);
    }

    /**
     * Charge la vue principale des documents
     */
    @FXML
    private void loadDocumentView() {
        loadView("/fxml/document_view.fxml");
        setActiveButton(btnDocuments);
    }

    /**
     * Méthode générique pour charger une vue
     */
    private void loadView(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur de chargement", "Impossible de charger la vue: " + e.getMessage());
        }
    }

    /**
     * Définit le bouton actif dans la sidebar
     */
    private void setActiveButton(Button activeBtn) {
        // Réinitialiser tous les boutons
        if (btnDashboard != null) btnDashboard.getStyleClass().removeAll("active-menu", "nav-btn-active");
        if (btnDocuments != null) btnDocuments.getStyleClass().removeAll("active-menu", "nav-btn-active");

        // Activer le bouton sélectionné
        if (activeBtn != null && !activeBtn.getStyleClass().contains("active-menu")) {
            activeBtn.getStyleClass().add("active-menu");
        }
    }
}

