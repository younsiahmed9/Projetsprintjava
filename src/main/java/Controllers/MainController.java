package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class MainController {
    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnDocuments;
    @FXML
    private Button btnTrash;

    private static MainController instance;

    public MainController() {
        instance = this;
    }

    public static MainController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        loadDashboardView();
        if (btnDashboard != null)
            setActiveButton(btnDashboard);
    }

    /**
     * Navigation globale avec sélection de document
     */
    public static void navigateToDocumentsWithSelect(int documentId) {
        if (instance != null) {
            instance.loadDocumentView();
            // Attendre un peu que la vue soit chargée pour sélectionner
            javafx.application.Platform.runLater(() -> {
                DocumentViewController controller = DocumentViewController.getCurrentInstance();
                if (controller != null) {
                    controller.selectDocumentById(documentId);
                }
            });
        }
    }

    @FXML
    public void loadDashboardView() {
        loadView("/fxml/dashboard_view.fxml");
        if (btnDashboard != null)
            setActiveButton(btnDashboard);
    }

    @FXML
    public void loadDocumentView() {
        loadView("/fxml/document_view.fxml");
        if (btnDocuments != null)
            setActiveButton(btnDocuments);
    }

    @FXML
    public void loadTrashView() {
        loadView("/fxml/trash_view.fxml");
        if (btnTrash != null)
            setActiveButton(btnTrash);
    }

    private void loadView(String fxmlPath) {
        try {
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                throw new java.io.IOException("Le fichier FXML est introuvable au chemin : " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            if (e.getCause() != null) {
                errorMsg += "\n\nCause probable : " + e.getCause().getMessage();
            }
            AlertUtils.showError("Erreur de chargement",
                    "Erreur fatale lors du chargement de la vue [" + fxmlPath + "]\n\nDétails : " + errorMsg);
        }
    }

    private void setActiveButton(Button activeBtn) {
        if (btnDashboard != null)
            btnDashboard.getStyleClass().removeAll("active-menu", "nav-btn-active");
        if (btnDocuments != null)
            btnDocuments.getStyleClass().removeAll("active-menu", "nav-btn-active");
        if (btnTrash != null)
            btnTrash.getStyleClass().removeAll("active-menu", "nav-btn-active");

        if (activeBtn != null) {
            activeBtn.getStyleClass().add("active-menu");
        }
    }
}
