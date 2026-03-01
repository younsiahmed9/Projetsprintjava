package Controllers;

import Models.Document;
import Services.ServiceDocument;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TrashViewController {

    @FXML
    private VBox containerTrash;
    @FXML
    private Label lblCount;
    @FXML
    private Button btnBack;
    @FXML
    private Button btnPurge;
    @FXML
    private Label lblTrashInfo;

    private final ServiceDocument docService = new ServiceDocument();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        loadTrash();
        btnBack.setOnAction(e -> goBack());
        btnPurge.setOnAction(e -> purgeTrash());
    }

    private void loadTrash() {
        try {
            containerTrash.getChildren().clear();
            List<Document> deletedDocs = docService.findAllDeleted();
            for (Document doc : deletedDocs) {
                containerTrash.getChildren().add(createTrashCard(doc));
            }
            lblCount.setText(deletedDocs.size() + " document(s) dans la corbeille");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger la corbeille: " + e.getMessage());
        }
    }

    private VBox createTrashCard(Document doc) {
        VBox card = new VBox(10);
        card.getStyleClass().add("trash-card");

        Label lblTitle = new Label("📄 " + doc.getTitre());
        lblTitle.getStyleClass().add("trash-title");

        Label lblDeletedAt = new Label(
                "Supprimé le: " + (doc.getDeletedAt() != null ? doc.getDeletedAt().format(DATE_FORMAT) : "N/A"));
        lblDeletedAt.getStyleClass().add("trash-date");

        HBox actions = new HBox(15);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button btnRestore = new Button("🔄 Restaurer");
        btnRestore.getStyleClass().add("btn-restore-premium");
        btnRestore.setOnAction(e -> restoreDocument(doc));

        Button btnDelete = new Button("🗑️ Supprimer Déf.");
        btnDelete.getStyleClass().add("btn-hard-delete-premium");
        btnDelete.setOnAction(e -> hardDeleteDocument(doc));

        actions.getChildren().addAll(btnRestore, btnDelete);
        card.getChildren().addAll(lblTitle, lblDeletedAt, actions);

        return card;
    }

    private void restoreDocument(Document doc) {
        try {
            docService.restore(doc.getId());
            AlertUtils.showSuccess("Succès", "Document restauré avec succès !");
            loadTrash();
            if (DocumentViewController.getCurrentInstance() != null) {
                DocumentViewController.getCurrentInstance().refresh();
            }
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de restaurer le document: " + e.getMessage());
        }
    }

    private void hardDeleteDocument(Document doc) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer définitivement ?");
        confirm.setContentText("Cette action est irréversible. Le document sera définitivement effacé.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                docService.hardDelete(doc.getId());
                AlertUtils.showSuccess("Succès", "Document supprimé définitivement.");
                loadTrash();
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de supprimer le document: " + e.getMessage());
            }
        }
    }

    private void purgeTrash() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Vider la corbeille");
        confirm.setHeaderText("Supprimer tous les documents ?");
        confirm.setContentText("Voulez-vous supprimer définitivement tous les éléments de la corbeille ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                List<Document> deletedDocs = docService.findAllDeleted();
                for (Document doc : deletedDocs) {
                    docService.hardDelete(doc.getId());
                }
                AlertUtils.showSuccess("Succès", "La corbeille a été vidée.");
                loadTrash();
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Erreur lors du vidage: " + e.getMessage());
            }
        }
    }

    private void goBack() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().loadDocumentView();
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/document_view.fxml"));
                Scene scene = new Scene(loader.load());
                Stage stage = (Stage) btnBack.getScene().getWindow();
                stage.setScene(scene);
            } catch (IOException e) {
                AlertUtils.showError("Erreur", "Impossible de retourner à la vue principale");
            }
        }
    }
}
