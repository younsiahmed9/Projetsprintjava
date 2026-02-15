package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class MainController {
    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        loadDocumentView();
    }

    /**
     * Charge la vue principale des documents
     */
    private void loadDocumentView() {
        try {
            Node view = FXMLLoader.load(getClass().getResource("/fxml/document_view.fxml"));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

