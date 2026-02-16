package Controllers;

import Models.Document;
import Services.ServiceDocument;
// import Controllers.Dialogs.DeleteConfirmationDialog;
import Controllers.Dialogs.CrudDialogManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class DocumentListCell extends ListCell<Document> {
    private final Label lblTitre;
    private final Label lblDossier;
    private final Label lblPath;
    private final Label lblUploaded;
    private final Button editBtn;
    private final Button deleteBtn;
    private HBox container;
    private final ServiceDocument docService = new ServiceDocument();

    public DocumentListCell() {
        lblTitre = new Label();
        lblTitre.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        lblDossier = new Label();
        lblDossier.setStyle("-fx-font-size: 12; -fx-text-fill: #1d4ed8;");

        lblPath = new Label();
        lblPath.setStyle("-fx-font-size: 11; -fx-text-fill: #6b7280;");
        lblPath.setWrapText(true);

        lblUploaded = new Label();
        lblUploaded.setStyle("-fx-font-size: 11; -fx-text-fill: #9ca3af;");

        editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("btn-modify");
        editBtn.setStyle("-fx-font-size: 12; -fx-min-width: 110; -fx-pref-width: 110; -fx-max-width: 110; -fx-text-overrun: clip;");

        deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setStyle("-fx-font-size: 12; -fx-min-width: 110; -fx-pref-width: 110; -fx-max-width: 110; -fx-text-overrun: clip;");

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(lblTitre, lblDossier, lblPath, lblUploaded);
        VBox.setVgrow(infoBox, Priority.ALWAYS);

        HBox actionsBox = new HBox(10, editBtn, deleteBtn);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        container.getChildren().addAll(infoBox, actionsBox);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
    }

    @Override
    protected void updateItem(Document item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            lblTitre.setText(item.getTitre());
            lblDossier.setText("📁 " + (item.getDossier() != null ? item.getDossier().getNom() : "N/A"));
            lblPath.setText("📄 " + item.getFilePath());

            if (item.getUploadedAt() != null) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                lblUploaded.setText("Uploadé le: " + item.getUploadedAt().format(fmt));
            } else {
                lblUploaded.setText("");
            }

            editBtn.setOnAction(e -> editDocument(item));
            deleteBtn.setOnAction(e -> deleteDocument(item));
            setGraphic(container);
        }
    }

    private void editDocument(Document document) {
        CrudDialogManager dialogManager = new CrudDialogManager();
        dialogManager.showDocumentDialog(document, true).ifPresent(updated -> {
            try {
                docService.update(updated);
                getListView().getItems().set(getIndex(), updated);
                getListView().refresh();
                AlertUtils.showSuccess("Succès", "Document modifié avec succès !");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de modifier le document : " + e.getMessage());
            }
        });
    }

    private void deleteDocument(Document document) {
        // Utilisé seulement par l'ancien système ListView - désactivé
        /*
        if (DeleteConfirmationDialog.showAndWait(
            "Document",
            document.getTitre(),
            "Le fichier \"" + document.getFilePath() + "\" sera supprimé.")) {

            try {
                docService.delete(document.getId());
                AlertUtils.showSuccess("Succès", "Document supprimé avec succès !");
                getListView().getItems().remove(getItem());
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de supprimer le document : " + e.getMessage());
            }
        }
        */
    }
}
